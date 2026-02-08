package client.view;

import client.Client;
import client.service.ChatSender;
import client.service.LocalData;
import client.view.login.*;
import client.view.util.DesignToken;
import server.serveice.Wrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 登录页面，用于完成用户账户登录的功能，作为一个独立的页面，登录完成之后将自动关闭，并开启主界面
 */
public class LoginPage extends JFrame {
    private static volatile LoginPage INSTANCE;

    // 获取登录页面实例，使用单例模式确保全局唯一性
    public static LoginPage get() {
        if (INSTANCE == null) {
            synchronized (LoginPage.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LoginPage();
                }
            }
        }
        return INSTANCE;
    }

    // 交替展示两个窗口，分别用于进行注册和登录操作。
    private SignInView signInView;
    private SignUpView signUpView;

    private LoginPage() {
        setTitle("欢迎来到本地网聊天室！");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(DesignToken.LOGIN_WIDTH, DesignToken.LOGIN_HEIGHT);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 如果链接上了，发出退出信息
                if (Client.isConnected()) {
                    ChatSender.addMsg(Wrapper.logoutRequest(LocalData.get().getId()));
                }
                super.windowClosing(e);
                // 如果未登录，则直接退出。
                if (LocalData.get().getId().length() == 0) {
                    System.exit(0);
                }
            }
        });

        signInView = new SignInView(this);
        signUpView = new SignUpView(this);

        // 默认为登录
        exchangeToSignInView();
    }

    // 更换到注册界面
    public void exchangeToSignUpView() {
        this.remove(signInView);
        this.add(signUpView, BorderLayout.CENTER);
        this.validate();
        this.repaint();
    }

    // 更换到登录界面
    public void exchangeToSignInView() {
        this.remove(signUpView);
        this.add(signInView, BorderLayout.CENTER);
        this.validate();
        this.repaint();
    }

    // 打开主界面，并关闭本界面
    public void openMainPage() {
        // 创建新窗口
        MainPage.get().setVisible(true);

        // 关闭当前窗口
        this.dispose();
    }

    public void showMsgDialog(String text) {
        JDialog inviteDialog = new JDialog(this, "信息", true);
        inviteDialog.setSize(300, 200);
        inviteDialog.setLocationRelativeTo(this);

        // 设置对话框内容
        String htmlText = "<html><body style='width: 210px; padding: 10px;'>" + text + "</body></html>";
        JLabel label = new JLabel(htmlText, SwingConstants.CENTER);
        JButton closeBtn = new JButton("确定");

        closeBtn.addActionListener(e -> inviteDialog.dispose());

        JPanel panel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(label);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(closeBtn, BorderLayout.SOUTH);
        inviteDialog.add(panel);

        // 显示对话框（会阻塞主窗口交互）
        inviteDialog.setVisible(true);
    }
}


