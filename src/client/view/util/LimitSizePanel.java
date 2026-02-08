package client.view.util;

import javax.swing.*;
import java.awt.*;

/**
 * 一个可以限制最小宽度的面板，由于splitPane中
 */
public class LimitSizePanel extends JPanel {
    private int minWidth;

    public LimitSizePanel(int minWidth) {
        super();
        this.minWidth = minWidth;
    }

    @Override
    public Dimension getMinimumSize() {
        // 设置最小尺寸
        Dimension dim = super.getMinimumSize();
        dim.width = Math.max(dim.width, minWidth);
        return dim;
    }

    @Override
    public Dimension getPreferredSize() {
        // 设置首选尺寸
        Dimension dim = super.getPreferredSize();
        dim.width = Math.max(dim.width, minWidth * 2);
        return dim;
    }
}
