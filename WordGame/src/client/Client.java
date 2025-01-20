package client;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import javax.swing.*;
import receiverThread.ReceiverThread;

public class Client {
    private static final String SERVER_IP = "localhost"; // 服务器IP地址
    private static final int SERVER_PORT = 9999; // 服务器端口

    private static Socket socket;
    public BufferedReader in;
    public static PrintWriter out;

    public MainMenu mainMenu;
    private Function1Window function1Window;
    private Function2Window function2Window;
    private ReviewWindow reviewWindow;

    private ReceiverThread receiverThread;

    private static String nickName;//用户昵称

    public Client() {
        // 连接服务器
        connectToServer();

        // 显示主菜单窗口
        mainMenu = new MainMenu(this);

        // 设置主菜单窗口的默认关闭操作为 DO_NOTHING_ON_CLOSE
        mainMenu.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 添加窗口监听器以处理关闭事件
        mainMenu.addWindowListener(new MainMenuWindowListener());

        mainMenu.setVisible(true);
    }

    // 建立与服务器的连接
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            System.out.println("成功连接到服务器。");

            // 启动接收消息的线程
            receiverThread = new ReceiverThread(this, in);
            receiverThread.start();
        } catch (IOException e) {
            System.out.println("无法连接到服务器: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "无法连接到服务器，请检查网络连接。", "连接错误", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    // 发送功能请求到服务器
    public void sendFunctionRequest(String function) {
        if (out != null) {
            out.println(function);
        } else {
            System.out.println("无法发送功能选择，输出流未初始化。");
        }
    }

    // 处理服务器发送的消息
    public void processServerMessage(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                processServerMessageOnEDT(message);
            }
        });
    }

    // 在事件调度线程上处理服务器消息
    private void processServerMessageOnEDT(String message) {
        String[] parts = message.trim().split(" ", 3); // 分割为最多3部分
        if (parts.length < 1) return;

        String messageType = parts[0];

        if (messageType.equalsIgnoreCase("FUNCTION1_QUESTION")) {
            handleFunction1Question(parts);
        } else if (messageType.equalsIgnoreCase("FUNCTION2_QUESTION")) {
            handleFunction2Question(parts);
        } else if (messageType.equalsIgnoreCase("REVIEW_QUESTION")) {
            handleReviewQuestion(parts);
        } else if (messageType.equalsIgnoreCase("FEEDBACK")) {
            handleFeedback(parts);
        } else if (messageType.equalsIgnoreCase("FEEDBACK_ERROR")) {
            handleFeedbackError(parts);
        } else {
            System.out.println("未知的消息类型: " + messageType);
        }
    }

    // 处理功能1的问题
    private void handleFunction1Question(String[] parts) {
        if (parts.length < 3) {
            System.out.println("功能1题目格式错误。");
            return;
        }
        String chinese = parts[1];
        String display = parts[2];
        openFunction1Window(chinese, display);
    }

    // 处理功能2的问题
    private void handleFunction2Question(String[] parts) {
        if (parts.length < 3) {
            System.out.println("功能2题目格式错误。");
            return;
        }
        String english = parts[1];
        String optionsStr = parts[2];
        openFunction2Window(english, optionsStr);
    }

    // 处理复习的问题
    private void handleReviewQuestion(String[] parts) {
        if (parts.length < 3) {
            System.out.println("复习题目格式错误。");
            return;
        }
        String english = parts[1];
        String optionsStr = parts[2];
        openReviewWindow(english, optionsStr);
    }

    // 处理反馈消息
    private void handleFeedback(String[] parts) {
        if (parts.length < 2) {
            System.out.println("反馈消息格式错误。");
            return;
        }
        String feedback = parts[1];
        String correctAnswer = parts.length == 3 ? parts[2] : "";
        if (function1Window != null && function1Window.isVisible()) {
            function1Window.handleFeedback(feedback, correctAnswer);
        } else if (function2Window != null && function2Window.isVisible()) {
            function2Window.handleFeedback(feedback, correctAnswer);
        } else if (reviewWindow != null && reviewWindow.isVisible()) {
            reviewWindow.handleFeedback(feedback, correctAnswer);
        }
    }

    // 处理反馈错误消息
    private void handleFeedbackError(String[] parts) {
        if (parts.length < 2) {
            System.out.println("反馈错误消息格式错误。");
            return;
        }
        String errorMsg = parts[1];
        System.out.println("服务器错误: " + errorMsg);
    }

    // 打开功能1窗口
    private void openFunction1Window(String chinese, String display) {
        if (function1Window == null || !function1Window.isVisible()) {
            function1Window = new Function1Window(this, mainMenu);
            mainMenu.setEnabled(false); // 禁用主菜单窗口
        }
        function1Window.displayQuestion(chinese, display);
    }

    // 打开功能2窗口
    private void openFunction2Window(String english, String optionsStr) {
        if (function2Window == null || !function2Window.isVisible()) {
            function2Window = new Function2Window(this, mainMenu);
            mainMenu.setEnabled(false); // 禁用主菜单窗口
        }
        function2Window.displayQuestion(english, optionsStr);
    }

    // 打开复习窗口
    private void openReviewWindow(String english, String optionsStr) {
        if (reviewWindow == null || !reviewWindow.isVisible()) {
            reviewWindow = new ReviewWindow(this, mainMenu);
            mainMenu.setEnabled(false); // 禁用主菜单窗口
        }
        reviewWindow.displayQuestion(english, optionsStr);
    }
    // 关闭连接
    public void closeConnection() {
        try {
            if (receiverThread != null && receiverThread.isAlive()) {
                if (out != null) {
                    out.println("Interrupt");
                }
                receiverThread.interrupt();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("已断开与服务器的连接。");
        } catch (IOException e) {
            System.out.println("断开连接时发生错误: " + e.getMessage());
        }
    }

    // 主方法，启动客户端
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }

    // 主菜单窗口监听器，处理窗口关闭事件
    private class MainMenuWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            // 确认是否退出
            int confirm = JOptionPane.showConfirmDialog(mainMenu, "确定要退出游戏吗？", "退出确认", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                closeConnection();
                mainMenu.dispose();
                System.exit(0);
            }
        }
    }
}
