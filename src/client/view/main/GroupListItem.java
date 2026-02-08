package client.view.main;

import client.service.LocalData;
import client.view.MainPage;
import client.view.util.CircleCharIcon2;
import client.view.util.DesignToken;
import client.view.util.RoundedRectCharIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// 群聊列表项组件
public class GroupListItem extends JPanel implements Comparable<String> {
    private int unread;
    private final String groupId;

    JLabel icon;
    JLabel badge;
    JPanel centerPanel;
    JLabel titleLabel;
    JPanel rightPanel; // 用于放置徽章

    /**
     * 构造函数
     *
     * @param groupId 群聊ID
     * @param title   群聊标题
     * @param unread  未读消息数量
     */
    public GroupListItem(String groupId, String title, int unread) {
        this.groupId = groupId;
        this.unread = unread;

        // 设置布局管理器
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        setPreferredSize(new Dimension(200, 70));

        // 左侧图标
        boolean isGroup = LocalData.get().getGroupData(groupId) != null;
        if (isGroup) {
            icon = new JLabel(new RoundedRectCharIcon(Color.decode(DesignToken.BUBBLE_COLOR_BLUE), Color.WHITE,
                    title.substring(0, 1).toUpperCase(), 40));
        } else {
            icon = new JLabel(new CircleCharIcon2(Color.ORANGE, Color.WHITE,
                    title.substring(0, 1).toUpperCase(), 40));
        }
        icon.setPreferredSize(new Dimension(40, 40));

        // 中间区域 - 群聊标题
        centerPanel = new JPanel(new GridLayout(1, 1));
        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(DesignToken.DEFAULT_FONT, Font.BOLD, 14));
        centerPanel.add(titleLabel);

        // 右侧区域 - 未读消息徽章
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);

        badge = new JLabel();
        badge.setForeground(Color.WHITE);
        badge.setBackground(Color.RED);
        badge.setOpaque(true);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setFont(new Font("Microsoft YaHei", Font.BOLD, 10));
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        badge.setPreferredSize(new Dimension(20, 20));

        updateBadge(); // 初始化徽章显示状态
        rightPanel.add(badge);

        // 组装
        this.add(icon, BorderLayout.WEST);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(rightPanel, BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                enterGroup();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(240, 240, 240)); // 悬停效果
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(UIManager.getColor("Panel.background")); // 恢复原背景
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(new Color(220, 220, 220)); // 点击效果
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (contains(e.getPoint())) {
                    setBackground(new Color(240, 240, 240));
                } else {
                    setBackground(UIManager.getColor("Panel.background"));
                }
            }
        });
    }

    /**
     * 更新徽章显示
     */
    private void updateBadge() {
        if (unread > 0) {
            String badgeText = unread > 99 ? "99+" : String.valueOf(unread);
            badge.setText(badgeText);
            badge.setVisible(true);

            // 根据文本长度调整徽章大小
            FontMetrics fm = badge.getFontMetrics(badge.getFont());
            int width = fm.stringWidth(badgeText) + 12;
            badge.setPreferredSize(new Dimension(width, 20));
        } else {
            badge.setVisible(false);
        }
    }

    /**
     * 更新组件
     * 将组件上对应的信息修改。
     * newUnread 是加在原有的unread上的
     */
    public void updateUI(String name, int newUnread) {
        titleLabel.setText(name);

        // 更新图标的首字母显示
        boolean isGroup = LocalData.get().getGroupData(groupId) != null;
        if (isGroup) {
            icon.setIcon(new RoundedRectCharIcon(Color.decode(DesignToken.BUBBLE_COLOR_BLUE), Color.WHITE,
                    name.substring(0, 1).toUpperCase(), 40));
        } else {
            icon.setIcon(new CircleCharIcon2(Color.ORANGE, Color.WHITE,
                    name.substring(0, 1).toUpperCase(), 40));
        }

        unread += newUnread;
        updateBadge();
    }

    /**
     * 点击事件
     * 当点击之后，使用UIUpdater来更新ContentView的UI，
     * 使得其加载新的群聊信息
     * 清空unread为0
     * UIUpdater在ContentView展示当前群聊信息的时候不会更新这个群聊的未读信息数量。
     */
    public void enterGroup() {
        unread = 0;
        LocalData.get().setCurrentChatId(groupId);

        updateBadge(); // 更新徽章显示

        String name;
        if (LocalData.get().getFriends().containsKey(groupId)) {
            name = LocalData.get().getFriends().get(groupId);
        } else {
            name = LocalData.get().getGroupName(groupId);
        }

        updateUI(name, 0);

        MainPage.get().exchangeToChatRoom(groupId);

        if (LocalData.get().getGroupData(groupId) != null) {
            System.out.println(
                    "进入群聊：" + groupId +
                            "，人数：" + LocalData.get().getGroupData(groupId).getMemberCount() +
                            ", 信息数量: " + LocalData.get().getChatMsg(groupId).size());
        } else {
            System.out.println("进入私聊：" + groupId);
        }

        MainPage.get().revalidate(); // 重新计算布局
        MainPage.get().repaint();
    }

    /**
     * 获取未读消息数量
     */
    public int getUnread() {
        return unread;
    }

    /**
     * 获取群组ID
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * 实现Comparable接口，用于排序
     * 按照群组ID进行排序
     *
     * @param o 要比较的对象
     * @return 比较结果
     */
    @Override
    public int compareTo(String o) {
        return this.groupId.compareTo(o);
    }
}