package client;

import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.*;
import javax.swing.Timer;

public class Function1Window extends JFrame {
    private Client client;
    private MainMenu mainMenu;

    private JLabel questionLabel;
    private JTextField answerField;
    private RoundedButton submitButton;
    private RoundedButton nextQuestionButton;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JTextArea feedbackArea;
    private String answer;

    private int score = 10;
    private Timer timer;
    private int timeLeft = 10; // 10秒倒计时
    private boolean timeOut = false; // 时间是否耗尽
    private Random random;
    private String currentEnglish;
    private String currentChinese;

    public Function1Window(Client client, MainMenu mainMenu) {
        this.client = client;
        this.mainMenu = mainMenu;
        random = new Random();
        initializeUI();
        setVisible(true);
    }

    // 初始化UI组件
    private void initializeUI() {
        setTitle("请根据以下中文补齐英文单词");
        setSize(600, 500);
        setLocationRelativeTo(null); // 居中显示
        setLayout(new BorderLayout());

        // 窗口关闭事件
        addWindowListener(new FunctionWindowAdapter());

        // 题目显示区域
        questionLabel = createQuestionLabel();
        add(questionLabel, BorderLayout.NORTH);

        // 答题区域
        JPanel answerPanel = createAnswerPanel();
        add(answerPanel, BorderLayout.CENTER);

        // 计时器和分数显示
        JPanel statusPanel = createStatusPanel();

        // 反馈信息显示区域
        feedbackArea = createFeedbackArea();
        JScrollPane feedbackScrollPane = new JScrollPane(feedbackArea);
        feedbackScrollPane.setPreferredSize(new Dimension(580, 100));

        // 创建一个容器面板来放置 statusPanel 和 feedbackScrollPane
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusPanel, BorderLayout.NORTH);
        southPanel.add(feedbackScrollPane, BorderLayout.CENTER);

        // 新增“下一题”按钮
        nextQuestionButton = createNextQuestionButton();
        southPanel.add(nextQuestionButton, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

    // 创建题目显示区域（JLabel）
    private JLabel createQuestionLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        label.setHorizontalAlignment(SwingConstants.CENTER); // 水平居中
        label.setVerticalAlignment(SwingConstants.CENTER); // 垂直居中（可选）
        label.setPreferredSize(new Dimension(580, 150));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加内边距
        return label;
    }

    // 创建答题区域
    private JPanel createAnswerPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        answerField = new JTextField();
        answerField.setPreferredSize(new Dimension(300, 30));
        submitButton = new RoundedButton("提交答案");
        submitButton.setFont(new Font("微软雅黑", Font.PLAIN, 16)); // 使用支持中文的字体
        panel.add(new JLabel("你的答案："));
        panel.add(answerField);
        panel.add(submitButton);

        // 提交按钮事件监听器
        submitButton.addActionListener(new SubmitButtonListener());

        // 回车键提交答案
        answerField.addKeyListener(new AnswerFieldKeyListener());

