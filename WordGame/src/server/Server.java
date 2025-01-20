package server;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Server extends JFrame {
    private static final int PORT = 9999;

    private final JTextArea logArea;
    public Server() {
        setTitle("服务器端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(540, 360);
        setLayout(new BorderLayout());

        // 设置图标
        String path = "/Img/serverIcon.png";
        try{
            Image img = ImageIO.read(this.getClass().getResource(path));
            this.setIconImage(img);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 日志显示区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
        // 启动服务器
        startServer();
    }

    // 日志更新线程
    private class LogUpdater implements Runnable {
        private String message;

        public LogUpdater(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            logArea.append(message + "\n");
        }
    }

    // 日志记录方法
    private void log(String message) {
        SwingUtilities.invokeLater(new LogUpdater(message));
    }

    // 服务器线程，负责监听客户端连接
    private class ServerRunnable implements Runnable {
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                log("服务器已经启动，监听端口 " + PORT + "。");
                log("等待客户端连接……");
                while (true) {
                    Socket clientSocket = serverSocket.accept();

                    // 为每个客户端启动一个新的线程
                    Thread clientThread = new Thread(new ClientHandler(clientSocket, Server.this));
                    clientThread.start();
                }
            } catch (Exception e) {
                log("服务器异常: " + e.getMessage());
            }
        }
    }

    // 启动服务器
    private void startServer() {
        Thread serverThread = new Thread(new ServerRunnable());
        serverThread.start();
    }

    // 客户端处理线程
    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final Server server;
        private BufferedReader in;
        private PrintWriter out;

        private String nickName;

        private boolean wordLoaded = false;
        private boolean umwordLoaded = false;

        private final Map<String, String> wordsMap = new HashMap<>(); // 英文 -> 中文
        private final List<String> englishWords = new ArrayList<>(); // 英文单词列表

        private String currentFunction = null;
        private String currentEnglish = null;
        private String currentChinese = null;
        private List<String> currentOptions = new ArrayList<>();

        private String MASTERED_FILE = "的已掌握单词.txt";
        private String NOT_MASTERED_FILE = "的未掌握单词.txt";

        private final Random random = new Random();

        private static final String WORD_FILE = "word.txt";

        public ClientHandler(Socket socket, Server server) {
            this.clientSocket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            handleClient();
        }

        // 处理客户端连接
        private void handleClient() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    // 解析客户端消息
                    String[] parts = clientMessage.trim().split(" ", 3);
                    if (parts.length == 0) continue;
                    String receiveType = parts[0];

                    if (receiveType.equalsIgnoreCase("NICKNAME")) {
                        nickName = parts[1];
                        server.log("用户" + nickName + "已连接。");
                        MASTERED_FILE = nickName + "的已掌握单词.txt";
                        NOT_MASTERED_FILE = nickName + "的未掌握单词.txt";
                    } else {
                        if (receiveType.equalsIgnoreCase("FUNCTION1")) {
                            if(!wordLoaded){
                                umwordLoaded = false;
                                wordLoaded = true;
                                loadWords(WORD_FILE);
                            }
                            sendFunction1Question();
                        } else if (receiveType.equalsIgnoreCase("FUNCTION2")) {
                            if(!wordLoaded){
                                umwordLoaded = false;
                                wordLoaded = true;
                                loadWords(WORD_FILE);
                            }
                            sendFunction2Question();
                        } else if (receiveType.equalsIgnoreCase("ANSWER")) {
                            // 处理答案
                            handleAnswer(parts);
                        } else if (receiveType.equalsIgnoreCase("REVIEW")) {
                            if(!umwordLoaded){
                                wordLoaded = false;
                                umwordLoaded = true;
                                loadWords(NOT_MASTERED_FILE);
                            }
                            sendReviewQuestion();
                        } else if (receiveType.equalsIgnoreCase("Interrupt")) {
                            server.log("用户"+nickName+"已断开连接。");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                server.log("与客户端通信时发生错误: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    server.log("已关闭与客户端的连接: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    server.log("关闭客户端连接时发生错误: " + e.getMessage());
                }
            }
        }

        // 加载单词列表
        private void loadWords(String fileName) {
            server.log("正在加载单词表: " + fileName);
            wordsMap.clear();
            englishWords.clear();
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.trim().split(" ");
                    if (parts.length >= 2) {
                        String english = parts[0];
                        String chinese = parts[1];
                        wordsMap.put(english, chinese);
                        englishWords.add(english);
                    }
                }
                server.log(fileName + "中" + englishWords.size() + " 个单词已加载完成");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "无法加载词汇表文件: " + fileName);
                System.exit(0);
            }
        }

        // 获取随机英文单词
        private String getRandomEnglishWord() {
            int index = random.nextInt(englishWords.size());
            return englishWords.get(index);
        }

        // 发送功能1的题目
        private void sendFunction1Question() {
            String english = getRandomEnglishWord();
            String chinese = wordsMap.get(english);

            currentFunction = "FUNCTION1";
            currentEnglish = english;
            currentChinese = chinese;

            out.println("FUNCTION1_QUESTION " + chinese + " " + english);

            server.log("向用户"+ nickName +"发送功能1题目: 中文=" + chinese + ", 显示=" + english);
        }

        // 发送功能2的题目
        private void sendFunction2Question() {
            String english = getRandomEnglishWord();
            String chinese = wordsMap.get(english);

            List<String> options = new ArrayList<>();
            options.add(chinese); // 正确选项

            // 获取所有中文翻译，排除正确选项
            List<String> incorrectOptions = new ArrayList<>(wordsMap.values());
            incorrectOptions.remove(chinese);

            // 打乱错误选项并选择前三个
            Collections.shuffle(incorrectOptions);
            for (int i = 0; i < 3 && i < incorrectOptions.size(); i++) {
                options.add(incorrectOptions.get(i));
            }

            // 打乱所有选项
            Collections.shuffle(options);

            currentFunction = "FUNCTION2";
            currentEnglish = english;
            currentChinese = chinese;
            currentOptions = options;

            // 构建选项字符串
            StringBuilder optionsStr = new StringBuilder();
            for (String option : options) {
                optionsStr.append(option).append(" ");
            }
            if (!optionsStr.isEmpty()) {
                optionsStr.setLength(optionsStr.length() - 1);
            }

            out.println("FUNCTION2_QUESTION " + english + " " + optionsStr.toString());

            server.log("向用户"+ nickName +"发送功能2题目: 英文=" + english + ", 选项=" + optionsStr.toString());
        }

        // 发送复习的题目
        private void sendReviewQuestion() {
            String english = getRandomEnglishWord();
            String chinese = wordsMap.get(english);

            List<String> options = new ArrayList<>();
            options.add(chinese); // 正确选项

            // 获取所有中文翻译，排除正确选项
            List<String> incorrectOptions = new ArrayList<>(wordsMap.values());
            incorrectOptions.remove(chinese);

            // 打乱错误选项并选择前三个
            Collections.shuffle(incorrectOptions);
            for (int i = 0; i < 3 && i < incorrectOptions.size(); i++) {
                options.add(incorrectOptions.get(i));
            }

            // 打乱所有选项
            Collections.shuffle(options);

            currentFunction = "REVIEW";
            currentEnglish = english;
            currentChinese = chinese;
            currentOptions = options;

            // 构建选项字符串
            StringBuilder optionsStr = new StringBuilder();
            for (String option : options) {
                optionsStr.append(option).append(" ");
            }
            if (!optionsStr.isEmpty()) {
                optionsStr.setLength(optionsStr.length() - 1);
            }

            out.println("REVIEW_QUESTION " + english + " " + optionsStr.toString());

            server.log("向用户"+ nickName +"发送复习题目: 英文=" + english + ", 选项=" + optionsStr.toString());
        }

        // 处理客户端的答案
        private void handleAnswer(String[] parts) {
            if (currentFunction == null) {
                server.log("当前没有活动的题目。");
                out.println("FEEDBACK_ERROR " + "当前没有活动的题目。");
                return;
            }
            if (parts.length < 3) {
                out.println("FEEDBACK 未作答 " + currentChinese);
                saveWord(NOT_MASTERED_FILE, currentEnglish, currentChinese, "未作答");
                return;
            }

            String functionType = parts[1];
            String userAnswer = parts[2];

            if (functionType.equalsIgnoreCase("FUNCTION1")) {
                // 处理功能1答案
                handleFunction1Answer(userAnswer);
            } else if (functionType.equalsIgnoreCase("FUNCTION2")) {
                // 处理功能2答案
                handleFunction2Answer(userAnswer);
            } else if (functionType.equalsIgnoreCase("REVIEW")) {
                // 处理复习答案
                handleReviewAnswer(userAnswer);
            }
        }

        // 处理功能1的答案
        private void handleFunction1Answer(String userAnswer) {
            if (userAnswer.equalsIgnoreCase(currentEnglish)) {
                out.println("FEEDBACK 正确");
                saveWord(MASTERED_FILE, currentEnglish, currentChinese);
            } else {
                out.println("FEEDBACK 错误 " + currentEnglish);
                saveWord(NOT_MASTERED_FILE, currentEnglish, currentChinese, "错误");
            }
            resetCurrentQuestion();
        }

        // 处理功能2的答案
        private void handleFunction2Answer(String userAnswer) {
            if (userAnswer.equals(currentChinese)) {
                out.println("FEEDBACK 正确");
                saveWord(MASTERED_FILE, currentEnglish, currentChinese);
            } else {
                out.println("FEEDBACK 错误 " + currentChinese);
                saveWord(NOT_MASTERED_FILE, currentEnglish, currentChinese, "错误");
            }
            resetCurrentQuestion();
        }

        // 处理复习的答案
        private void handleReviewAnswer(String userAnswer) {
            if (userAnswer.equals(currentChinese)) {
                out.println("FEEDBACK 正确");
                saveWord(MASTERED_FILE, currentEnglish, currentChinese);
            } else {
                out.println("FEEDBACK 错误 " + currentChinese);
                saveWord(NOT_MASTERED_FILE, currentEnglish, currentChinese, "错误");
            }
            resetCurrentQuestion();
        }

        // 重置当前题目状态
        private void resetCurrentQuestion() {
            currentFunction = null;
            currentEnglish = null;
            currentChinese = null;
            currentOptions.clear();
        }
        // 保存单词到文件，附加原因
        private void saveWord(String filePath, String english, String chinese, String reason) {
            synchronized (this) { // 同步锁，确保线程安全
                try {
                    if (!isWordInFile(filePath, english)) { // 检查单词是否已经存在
                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
                            if (reason == null || reason.isEmpty()) {
                                bw.write(english + " " + chinese + " " + "时间: " + LocalDate.now());
                            } else {
                                bw.write(english + " " + chinese + " " + reason + " " + "时间: " + LocalDate.now());
                            }
                            bw.newLine();
                            server.log("保存单词到 " + filePath + ": " + english + " " + chinese + (reason != null ? " " + reason : ""));
                        }
                    } else {
                        server.log("单词已存在于 " + filePath + ": " + english + " " + chinese);
                    }
                } catch (IOException e) {
                    server.log("无法保存单词到文件: " + filePath + "，异常: " + e.getMessage());
                }
            }
        }

        // 保存单词到文件，不含原因
        private void saveWord(String filePath, String english, String chinese) {
            saveWord(filePath, english, chinese, null);
        }

        // 检查单词是否已经存在于文件中
        private boolean isWordInFile(String filePath, String english) {
            File file = new File(filePath);
            if (!file.exists()) {
                return false; // 文件不存在，意味着单词不存在
            }
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.trim().split(" ");
                    if (parts.length >= 2) {
                        String existingEnglish = parts[0];
                        if (existingEnglish.equalsIgnoreCase(english)) {
                            return true; // 找到匹配的单词
                        }
                    }
                }
            } catch (IOException e) {
                server.log("无法读取文件: " + filePath + "，异常: " + e.getMessage());
            }
            return false; // 文件中未找到该单词
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
