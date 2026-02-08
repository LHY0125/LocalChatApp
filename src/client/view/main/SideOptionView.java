package client.view.main;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import client.view.util.CircleCharIcon2;
import client.view.util.DesignToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 左侧侧边栏组件
 * 包含消息、好友、群聊、设置、黑暗模式切换按钮
 */
public class SideOptionView extends JPanel {
    private static volatile SideOptionView instance;

    /**
     * 获取单例实例
     * 
     * @return 单例实例
     */
    public static SideOptionView get() {
        if (instance == null) {
            synchronized (SideOptionView.class) {
                if (instance == null) {
                    instance = new SideOptionView();
                }
            }
        }
        return instance;
    }

    /**
     * 按照原型图，生成三个按钮组件：群聊，好友，设置组件
     * 每个组件都设置一个图标
     * 为每一个按钮配置一个事件
     * 群聊：exchangeToChatPage()/UIUpdate
     * 设置： exchangeToSettingPage()/UIUpdate。这个当前还没做，提示用户正在制作中。
     * 现在暂时没有设置相关功能
     */
    public SideOptionView() {
        this.setLayout(new GridLayout(5, 1));

        // this.setBackground(Color.GRAY);
        // 添加右侧边框分割线
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // 初始化五个核心按钮：消息、好友、群聊、设置、黑暗模式切换按钮
        initMessageButton();
        initFriendButton();
        initGroupButton();
        initSettingButton();
        initDarkModeButton();
    }

    /**
     * 初始化消息按钮
     * 点击后切换到消息页面
     */
    private void initMessageButton() {
        JButton messageBtn = createIconButton("消", Color.LIGHT_GRAY);
        messageBtn.setToolTipText("消息");
        messageBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SecondaryOptionView.get().exchangeToMessageList();
            }
        });
        this.add(messageBtn);
    }

    /**
     * 初始化好友按钮
     * 点击后切换到好友页面
     */
    private void initFriendButton() {
        JButton friendBtn = createIconButton("友", Color.LIGHT_GRAY);
        friendBtn.setToolTipText("好友");
        friendBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SecondaryOptionView.get().exchangeToFriendList();
            }
        });
        this.add(friendBtn);
    }

    /**
     * 初始化群聊按钮
     * 点击后切换到群聊页面
     */
    private void initGroupButton() {
        JButton groupBtn = createIconButton("群", Color.LIGHT_GRAY);
        groupBtn.setToolTipText("群聊");
        groupBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SecondaryOptionView.get().exchangeToGroupList();
            }
        });
        this.add(groupBtn);
    }

    /**
     * 初始化设置按钮
     * 点击后切换到设置页面
     */
    private void initSettingButton() {
        JButton settingBtn = createIconButton("设", Color.LIGHT_GRAY);
        settingBtn.setToolTipText("设置");
        settingBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SecondaryOptionView.get().exchangeToSettingList();
            }
        });
        this.add(settingBtn);
    }

    /**
     * 初始化黑暗模式切换按钮
     */
    private void initDarkModeButton() {
        JButton darkModeBtn = createIconButton("黑", Color.DARK_GRAY);
        darkModeBtn.setToolTipText("切换模式");
        darkModeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (FlatLaf.isLafDark()) {
                    try {
                        DesignToken.setDarkMode(false);
                        FlatLightLaf.setup();
                        FlatLaf.updateUI();
                        ChatInfoView.get().updateTheme();
                        // 切换图标
                        darkModeBtn.setIcon(new CircleCharIcon2(Color.DARK_GRAY, Color.WHITE, "黑", 36));
                        darkModeBtn.setToolTipText("切换到黑暗模式");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        DesignToken.setDarkMode(true);
                        FlatDarkLaf.setup();
                        FlatLaf.updateUI();
                        ChatInfoView.get().updateTheme();
                        // 切换图标
                        darkModeBtn.setIcon(new CircleCharIcon2(Color.LIGHT_GRAY, Color.BLACK, "白", 36));
                        darkModeBtn.setToolTipText("切换到明亮模式");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        this.add(darkModeBtn);
    }

    /**
     * 创建带圆形图标的按钮
     */
    private JButton createIconButton(String text, Color bgColor) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(40, 40));
        button.setIcon(new CircleCharIcon2(bgColor, Color.WHITE, text, 36));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        return button;
    }
}
