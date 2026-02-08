package client.view.login;


import client.Client;
import client.service.ChatSender;
import client.service.LocalData;
import client.view.LoginPage;
import client.view.util.DesignToken;
import server.serveice.Wrapper;

import javax.swing.*;

// 登录界面
public class SignInView extends JPanel {
    private LoginPage loginPage;

    JTextField account;
    JTextField password;

    /**
     * 生成一个登录界面
     * 按照原型图中设计,创建两个文本输入框，用于让用户输入id和密码。
     * 创建一个按钮用于登录，为按钮添加SignIncheck的按下事件
     * 创建一个注册选项按钮，按下后该界面更换为注册界面。
     */
    public SignInView(LoginPage loginPage) {
        this.loginPage = loginPage;

        //面板
        this.setLayout(null);
        this.setSize(DesignToken.LOGIN_WIDTH, DesignToken.LOGIN_HEIGHT);
        this.setVisible(true);

        //文本展示
        JLabel ja1 = new JLabel("登录");
        ja1.setBounds(165, 10, 80, 80);
        this.add(ja1);


        JLabel ja2 = new JLabel("账户:");
        ja2.setBounds(50, 80, 100, 30);
        this.add(ja2);


        JLabel ja3 = new JLabel("密码:");
        ja3.setBounds(50, 120, 100, 30);
        this.add(ja3);


        //按钮设计
        JButton jb1 = new JButton("注册");
        jb1.setBounds(230, 190, 59, 20);
        jb1.setBorderPainted(false);
        jb1.addActionListener(e -> {
            this.loginPage.exchangeToSignUpView();
        });
        this.add(jb1);

//        JButton jb2=new JButton("忘记密码");
//        jb2.setBounds(290,190,90,20);
//        jb2.setBorderPainted(false);
//        jb2.addActionListener(e -> {
//
//        });
//        add(jb2);


        JButton jb3 = new JButton("登录");
        jb3.setBounds(150, 160, 59, 50);
        jb3.addActionListener(e -> {
            signIncheck();
        });
        this.add(jb3);

        //建立文本域
        account = new JTextField(DesignToken.MAX_FONT_SIZE);
        account.setBounds(80, 80, 220, 30);
        this.add(account);

        password = new JPasswordField(DesignToken.MAX_FONT_SIZE);
        password.setBounds(80, 120, 220, 30);
        this.add(password);

    }

    /**
     * 获取当前页面的用户id和密码
     * 如果当前服务器未在线，则直接向用户说明
     * 否则，发出登录请求
     */
    private void signIncheck() {
        if (!Client.isConnected()) {
            loginPage.showMsgDialog("当前服务器未在线，请稍后再试");
            return;
        }

        LocalData.get().setId(account.getText());
        ChatSender.addMsg(
                Wrapper.loginRequest(LocalData.get().getId(), password.getText())
        );
    }
}
