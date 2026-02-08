package client.view.main;

import client.service.ChatSender;
import client.service.LocalData;
import client.view.MainPage;
import client.view.util.DesignToken;
import server.data.UserData;
import server.serveice.Wrapper;

import javax.swing.*;
import java.awt.*;

/**
 * 设置界面组件
 * 用于显示用户个人信息和关于 LocalChatApp 的设置选项。
 * 包括用户ID、用户名、退出登录等功能。
 */
public class SettingsView extends JPanel {

    public SettingsView(String type) {
        initUI(type);
    }

    /**
     * 初始化设置界面组件
     * 用于根据界面类型显示不同的设置内容。
     * 如果是 "info" 类型，显示用户个人信息，包括用户ID和用户名。
     * 如果是 "about" 类型，显示关于 LocalChatApp 的信息，包括版本号等。
     * @param type 界面类型，"info" 显示个人信息，"about" 显示关于 LocalChatApp 的信息
     */
    private void initUI(String type) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        contentPanel.setBackground(Color.WHITE);

        if ("info".equals(type)) {
            addInfoContent(contentPanel);
        } else if ("about".equals(type)) {
            addAboutContent(contentPanel);
        }

        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * 添加个人信息内容到设置界面
     * 包括用户ID、用户名、邮箱、生日、地址、签名等。
     * @param panel 用于添加组件的面板
     */
    private void addInfoContent(JPanel panel) {
        JLabel title = new JLabel("个人信息");
        title.setFont(new Font(DesignToken.DEFAULT_FONT, Font.BOLD, 24));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        String myId = LocalData.get().getId();
        UserData myData = LocalData.get().getUserDetail(myId);
        if (myData == null) {
            // 如果数据尚未同步，使用本地基本信息创建一个临时对象
            myData = new UserData(LocalData.get().getUserName(myId), myId, null);
        }

        addLabel(panel, "用户ID:", myId);
        addLabel(panel, "用户名:", myData.getNickname());

        // 可编辑字段
        JTextField emailField = addEditableField(panel, "邮箱:", myData.getEmail());
        JTextField birthdayField = addEditableField(panel, "生日:", myData.getBirthday());
        JTextField addressField = addEditableField(panel, "地址:", myData.getAddress());
        JTextField signatureField = addEditableField(panel, "个性签名:", myData.getSignature());

        panel.add(Box.createVerticalStrut(30));

        // 保存修改按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 保存修改按钮
        JButton saveBtn = new JButton("保存修改");
        final UserData currentData = myData;
        saveBtn.addActionListener(e -> {
            // 更新本地对象字段
            currentData.setEmail(emailField.getText().trim());
            currentData.setBirthday(birthdayField.getText().trim());
            currentData.setAddress(addressField.getText().trim());
            currentData.setSignature(signatureField.getText().trim());

            // 发送更新请求
            ChatSender.addMsg(Wrapper.updateUserDetailRequest(myId, currentData));

            // 更新本地缓存（虽然服务器会广播回来，但本地先更新体验更好）
            LocalData.get().updateUserDetails(currentData);

            JOptionPane.showMessageDialog(this, "个人信息已保存", "提示", JOptionPane.INFORMATION_MESSAGE);
        });

        // 退出登录按钮
        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "确定要退出登录吗？", "提示", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                MainPage.get().openLogInPage();
            }
        });

        // 添加按钮到面板
        btnPanel.add(saveBtn);
        btnPanel.add(logoutBtn);
        panel.add(btnPanel);
    }
    
    /**
     * 添加可编辑文本字段到设置界面
     * 用于用户输入个人信息的编辑。
     * @param panel 用于添加组件的面板
     * @param labelText 标签文本，描述字段的作用
     * @param value 初始文本字段值
     * @return 新创建的 JTextField 对象
     */
    private JTextField addEditableField(JPanel panel, String labelText, String value) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.setBackground(Color.WHITE);
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(DesignToken.DEFAULT_FONT, Font.PLAIN, 16));
        label.setPreferredSize(new Dimension(80, 30));
        
        JTextField textField = new JTextField(value, 20);
        textField.setFont(new Font(DesignToken.DEFAULT_FONT, Font.PLAIN, 14));
        
        fieldPanel.add(label);
        fieldPanel.add(textField);
        panel.add(fieldPanel);
        return textField;
    }
    
    /**
     * 添加关于 LocalChatApp 的内容到设置界面
     * 包括版本号、开发团队、应用描述等。
     * @param panel 用于添加组件的面板
     */
    private void addAboutContent(JPanel panel) {
        JLabel title = new JLabel("关于 LocalChatApp");
        title.setFont(new Font(DesignToken.DEFAULT_FONT, Font.BOLD, 24));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));

        JLabel version = new JLabel("版本: v1.0.0");
        version.setFont(new Font(DesignToken.DEFAULT_FONT, Font.PLAIN, 16));
        panel.add(version);
        
        panel.add(Box.createVerticalStrut(10));
        JLabel author = new JLabel("开发团队: 添砖加瓦小组");
        author.setFont(new Font(DesignToken.DEFAULT_FONT, Font.PLAIN, 16));
        panel.add(author);

        panel.add(Box.createVerticalStrut(10));
        JLabel desc = new JLabel("<html><body><p style='width:300px'>基于Java Swing和Socket开发的本地局域网聊天室。</p></body></html>");
        desc.setFont(new Font(DesignToken.DEFAULT_FONT, Font.PLAIN, 14));
        panel.add(desc);
    }
    
    /**
     * 添加标签到设置界面
     * 用于显示键值对信息，例如用户ID和用户名。
     * @param panel 用于添加组件的面板
     * @param key 键，例如 "用户ID:"
     * @param value 值，例如 "10086"
     */
    private void addLabel(JPanel panel, String key, String value) {
        // 创建一个行面板，用于添加键值对标签
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 键标签
        JLabel k = new JLabel(key);
        k.setFont(new Font(DesignToken.DEFAULT_FONT, Font.BOLD, 14));
        k.setPreferredSize(new Dimension(80, 30));

        // 值标签
        JLabel v = new JLabel(value);
        v.setFont(new Font(DesignToken.DEFAULT_FONT, Font.PLAIN, 14));
        
        // 添加键值对标签到行面板
        row.add(k);
        row.add(v);
        panel.add(row);
    }
}
