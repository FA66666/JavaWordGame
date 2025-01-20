package client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ReviewWindow extends JFrame {
    private Client client;
    private MainMenu mainMenu;

    private JLabel questionLabel; // 使用 JLabel 替代 JTextArea
    private RoundedButton[] optionButtons; // 替换为 RoundedButton
    private RoundedButton nextQuestionButton; // 替换为 RoundedButton
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JTextArea feedbackArea;

    private int score = 10;
    private Timer timer;
    private int timeLeft = 10;

    private String[] currentOptions;

    public ReviewWindow(Client client, MainMenu mainMenu) {
        this.client = client;
        this.mainMenu = mainMenu;

        setTitle("请选择以下英文单词的中文意思");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initializeComponents();
        initializeListeners();

        setVisible(true);
    }

    // 初始化组件
    private void initializeComponents() {
        // 题目显示区域
        questionLabel = createQuestionLabel();
        add(questionLabel, BorderLayout.NORTH);

        // 选项按钮区域
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        optionButtons = new RoundedButton[4];
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new RoundedButton(""); // 初始化时文本为空
            optionButtons[i].setPreferredSize(new Dimension(200, 50));
            optionsPanel.add(optionButtons[i]);
        }
        // 添加适当的边距
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(optionsPanel, BorderLayout.CENTER);

        // 状态显示区域
        JPanel statusPanel = new JPanel(new FlowLayout());
        Font chineseFont = new Font("微软雅黑", Font.BOLD, 14);

        timerLabel = new JLabel("时间剩余：10秒");
        timerLabel.setFont(chineseFont);

        scoreLabel = new JLabel("当前分数：10分");
        scoreLabel.setFont(chineseFont);

        statusPanel.add(timerLabel);
        statusPanel.add(scoreLabel);

        // 反馈信息显示区域
        feedbackArea = new JTextArea();
        feedbackArea.setEditable(false);
        feedbackArea.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        JScrollPane feedbackScrollPane = new JScrollPane(feedbackArea);
        feedbackScrollPane.setPreferredSize(new Dimension(580, 100));

        // “下一题”按钮
        nextQuestionButton = new RoundedButton("下一题"); // 使用 RoundedButton
        nextQuestionButton.setEnabled(false);
        nextQuestionButton.setVisible(false);

        // 底部面板
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusPanel, BorderLayout.NORTH);
        southPanel.add(feedbackScrollPane, BorderLayout.CENTER);
        southPanel.add(nextQuestionButton, BorderLayout.SOUTH);

        // 添加适当的边距
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(southPanel, BorderLayout.SOUTH);
    }

    // 创建题目显示区域 (使用 JLabel)
    private JLabel createQuestionLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font("微软雅黑", Font.PLAIN, 18)); // 使用支持中文的字体
        label.setHorizontalAlignment(SwingConstants.CENTER); // 水平居中
        label.setVerticalAlignment(SwingConstants.CENTER); // 垂直居中
        label.setPreferredSize(new Dimension(580, 150));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加内边距
        return label;
    }

    // 初始化事件监听器
    private void initializeListeners() {
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose(); // 关闭当前窗口
                mainMenu.setEnabled(true); // 启用主菜单窗口
            }
        });

        // 选项按钮事件
        for (int i = 0; i < optionButtons.length; i++) {
            final int index = i;
            optionButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    submitAnswer(currentOptions[index]);
                }
            });
        }

        // “下一题”按钮事件
        nextQuestionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                requestNextQuestion();
            }
        });
    }

    // 显示题目
    public void displayQuestion(String english, String optionsStr) {
        // 将文本内容连接为单行，使用空格或其他分隔符代替换行符
        questionLabel.setText("<html><body style='text-align: center;'>" + english + "</body></html>");
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER); // 确保居中对齐

        currentOptions = optionsStr.split(" ");
        for (int i = 0; i < optionButtons.length; i++) {
            if (i < currentOptions.length) {
                optionButtons[i].setText(currentOptions[i]);
                optionButtons[i].setEnabled(true);
            } else {
                optionButtons[i].setText("");
                optionButtons[i].setEnabled(false);
            }
        }
        feedbackArea.setText("");
        nextQuestionButton.setEnabled(false);
        nextQuestionButton.setVisible(false);
        startTimer();
    }

    // 处理反馈
    public void handleFeedback(String feedback, String correctAnswer) {
        stopTimer();
        if (feedback.equalsIgnoreCase("正确")) {
            score += 1;
            logFeedback("恭喜回答正确！分数 +1。");
        } else if (feedback.equalsIgnoreCase("错误")) {
            score -= 2;
            logFeedback("回答错误，正确答案是: " + correctAnswer + "。分数 -2。");
        } else if (feedback.equalsIgnoreCase("未作答")) {
            score -= 1;
            logFeedback("未作答，正确答案是: " + correctAnswer + "。分数 -1。");
        }
        if (score < 0) {
            score = 0;
        }
        updateScoreLabel();
        resetQuestionState();
        checkGameOver();

        nextQuestionButton.setEnabled(true);
        nextQuestionButton.setVisible(true);
    }

    // 提交答案
    private void submitAnswer(String selectedOption) {
        Client.out.println("ANSWER REVIEW " + selectedOption);
        for (RoundedButton btn : optionButtons) {
            btn.setEnabled(false);
        }
        stopTimer();
    }

    // 请求下一题
    private void requestNextQuestion() {
        client.sendFunctionRequest("REVIEW");
        feedbackArea.setText("");
        nextQuestionButton.setEnabled(false);
        nextQuestionButton.setVisible(false);
    }

    // 启动计时器
    private void startTimer() {
        timeLeft = 10;
        timerLabel.setText("时间剩余：10秒");
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText("时间剩余：" + timeLeft + "秒");
                if (timeLeft <= 0) {
                    timer.stop();
                    handleTimeOut();
                }
            }
        });
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
        submitAnswer(""); // 传递一个标识未作答的值
        updateScoreLabel();
        resetQuestionState();
        checkGameOver();
        nextQuestionButton.setVisible(true);
        nextQuestionButton.setEnabled(true);
    }

    // 更新分数显示
    private void updateScoreLabel() {
        scoreLabel.setText("当前分数：" + score + "分");
    }

    // 重置题目状态
    private void resetQuestionState() {
        currentOptions = null;
        for (RoundedButton btn : optionButtons) {
            btn.setEnabled(false);
        }
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
}
