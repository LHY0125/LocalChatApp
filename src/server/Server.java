package server;

import server.data.ServerData;
import util.FileUtil;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    public static void main(String[] args) {
        //启动服务器
        ServerMainThread serverThread = new ServerMainThread();
        serverThread.start();

        //启动定时任务：保存服务器数据
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // 设置为守护线程
            return t;
        });

        // 每隔半小时执行一次
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("保存数据中");
            FileUtil.saveServerData();
        }, 1200, 1200, TimeUnit.SECONDS);

        Scanner sc = new Scanner(System.in);
        while (true) {
            // System.out.print("SERVER_CMD>>");
            String cmd = sc.nextLine();
            switch (cmd) {
                case "shutdown":
                    serverThread.shutdown();
                    System.out.println("=======服务器已关闭=======");
                    System.exit(0);
                    break;
                case "groupInfo":
                    System.out.println("=======群聊列表=======");
                    ServerData.getInstance().getServerGroups().values().forEach(
                            group -> System.out.println(group.getGroupId() + " " + group.getGroupName())
                    );
                    break;
                case "chatThreadStatus":
                    System.out.println("=======聊天线程状态=======");
                    System.out.println(ServerMainThread.getChatThreadPoolStatus());
                    break;
                case "receiveThreadStatus":
                    System.out.println("=======接收线程状态=======");
                    System.out.println(ServerMainThread.getReceiveThreadPoolStatus());
                    break;
                case "blockingQueueStatus":
                    System.out.println("=======阻塞队列状态=======");
                    System.out.println(ServerMainThread.getBlockingQueueStatus());
                    break;
                default:
                    System.out.println("无效指令");
            }
        }
    }
}