        return panel;
    }

    // 创建计时器和分数显示面板
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        Font chineseFont = new Font("微软雅黑", Font.BOLD, 14); // 使用支持中文的字体

        timerLabel = new JLabel("时间剩余：10秒");
        timerLabel.setFont(chineseFont);

        scoreLabel = new JLabel("当前分数：10分");
        scoreLabel.setFont(chineseFont);

        panel.add(timerLabel);
        panel.add(scoreLabel);

        return panel;
    }

    // 创建反馈信息显示区域
    private JTextArea createFeedbackArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("微软雅黑", Font.PLAIN, 16)); // 使用支持中文的字体
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    // 创建“下一题”按钮
    private RoundedButton createNextQuestionButton() {
        RoundedButton button = new RoundedButton("下一题"); // 使用 RoundedButton
        button.setFont(new Font("微软雅黑", Font.PLAIN, 16)); // 使用支持中文的字体
        button.setVisible(false); // 初始不可见
        button.addActionListener(new NextQuestionButtonListener());
        return button;
    }

    // 遮盖英文单词中的部分字母
    private String maskWord(String word) {
        if (word.length() <= 2) return word;
        int numToMask = word.length() - 2;
        char[] chars = word.toCharArray();
        Set<Integer> maskIndices = new HashSet<>();
        while (maskIndices.size() < numToMask) {
            int index = random.nextInt(chars.length);
            if (Character.isLetter(chars[index]) && !maskIndices.contains(index)) {
                maskIndices.add(index);
            }
        }
        for (int index : maskIndices) {
            chars[index] = '_';
        }
        return new String(chars);
    }

    // 显示题目
    public void displayQuestion(String chinese, String display) {
        timeOut = false;
        currentChinese = chinese;
        currentEnglish = display;
        String maskedDisplay = maskWord(display);

        // 使用HTML格式设置居中和换行
        String questionText = "<html><div style='text-align: center;'>"
                + chinese
                + "<br/>"
                + maskedDisplay
                + "</div></html>";
        questionLabel.setText(questionText);

        answerField.setText("");
        answerField.setEditable(true);
        submitButton.setEnabled(true);
        nextQuestionButton.setVisible(false); // 隐藏“下一题”按钮
        startTimer();
    }

    // 处理反馈
    public void handleFeedback(String feedback, String correctAnswer) {
        stopTimer();
        timeLeft = 0; // 将计时器归零
        timerLabel.setText("时间剩余：0秒");
        if (feedback.equalsIgnoreCase("正确")) {
            score += 1;
            logFeedback("恭喜回答正确！分数 +1。");
        } else if (feedback.equalsIgnoreCase("错误")) {
            score -= 2;
            logFeedback("回答错误，正确答案是: " + currentEnglish + "。分数 -2。");
        } else if (feedback.equalsIgnoreCase("未作答")) {
            score -= 1;
            logFeedback("未作答，正确答案是: " + currentEnglish + "。分数 -1。");
        }
        if(score < 0){
            score = 0;
        }
        updateScoreLabel();
        resetQuestionState();
        checkGameOver();

        // 显示“下一题”按钮
        submitButton.setEnabled(false);
        nextQuestionButton.setVisible(true);
    }

    // 提交答案
    private void submitAnswer(String answer) {
        timeOut = true;
        client.out.println(answer); // 发送答案到服务器
        submitButton.setEnabled(false);
        submitButton.setVisible(false);
        answerField.setEditable(false);
        stopTimer();
    }

    // 请求下一题
    private void requestNextQuestion() {
        feedbackArea.setText(""); // 清除反馈信息
        client.out.println("FUNCTION1"); // 发送新的题目请求到服务器
        nextQuestionButton.setVisible(false); // 隐藏“下一题”按钮
        resetQuestionState(); // 清除当前题目状态
        submitButton.setVisible(true);
    }

    // 启动计时器
    private void startTimer() {
        timeLeft = 10;
        timerLabel.setText("时间剩余：10秒");
        timer = new Timer(1000, new TimerActionListener());
        timer.start();
    }

    // 停止计时器
    private void stopTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    // 处理计时器超时
    private void handleTimeOut() {
        timeOut = true;
        answer = "ANSWER FUNCTION1 " + answerField.getText().trim();
        submitAnswer(answer); // 未回答，自动提交已输入答案
        updateScoreLabel();
        checkGameOver();

        timeLeft = 0;
        timerLabel.setText("时间剩余：0秒");

        submitButton.setEnabled(false);
        nextQuestionButton.setVisible(true);
    }

    // 更新分数显示
    private void updateScoreLabel() {
        scoreLabel.setText("当前分数：" + score + "分");
    }

    // 重置题目状态
    private void resetQuestionState() {
        currentEnglish = null;
        currentChinese = null;
        answerField.setText("");
        submitButton.setEnabled(true);
    }

    // 检查游戏是否结束
    private void checkGameOver() {
        if (score <= 0) {
            JOptionPane.showMessageDialog(this, "您的分数已降至0分，游戏结束！", "游戏结束", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            mainMenu.setEnabled(true);
        }
    }

    // 日志反馈显示
    private void logFeedback(String message) {
        feedbackArea.append(message + "\n");
    }

    // 窗口事件处理器
    private class FunctionWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            dispose(); // 关闭当前窗口
            mainMenu.setEnabled(true); // 启用主菜单窗口
        }
    }

    // 提交按钮事件监听器
    private class SubmitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            answer = "ANSWER FUNCTION1 " + answerField.getText().trim();
            submitAnswer(answer);
        }
    }

    // 回答输入框键盘监听器
    private class AnswerFieldKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if(!timeOut){
                    answer = "ANSWER FUNCTION1 " + answerField.getText().trim();
                    submitAnswer(answer);
                }
                else{
                    requestNextQuestion();
                }
            }
        }
    }

    // “下一题”按钮事件监听器
    private class NextQuestionButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            requestNextQuestion();
        }
    }

    // 计时器事件监听器
    private class TimerActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            timeLeft--;
            timerLabel.setText("时间剩余：" + timeLeft + "秒");
            if (timeLeft <= 0) {
                timer.stop();
                handleTimeOut();
            }
        }
    }
}
