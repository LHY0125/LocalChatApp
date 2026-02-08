package client.view.login;

import client.Client;
import client.service.ChatSender;
import client.service.LocalData;
import client.view.LoginPage;
import client.view.util.DesignToken;
import server.serveice.Wrapper;

import javax.swing.*;

public class SignUpView extends JPanel {
    private LoginPage loginPage;

    private JTextField account;
    private JTextField password;
    private JTextField name;

    /**
     * 生成一个注册界面
     * 按照原型图中设计,创建三个文本输入框，用于让用户输入id，密码，用户名
     * 创建一个按钮用于注册
     * 创建一个登录选项按钮，按下后该界面更换为登录界面。
     */
    public SignUpView(LoginPage loginPage) {
        this.loginPage = loginPage;

        //功能面板的建立
        this.setLayout(null);
        this.setSize(DesignToken.LOGIN_WIDTH, DesignToken.LOGIN_HEIGHT);
        this.setVisible(true);

        //文本
        JLabel ja1 = new JLabel("注册");
        ja1.setBounds(165, 10, 80, 80);
        this.add(ja1);

        JLabel ja2 = new JLabel("账户:");
        ja2.setBounds(50, 80, 100, 30);
        this.add(ja2);

        JLabel ja3 = new JLabel("密码:");
        ja3.setBounds(50, 120, 100, 30);
        this.add(ja3);

        JLabel ja4 = new JLabel("名字:");
        ja4.setBounds(50, 160, 100, 30);
        this.add(ja4);


        //监听按钮
        JButton jb1 = new JButton("注册");
        jb1.setBounds(150, 200, 59, 50);
        this.add(jb1);
        jb1.addActionListener(e -> {
            this.SignUpcheck();
        });

        JButton jb2 = new JButton("已有账号?去登录");
        jb2.setBounds(220, 230, 150, 20);
        jb2.setBorderPainted(false);
        jb2.addActionListener(e -> {
            this.loginPage.exchangeToSignInView();
        });
        this.add(jb2);

        //文本域
        account = new JTextField(DesignToken.MAX_FONT_SIZE);
        account.setBounds(80, 80, 220, 30);
        this.add(account);

        password = new JTextField(DesignToken.MAX_FONT_SIZE);
        password.setBounds(80, 120, 220, 30);
        this.add(password);

        name = new JTextField(DesignToken.MAX_FONT_SIZE);
        name.setBounds(80, 160, 220, 30);
        this.add(name);
    }

    /**
     * 获取当前界面的文本的内容
     * 检查服务器是否链接
     * 检查注册的id，name，password是否合法
     * 都合适的话，发送注册请求。
     */
    private void SignUpcheck() {
        if (!Client.isConnected()) {
            loginPage.showMsgDialog("当前服务器未在线，请稍后再试");
            return;
        }

        String userId = account.getText();
        String userName = name.getText();
        String userPassword = password.getText();

        if (userId.length() < 6 || userId.length() > 10 || !userId.matches("[a-zA-Z0-9_]+")) {
            loginPage.showMsgDialog("用户id只能包含字母大小写和数字下划线，且长度不得小于6，大于10");
            return;
        }

        if (userName.contains(" ")) {
            loginPage.showMsgDialog("用户名不能包含空格");
            return;
        }

        if (userPassword.contains(" ")) {
            loginPage.showMsgDialog("密码不能包含空格");
            return;
        }

        LocalData.get().setId(userId);
        LocalData.get().setUserName(userId, userName);

        ChatSender.addMsg(Wrapper.registerRequest(
                LocalData.get().getId(),
                password.getText(),
                name.getText()));
    }
}
