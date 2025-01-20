package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

import com.formdev.flatlaf.FlatLightLaf;

public class MainMenu extends JFrame {

    public MainMenu(Client client) {
        String nickName = null;
        while (nickName == null || nickName.trim().isEmpty()) {
            nickName = JOptionPane.showInputDialog("输入昵称");
            if (nickName == null || nickName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "输入不能为空，请重新输入", "错误", JOptionPane.WARNING_MESSAGE);
            }
        }

        Client.out.println("NICKNAME " + nickName);

        setTitle("考研单词记忆游戏     用户: " + nickName);
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        // 设置图标
        String path = "/Img/menuIcon.png";
        try {
            Image img = ImageIO.read(this.getClass().getResource(path));
            this.setIconImage(img);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 设置现代的外观（FlatLaf）
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 创建带有背景图片的面板
        BackgroundPanel backgroundPanel = new BackgroundPanel("/Img/background.jpg");
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // 创建一个半透明的覆盖层
        JPanel overlay = new JPanel();
        overlay.setOpaque(false);
        overlay.setLayout(new GridBagLayout());
        backgroundPanel.add(overlay, BorderLayout.CENTER);

        // 创建按钮面板，使用BoxLayout进行垂直排列，并添加边距
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(new EmptyBorder(20, 50, 20, 50));

        // 创建按钮并设置样式
        JButton function1Button = new RoundedButton("根据中文补齐英文");
        JButton function2Button = new RoundedButton("根据英文选择中文");
        JButton reviewButton = new RoundedButton("复习"); // 新增的复习按钮

        // 设置按钮的最大宽度和高度，使其在垂直排列时具有一致的尺寸
        function1Button.setMaximumSize(new Dimension(350, 70));
        function2Button.setMaximumSize(new Dimension(350, 70));
        reviewButton.setMaximumSize(new Dimension(350, 70)); // 设置复习按钮的尺寸

        // 在按钮之间添加垂直间距
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(function1Button);
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(function2Button);
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(reviewButton); // 添加复习按钮到面板
        buttonPanel.add(Box.createVerticalStrut(30));

        // 添加按钮面板到覆盖层，并居中显示
        overlay.add(buttonPanel, new GridBagConstraints());

        // 按钮事件监听器
        function1Button.addActionListener(e -> {
            client.sendFunctionRequest("FUNCTION1");
            MainMenu.this.setEnabled(false);
        });

        function2Button.addActionListener(e -> {
            client.sendFunctionRequest("FUNCTION2");
            MainMenu.this.setEnabled(false);
        });

        reviewButton.addActionListener(e -> {
            client.sendFunctionRequest("REVIEW");
            MainMenu.this.setEnabled(false);
        });
    }

    // 主方法示例
    public static void main(String[] args) {
        // 设置字体以支持中文显示
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
            MainMenu mainMenu = new MainMenu(client);
            mainMenu.setVisible(true);
        });
    }
}
