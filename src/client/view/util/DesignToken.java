package client.view.util;

import java.io.Serializable;

/**
 * 用于设定 UI元素的各种变量
 */
public class DesignToken implements Serializable {
    private static final long serialVersionUID = -3791869536619757015L;

    //============================窗口大小==============================
    // 主界面大小
    public final static int WINDOW_ORI_WIDTH = 1200;
    public final static int WINDOW_ORI_HEIGHT = 600;
    public final static int WINDOW_MIN_WIDTH = 800;
    public final static int WINDOW_MIN_HEIGHT = 600;
    // 登录界面大小
    public final static int LOGIN_WIDTH = 400;
    public final static int LOGIN_HEIGHT = 300;

    // 主界面组件大小
    public final static int SIDE_PANEL_WIDTH = 50;
    public final static int SECONDARY_PANEL_WIDTH = 200;
    public final static int SECONDARY_PANEL_WIDTH_MIN = 150;
    public final static int CONTENT_PANEL_WIDTH = 570;

    // 内容组件大小
    public final static int CHAT_PANEL_WIDTH = 400;
    public final static int INPUT_AREA_HEIGHT = 100;
    public final static int MSG_BUBBLE_WIDTH = 50;
    public final static int INFO_PANEL_WIDTH = 150;

    public final static int CONTENT_PANEL_WIDTH_MIN = 300;
    public final static int GROUP_INFO_PANEL_WIDTH = 240;
    public final static int GROUP_CHAT_PANEL_WIDTH = 210;

    //============================字体==================================
    public static int FONT_SIZE = 14;
    public static int FONT_SIZE_SMALL = 12;
    public static int FONT_SIZE_TITLE = 20;
    public static int FONT_SIZE_TITLE_MIN = 18;

    public static String DEFAULT_FONT = "微软雅黑";

    //=============================颜色=================================

    // public static String COLOR_BACKGROUND = "#ddebee";

    public static String BUBBLE_COLOR_GREEN = "#5aca58";
    public static String BUBBLE_COLOR_WHITE = "#c8cdcd";
    public static String BUBBLE_COLOR_GRAY = "#8a8a8a";
    public static String BUBBLE_COLOR_RED = "#ff4d4d";
    public static String BUBBLE_COLOR_BLUE = "#4097bc";
    public static String BUBBLE_COLOR_YELLOW = "#f7b500";

    public static String BACKGROUND_COLOR = "#c7d9d9";
    public static String EDGE_COLOR = "#aab7b7";

    public static String COLOR_FONT_BLUE = "#4097bc";// blue
    public static String COLOR_FONT_BLACK = "#000000";// black
    public static String COLOR_FONT_WHITE = "#ffffff";// white
    public static String COLOR_FONT_GRAY = "#8a8a8a";// gray
    public static String COLOR_FONT_GREEN = "#008000";// green
    public static String COLOR_FONT_RED = "#ff0000";// red
    public static String COLOR_FONT_YELLOW = "#ffff00";// yellow
    public static String COLOR_FONT_ORANGE = "#ff8c00";// orange
    public static String COLOR_FONT_PURPLE = "#800080";// purple

    public static String COLOR_HEAD_BLACK = "#000000"; // black
    public static String COLOR_HEAD_GRAY = "#8a8a8a"; // gray
    public static String COLOR_HEAD_WHITE = "#ffffff"; // white
    public static String COLOR_HEAD_BLUE = "#4097bc"; // blue
    public static String COLOR_HEAD_GREEN = "#008000"; // green
    public static String COLOR_HEAD_RED = "#ff0000"; // red
    public static String COLOR_HEAD_YELLOW = "#ffff00"; // yellow
    public static String COLOR_HEAD_ORANGE = "#ff8c00"; // orange
    public static String COLOR_HEAD_PURPLE = "#800080"; // purple

    public static void setDarkMode(boolean isDark) {
        if (isDark) {
            BACKGROUND_COLOR = "#3c3f41";
            EDGE_COLOR = "#5e6060";
            BUBBLE_COLOR_WHITE = "#505050";     // 白色气泡颜色（其他用户）
            COLOR_FONT_BLACK = "#dddddd";       // 黑色字体颜色（其他用户）
            BUBBLE_COLOR_GREEN = "#2e7d32";     // 绿色气泡颜色（自己）
        } else {
            BACKGROUND_COLOR = "#c7d9d9";
            EDGE_COLOR = "#aab7b7";
            BUBBLE_COLOR_WHITE = "#c8cdcd";
            COLOR_FONT_BLACK = "#000000";
            BUBBLE_COLOR_GREEN = "#5aca58";
        }
    }

    //=============================文本域输入字数限制=================================
    public static int MAX_FONT_SIZE = 20;
}
