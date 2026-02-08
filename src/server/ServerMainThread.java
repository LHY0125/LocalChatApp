package server;

import server.serveice.*;
import util.FileUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static global.global.SERVER_PORT;

public class ServerMainThread extends Thread {
    // 服务器运行状态
    private static volatile boolean running = true;
    // 线程池：用于处理聊天消息的线程池
    private static ExecutorService chatThreadPool;
    // 线程池：用于接收客户端消息的线程池
    private static ExecutorService receiveThreadPool;
    // 用于存储每个客户端的消息队列
    private static ConcurrentHashMap<Socket, ArrayBlockingQueue<Wrapper>> msgQueues;

    // 核心：启动服务、监听端口、循环接收客户端连接
    @Override
    public void run() {
        System.out.println("加载本地数据成功");
        // 初始化推送信息线程池
        chatThreadPool = new ThreadPoolExecutor(
                10, // 核心线程数
                50, // 最大线程数
                60L, TimeUnit.SECONDS, // 空闲线程存活时间
                new SynchronousQueue<>(), // 直接提交队列
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
        receiveThreadPool = new ThreadPoolExecutor(
                10, // 核心线程数
                50, // 最大线程数
                60L, TimeUnit.SECONDS, // 空闲线程存活时间
                new SynchronousQueue<>(), // 直接提交队列
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );

        // 初始化消息队列
        msgQueues = new ConcurrentHashMap<>();

        System.out.println("初始化信息线程池成功");
        System.out.println("服务器启动成功");
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            while (running) {

                // 扫描端口，接收链接请求，如果有链接请求，则尝试链接
                Socket clientSocket = serverSocket.accept();
                System.out.println("有新的用户端连接: " + clientSocket.getPort());

                // 创建线程，处理客户端请求
                ArrayBlockingQueue<Wrapper> threadQueue = new ArrayBlockingQueue<>(40);
                msgQueues.put(clientSocket, threadQueue);

                ClientChatThread clientChatThread = new ClientChatThread(clientSocket, threadQueue);
                chatThreadPool.submit(clientChatThread);
                Thread.sleep(200);
                ClientReceiveThread clientReceiveThread = new ClientReceiveThread(clientSocket, threadQueue);
                receiveThreadPool.submit(clientReceiveThread);
                System.out.println("创建线程成功");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 关闭服务器
    public void shutdown() {
        FileUtil.saveServerData();
        // 向所有用户发送服务器关闭信息。
        if (msgQueues != null) {
            Wrapper exitMsg = new Wrapper(global.global.OPT_EXIT);
            for (ArrayBlockingQueue<Wrapper> queue : msgQueues.values()) {
                try {
                    queue.put(exitMsg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        running = false;
    }

    // 检查服务器是否运行
    public static boolean isRunning() {
        return running;
    }

    // 检查chatThreadPool状态
    public static Map<String, Object> getChatThreadPoolStatus() {
        Map<String, Object> status = new HashMap<>();

        if (chatThreadPool == null) {
            status.put("error", "线程池未初始化");
            return status;
        }

        // 检查是否为 ThreadPoolExecutor
        if (chatThreadPool instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) chatThreadPool;

            status.put("poolSize", tpe.getPoolSize());
            status.put("activeCount", tpe.getActiveCount());
            status.put("corePoolSize", tpe.getCorePoolSize());
            status.put("maximumPoolSize", tpe.getMaximumPoolSize());
            status.put("largestPoolSize", tpe.getLargestPoolSize());
            status.put("queueSize", tpe.getQueue().size());
            status.put("completedTaskCount", tpe.getCompletedTaskCount());
            status.put("taskCount", tpe.getTaskCount());
            status.put("isShutdown", tpe.isShutdown());
            status.put("isTerminated", tpe.isTerminated());
        }
        // 检查是否为 ForkJoinPool
        else if (chatThreadPool instanceof ForkJoinPool) {
            ForkJoinPool fjp = (ForkJoinPool) chatThreadPool;

            status.put("poolSize", fjp.getPoolSize());
            status.put("activeCount", fjp.getActiveThreadCount());
            status.put("parallelism", fjp.getParallelism());
            status.put("runningThreadCount", fjp.getRunningThreadCount());
            status.put("queuedTaskCount", fjp.getQueuedTaskCount());
            status.put("queuedSubmissionCount", fjp.getQueuedSubmissionCount());
            status.put("stealCount", fjp.getStealCount());
        }
        // 其他类型的 ExecutorService
        else {
            // 使用反射尝试获取信息
            status.put("type", chatThreadPool.getClass().getName());
            status.put("info", "无法直接获取详细状态");
        }

        return status;
    }

    // 检查receiveThreadPool的状态
    public static Map<String, Object> getReceiveThreadPoolStatus() {
        Map<String, Object> status = new HashMap<>();

        if (receiveThreadPool == null) {
            status.put("error", "线程池未初始化");
            return status;
        }

        // 检查是否为 ThreadPoolExecutor
        if (receiveThreadPool instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) receiveThreadPool;

            status.put("poolSize", tpe.getPoolSize());
            status.put("activeCount", tpe.getActiveCount());
            status.put("corePoolSize", tpe.getCorePoolSize());
            status.put("maximumPoolSize", tpe.getMaximumPoolSize());
            status.put("largestPoolSize", tpe.getLargestPoolSize());
            status.put("queueSize", tpe.getQueue().size());
            status.put("completedTaskCount", tpe.getCompletedTaskCount());
            status.put("taskCount", tpe.getTaskCount());
            status.put("isShutdown", tpe.isShutdown());
            status.put("isTerminated", tpe.isTerminated());
        }
        // 检查是否为 ForkJoinPool
        else if (receiveThreadPool instanceof ForkJoinPool) {
            ForkJoinPool fjp = (ForkJoinPool) receiveThreadPool;

            status.put("poolSize", fjp.getPoolSize());
            status.put("activeCount", fjp.getActiveThreadCount());
            status.put("parallelism", fjp.getParallelism());
            status.put("runningThreadCount", fjp.getRunningThreadCount());
            status.put("queuedTaskCount", fjp.getQueuedTaskCount());
            status.put("queuedSubmissionCount", fjp.getQueuedSubmissionCount());
            status.put("stealCount", fjp.getStealCount());
        }
        // 其他类型的 ExecutorService
        else {
            // 使用反射尝试获取信息
            status.put("type", receiveThreadPool.getClass().getName());
            status.put("info", "无法直接获取详细状态");
        }

        return status;
    }

    // 检查阻塞队列状态
    public static List<String> getBlockingQueueStatus() {
        List<String> queueStatus = new ArrayList<>();

        if (msgQueues == null) {
            queueStatus.add("阻塞队列未初始化");
            return queueStatus;
        }

        msgQueues.forEach(
                (key, value) -> queueStatus.add(key + ": " + value.size()));

        return queueStatus;
    }

    /**
     * 用于删除不需要的阻塞队列
     * 这里只能ClientChatThread调用
     *
     * @param socket 对应的客户端的socket
     */
    public static void dropMsgQueue(Socket socket) {
        if (msgQueues == null) {
            System.out.println("意外：主线程为初始化的情况下调用了dropMsgQueue");
            return;
        }
        if (msgQueues.containsKey(socket)) {
            msgQueues.remove(socket);
        } else {
            System.out.println("意外：尝试删除不存在的阻塞队列");
        }
    }
}