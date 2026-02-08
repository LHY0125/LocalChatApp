package client.view.main;

import client.view.MainPage;
import client.view.util.CircleCharIcon2;
import client.view.util.DesignToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 好友列表项组件
 * 用于显示好友列表中的每个好友项，包括好友头像、好友名称等。
 * 点击好友项可以进入好友个人信息界面。
 */
public class FriendListItem extends JPanel {
    private final String userId;
    private final String userName;

    JLabel icon;
    JPanel centerPanel;
    JLabel titleLabel;

    /**
     * 构造好友列表项组件
     *
     * @param userId   好友用户ID
     * @param userName 好友用户名
     */
    public FriendListItem(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;

        // 设置布局管理器
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        setPreferredSize(new Dimension(200, 70));

        // 左侧图标
        icon = new JLabel(new CircleCharIcon2(Color.LIGHT_GRAY, Color.WHITE,
                userName.substring(0, 1).toUpperCase(), 40));
        icon.setPreferredSize(new Dimension(40, 40));

        // 中间区域 - 好友名称
        centerPanel = new JPanel(new GridLayout(1, 1));
        titleLabel = new JLabel(userName);
        titleLabel.setFont(new Font(DesignToken.DEFAULT_FONT, Font.BOLD, 14));
        centerPanel.add(titleLabel);

        // 组装
        this.add(icon, BorderLayout.WEST);
        this.add(centerPanel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                enterFriendProfile();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(UIManager.getColor("List.selectionBackground")); // 悬停效果
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(UIManager.getColor("Panel.background")); // 恢复原背景
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(UIManager.getColor("List.selectionInactiveBackground")); // 点击效果
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (contains(e.getPoint())) {
                    setBackground(UIManager.getColor("List.selectionBackground"));
                } else {
                    setBackground(UIManager.getColor("Panel.background"));
                }
            }
        });
    }

    /**
     * 进入好友个人信息界面
     * 点击好友项时，切换到好友个人信息界面，显示与该好友相关的个人信息。
     */
    public void enterFriendProfile() {
        MainPage.get().exchangeToFriendProfile(userId, userName);
    }
}