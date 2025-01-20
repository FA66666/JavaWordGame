package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {
    private Color normalBackground = new Color(0, 0, 0, 180); // 半透明黑色
    private Color hoverBackground = new Color(255, 255, 255, 180); // 半透明白色
    private Color pressedBackground = new Color(200, 200, 200, 180); // 按下时的颜色

    private Color normalForeground = Color.WHITE;
    private Color hoverForeground = Color.BLACK;
    private Color pressedForeground = Color.BLACK;

    private int cornerRadius = 30; // 圆角半径

    // 缩小后的字体大小
    private Font buttonFont = new Font("微软雅黑", Font.BOLD, 14);

    public RoundedButton(String text) {
        super(text);
        setContentAreaFilled(false); // 不填充内容区
        setFocusPainted(false); // 不绘制焦点
        setForeground(normalForeground);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFont(buttonFont); // 设置较小的字体
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // 内边距

        setBackground(normalBackground);

        // 添加鼠标监听器以更改背景和前景颜色
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverBackground);
                setForeground(hoverForeground);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalBackground);
                setForeground(normalForeground);
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(pressedBackground);
                setForeground(pressedForeground);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (contains(e.getPoint())) {
                    setBackground(hoverBackground);
                    setForeground(hoverForeground);
                } else {
                    setBackground(normalBackground);
                    setForeground(normalForeground);
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 启用抗锯齿
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制圆角矩形背景
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // 绘制按钮文本
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();

        // 计算可用宽度
        int availableWidth = getWidth() - getInsets().left - getInsets().right;

        // 如果文本过长，进行截断并添加省略号
        if (fm.stringWidth(text) > availableWidth) {
            while (fm.stringWidth(text + "...") > availableWidth && text.length() > 0) {
                text = text.substring(0, text.length() - 1);
            }
            text += "...";
        }

        Rectangle stringBounds = fm.getStringBounds(text, g2).getBounds();
        int textX = (getWidth() - stringBounds.width) / 2;
        int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();

        g2.setColor(getForeground());
        g2.setFont(getFont());
        g2.drawString(text, textX, textY);

        g2.dispose();
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
    }
}
