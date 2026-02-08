package client.view.util;

import javax.swing.*;
import java.awt.*;

/**
 * 圆角矩形字符图标
 */
public class RoundedRectCharIcon implements Icon {
    private Color bgColor;      // 背景颜色
    private Color textColor;    // 字体颜色
    private String character;   // 显示的字符
    private int size;           // 图标大小
    private int arc;            // 圆角大小

    /**
     * 构造函数
     *
     * @param bgColor   背景颜色
     * @param textColor 字体颜色
     * @param character 显示的字符
     * @param size      图标大小
     */
    public RoundedRectCharIcon(Color bgColor, Color textColor, String character, int size) {
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.character = character;
        this.size = size;
        this.arc = size / 3; // 圆角大小
    }

    /**
     * 绘制图标
     *
     * @param c  组件
     * @param g  图形上下文
     * @param x  图标左上角的 x 坐标
     * @param y  图标左上角的 y 坐标
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;

        // 开启抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制圆角矩形
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, size - 1, size - 1, arc, arc);

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

    /**
     * 获取图标宽度
     *
     * @return 图标宽度
     */
    @Override
    public int getIconWidth() {
        return size;
    }

    /**
     * 获取图标高度
     *
     * @return 图标高度
     */
    @Override
    public int getIconHeight() {
        return size;
    }
}