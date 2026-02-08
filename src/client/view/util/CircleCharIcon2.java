package client.view.util;

import javax.swing.*;
import java.awt.*;

public class CircleCharIcon2 implements Icon {
    private Color circleColor;
    private Color textColor;
    private String character;
    private int size;

    public CircleCharIcon2(Color circleColor, Color textColor, String character, int size) {
        this.circleColor = circleColor;
        this.textColor = textColor;
        this.character = character;
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;

        // 开启抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制圆形
        g2d.setColor(circleColor);
        g2d.fillOval(x, y, size - 1, size - 1);

        // 绘制字符
        g2d.setColor(textColor);
        g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, size / 2));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(character);
        int textHeight = fm.getAscent();
        int textX = x + (size - textWidth) / 2;
        int textY = y + (size - textHeight) / 2 + (int) (fm.getAscent() / 1.5);
        g2d.drawString(character, textX, textY);
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
