package client.view.main;

import client.view.MainPage;
import client.view.util.LimitSizePanel;

import client.service.LocalData;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static client.view.util.DesignToken.SECONDARY_PANEL_WIDTH_MIN;

/**
 * 二级菜单栏组件
 * 用于显示聊天、好友、设置等二级选项。
 * 点击不同选项可以切换到对应的功能界面。
 */
public class SecondaryOptionView extends LimitSizePanel {
    private static volatile SecondaryOptionView instance;

    public static SecondaryOptionView get() {
        if (instance == null) {
            synchronized (SecondaryOptionView.class) {
                if (instance == null) {
                    instance = new SecondaryOptionView();
                }
            }
        }
        return instance;
    }

    // 二级选项模式
    private enum Mode {
        MESSAGE, FRIEND, GROUP, SETTING
    }

    // 当前二级选项模式
    private Mode currentMode = Mode.MESSAGE;
    // 这是一个群聊ID，组件的映射
    private Map<String, GroupListItem> listItems;
    // 关于好友ID，组件的映射
    private JPanel chatContainer;
    // 好友列表滚动面板
    private JScrollPane scrollPane;
    // 创建群聊按钮
    private JButton createGroupButton;
    // 二级选项标题标签
    private JLabel titleLabel;

    private SecondaryOptionView() {
        super(SECONDARY_PANEL_WIDTH_MIN);

        init();

        listItems = new LinkedHashMap<>();

        // 添加右侧边框分割线
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

        setLayout(new BorderLayout());

        // 创建顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 增加内边距

        titleLabel = new JLabel("消息"); // 或者 "群聊"/"好友"，根据当前视图动态变化更好，这里先用通用标题
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));

        // 调整“+”按钮样式，使其更像一个功能图标
        createGroupButton.setMargin(new Insets(2, 6, 2, 6));
        createGroupButton.setFocusPainted(false);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(createGroupButton, BorderLayout.EAST);

        // 创建群聊容器
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));

        // 创建滚动面板
        scrollPane = new JScrollPane(chatContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 移除默认边框

        // 自定义滚动条UI
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setPreferredSize(new Dimension(10, 0));

        // 组装界面
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(topPanel, BorderLayout.CENTER);
        // 添加底部分割线，同时为了更好的层次感，也可以考虑添加顶部分割线（如果需要与标题栏分隔）
        // 这里我们给上下都添加分割线，确保 headerContainer 与上面的 Window Title 和下面的 List 都有分隔线
        headerContainer
                .setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UIManager.getColor("Component.borderColor")));

        this.add(headerContainer, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 创建一个用于创建群聊的按钮组件
     * 按下这个组件后弹出一个创建群聊的对话框，填写完成后进行创建群聊的操作（调用DataManager的createGroupChat方法）
     * 如果失败，则放弃创建群聊。
     *
     * @return 群聊创建按钮组件
     */
    private JButton createGroupCreateBtn() {
        JButton groupCreateBtn = new JButton("+");
        return groupCreateBtn;
    }

    /**
     * 初始化所有组件
     * 读取DataManager的信息，而后群聊信息创建为GroupListItem，加入到类中对应的列表中
     * 并将其添加到chatContainer中
     * 同样，好友信息创建为FriendListItem，加入到类中对应的列表中
     * 并添加到friendContainer中
     * 最后使用exchangeToGroupChat将群聊列表加入到聊天项容器中
     * 使用createGroupCreateBtn创建群聊创建按钮，并添加到chatContainer中
     */
    public void init() {
        createGroupButton = createGroupCreateBtn();
        createGroupButton.addActionListener(e -> {
            showAddMenu(createGroupButton);
        });
    }

    /**
     * 显示添加菜单
     * 根据当前模式（群聊/好友/设置），显示不同的添加选项
     * 例如，在群聊模式下，显示创建群聊和加入群聊选项
     * 在好友模式下，显示添加好友选项
     * 在设置模式下，显示不同的设置选项
     */
    private void showAddMenu(Component invoker) {
        JPopupMenu popupMenu = new JPopupMenu();

        if (currentMode == Mode.GROUP) {
            JMenuItem createGroupItem = new JMenuItem("创建群聊");
            createGroupItem.addActionListener(e -> MainPage.get().showGroupCreateDialog());
            popupMenu.add(createGroupItem);

            JMenuItem joinGroupItem = new JMenuItem("加入群聊");
            joinGroupItem.addActionListener(e -> {
                MainPage.get().showJoinGroupDialog();
            });
            popupMenu.add(joinGroupItem);

        } else if (currentMode == Mode.FRIEND) {
            JMenuItem addFriendItem = new JMenuItem("添加好友");
            addFriendItem.addActionListener(e -> {
                MainPage.get().showAddFriendDialog();
            });
            popupMenu.add(addFriendItem);
        }

        popupMenu.show(invoker, 0, invoker.getHeight());
    }

    /**
     * 更新消息列表的UI
     * 根据传入的groupId，更新对应groupListItems的群聊列表项的UI
     * 这里需要先删除createGroupButton按钮，而后进行更新操作后再添加回来。
     * 如果没有对应的id，则创建新的GroupListItem，并添加到groupListItems中
     */
    public void updateGroupList(String groupId, String title, int unreadCount) {
        if (listItems.containsKey(groupId)) {
            listItems.get(groupId).updateUI(title, unreadCount);
        } else {
            GroupListItem item = new GroupListItem(groupId, title, 0);
            listItems.put(groupId, item);
        }

        // 只有在 MESSAGE 模式下才更新 UI 容器
        if (currentMode == Mode.MESSAGE) {
            // 简单处理：重新加载所有 item 保证顺序，或者只添加新的
            // 为了简单，如果它不在容器里，加进去
            GroupListItem item = listItems.get(groupId);
            boolean alreadyIn = false;
            for (Component c : chatContainer.getComponents()) {
                if (c == item) {
                    alreadyIn = true;
                    break;
                }
            }
            if (!alreadyIn) {
                chatContainer.add(item);
            }
            chatContainer.revalidate();
            chatContainer.repaint();
        }
    }

    /**
     * 更新消息列表（兼容群聊和私聊）
     */
    public void updateMessageList(String id, String name, String content, int unread) {
        updateGroupList(id, name, unread);
    }

    /**
     * 如果当前处于群聊模式，刷新群聊列表
     * 用于处理新加入群聊时的列表更新
     */
    public void refreshIfInGroupMode() {
        if (currentMode == Mode.GROUP) {
            exchangeToGroupList();
        }
    }

    /**
     * 删除指定的群聊列表
     * 如果当前模式是CHAT，且groupId存在于listItems中，
     * 则从chatContainer中移除对应的GroupListItem组件，
     * 并从listItems中删除该条目。
     * 最后调用chatContainer的revalidate和repaint方法更新UI。
     */
    public void removeGroupListItem(String groupId) {
        if (listItems.containsKey(groupId)) {
            GroupListItem item = listItems.get(groupId);
            listItems.remove(groupId);
            if (currentMode == Mode.MESSAGE || currentMode == Mode.GROUP) {
                chatContainer.remove(item);
                chatContainer.revalidate();
                chatContainer.repaint();
            }
        }
    }

    /**
     * 切换到设置列表模式
     * 将当前模式设置为SETTING，更新标题为"设置"，隐藏创建群聊按钮，清空聊天项容器。
     * 然后添加个人信息和关于软件的设置项按钮到聊天项容器中。
     * 最后调用revalidate和repaint方法更新UI。
     */
    public void exchangeToSettingList() {
        currentMode = Mode.SETTING;
        titleLabel.setText("设置");
        createGroupButton.setVisible(false);
        chatContainer.removeAll();

        // 添加设置项
        chatContainer.add(createSettingItem("个人信息", "info"));
        chatContainer.add(createSettingItem("关于软件", "about"));

        chatContainer.revalidate();
        chatContainer.repaint();

        MainPage.get().exchangeToBlankContent(); // 右侧清空或显示默认页
    }

    /**
     * 创建一个设置项按钮
     * 按钮的文本为text，类型为type。
     * 按钮的对齐方式为居中对齐，最大宽度为Integer.MAX_VALUE，高度为50。
     * 点击按钮时，调用MainPage的exchangeToSettings方法，传入type参数。
     *
     * @param text 按钮的文本
     * @param type 按钮的类型
     * @return 一个设置项按钮组件
     */
    private JButton createSettingItem(String text, String type) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.addActionListener(e -> MainPage.get().exchangeToSettings(type));
        return btn;
    }

    /**
     * 切换到消息列表模式
     * 将当前模式设置为MESSAGE，更新标题为"消息"，隐藏创建群聊按钮，清空聊天项容器。
     * 然后遍历listItems中的所有GroupListItem组件，添加到聊天项容器中。
     * 最后调用chatContainer的revalidate和repaint方法更新UI。
     */
    public void exchangeToMessageList() {
        currentMode = Mode.MESSAGE;
        titleLabel.setText("消息");
        createGroupButton.setVisible(false);
        chatContainer.removeAll();

        // 恢复消息列表
        for (GroupListItem item : listItems.values()) {
            chatContainer.add(item);
        }

        chatContainer.revalidate();
        chatContainer.repaint();
    }

    /**
     * 切换到群聊列表模式
     * 将当前模式设置为GROUP，更新标题为"群聊"，显示创建群聊按钮，清空聊天项容器。
     * 然后遍历LocalData中的所有群聊，创建GroupListItem组件并添加到聊天项容器中。
     * 最后调用chatContainer的revalidate和repaint方法更新UI。
     */
    public void exchangeToGroupList() {
        currentMode = Mode.GROUP;
        titleLabel.setText("群聊");
        createGroupButton.setVisible(true);
        chatContainer.removeAll();

        java.util.List<server.data.GroupData> groups = LocalData.get().getAllGroups();
        if (groups != null) {
            for (server.data.GroupData group : groups) {
                // 这里我们复用GroupListItem，未读数设为0
                GroupListItem item = new GroupListItem(group.getGroupId(), group.getGroupName(), 0);
                chatContainer.add(item);
            }
        }

        chatContainer.revalidate();
        chatContainer.repaint();
    }

    /**
     * 切换到好友列表模式
     * 将当前模式设置为FRIEND，更新标题为"好友"，隐藏创建群聊按钮，清空聊天项容器。
     * 然后遍历LocalData中的好友列表，创建FriendListItem组件并添加到聊天项容器中。
     * 最后调用chatContainer的revalidate和repaint方法更新UI。
     */
    public void exchangeToFriendList() {
        currentMode = Mode.FRIEND;
        titleLabel.setText("好友");
        createGroupButton.setVisible(true); // 显示加号按钮
        chatContainer.removeAll();

        Map<String, String> friends = LocalData.get().getFriends();
        if (friends != null) {
            for (Map.Entry<String, String> entry : friends.entrySet()) {
                FriendListItem item = new FriendListItem(entry.getKey(), entry.getValue());
                chatContainer.add(item);
            }
        }

        chatContainer.revalidate();
        chatContainer.repaint();

        MainPage.get().exchangeToBlankContent();
    }
}