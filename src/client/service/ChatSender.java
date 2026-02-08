package client.service;

import client.view.MainPage;
import server.serveice.Wrapper;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class ChatSender extends Thread {

    private static BlockingQueue<Wrapper> messageQueue;

    public static void addMsg(Wrapper msg) {
        messageQueue.add(msg);
    }

    private ObjectOutputStream oos;
    private final Socket clientSocket;

    private static boolean isRunning;

    public static void stopRunning() {
        isRunning = false;
    }

    public ChatSender(Socket clientSocket, BlockingQueue<Wrapper> messageQueue) {
        ChatSender.messageQueue = messageQueue;
        this.clientSocket = clientSocket;
        isRunning = true;
    }

    @Override
    public void run() {

        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Wrapper msg = null;
        while (isRunning) {
            try {
                msg = messageQueue.take();
                oos.writeObject(msg);
                oos.flush();
                System.out.println("信息已发出：" + msg.getOperation());
            } catch (InterruptedException e) {
                System.out.println(LocalData.get().getId() + ": 消息队列被中断");
                break;
            } catch (IOException e) {
                if (isConnectionClosed(e)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(MainPage.get(), "服务器连接已断开，请重新登录。", "连接断开",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    });
                    System.out.println("服务器连接已断开");
                    break;
                } else {
                    System.out.println("发送消息时发生IO异常: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isConnectionClosed(IOException e) {
        // 根据异常类型判断连接是否断开
        return e instanceof SocketException ||
                e.getMessage() != null && (e.getMessage().contains("Connection reset") ||
                        e.getMessage().contains("Broken pipe") ||
                        e.getMessage().contains("Connection refused") ||
                        e.getMessage().contains("Software caused connection abort"));
    }
}