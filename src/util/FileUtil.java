package util;

import server.data.ServerData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/*
 *通用文件IO工具类
 *将聊天记录等信息写入本地文件
 */
public class FileUtil {
    // 数据文件夹
    public static final String DATA_FILE = "data";

    // 子文件夹
    public static final String GROUPS_DIR = "groups";
    public static final String FRIENDS_DIR = "friends";

    // 服务器数据文件名
    public static final String SERVER_DATA_FILENAME = "server_data.data";

    // 聊天历史消息存储文件夹名
    public static final String CHAT_DATA_DIRNAME = "chat_data";

    // 辅助方法：获取Group ServerData路径
    private static Path getGroupServerDataPath() {
        return Paths.get(DATA_FILE, GROUPS_DIR, SERVER_DATA_FILENAME);
    }

    // 辅助方法：获取Friend ServerData路径
    private static Path getFriendServerDataPath() {
        return Paths.get(DATA_FILE, FRIENDS_DIR, SERVER_DATA_FILENAME);
    }

    // 辅助方法：获取Chat Data路径
    private static Path getChatDataPath(String id, boolean isGroup) {
        String subDir = isGroup ? GROUPS_DIR : FRIENDS_DIR;
        return Paths.get(DATA_FILE, subDir, CHAT_DATA_DIRNAME, id + ".txt");
    }

    /**
     * 读取文件中的serverData信息，返回ServerData对象
     * 从groups和friends文件夹分别读取并合并
     */
    public static ServerData loadServerData() {
        ServerData finalData = new ServerData();
        boolean loadedAny = false;

        // 1. Load Group Data
        ServerData groupData = loadDataFromFile(getGroupServerDataPath());
        if (groupData != null) {
            if (groupData.getServerGroups() != null) {
                finalData.setServerGroups(groupData.getServerGroups());
            }
            loadedAny = true;
        }

        // 2. 加载好友数据
        ServerData friendData = loadDataFromFile(getFriendServerDataPath());
        if (friendData != null) {
            if (friendData.getServerUsers() != null) {
                finalData.setServerUsers(friendData.getServerUsers());
            }
            loadedAny = true;
        }

        return loadedAny ? finalData : null;
    }

    private static ServerData loadDataFromFile(Path path) {
        if (!Files.exists(path) || path.toFile().length() == 0)
            return null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(path.toFile())))) {
            return (ServerData) ois.readObject();
        } catch (Exception e) {
            System.err.println("加载数据失败 (" + path + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * 使用静态方法获取serverData对象，将serverData中的信息写入文件中
     */
    public static void saveServerData() {
        ServerData data = ServerData.getInstance();

        // Save Groups
        ServerData groupData = new ServerData();
        groupData.setServerGroups(data.getServerGroups());
        // 清空 Users，避免重复存储到groups文件
        groupData.setServerUsers(null);
        saveDataToFile(getGroupServerDataPath(), groupData);

        // Save Friends
        ServerData friendData = new ServerData();
        friendData.setServerUsers(data.getServerUsers());
        // 清空 Groups，避免重复存储到friends文件
        friendData.setServerGroups(null);
        saveDataToFile(getFriendServerDataPath(), friendData);

        System.out.println("服务器数据保存成功");
    }

    private static void saveDataToFile(Path path, ServerData data) {
        File file = path.toFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        synchronized (ServerData.class) {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)))) {
                oos.writeObject(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查数据文件是否存在 (任意一个存在即认为存在)
     */
    public static boolean isDataFileExists() {
        return Files.exists(getGroupServerDataPath()) || Files.exists(getFriendServerDataPath());
    }

    /**
     * 获取数据文件大小（字节）
     */
    public static long getDataFileSize() {
        long size = 0;
        File gFile = getGroupServerDataPath().toFile();
        if (gFile.exists())
            size += gFile.length();
        File fFile = getFriendServerDataPath().toFile();
        if (fFile.exists())
            size += fFile.length();
        return size;
    }

    // ===================================聊天信息=======================

    /**
     * 将聊天信息追加写入文件
     *
     * @param groupId    群聊id 或 用户id
     * @param senderId   发送者id
     * @param senderName 发送者名字
     * @param content    发送内容
     */
    public static void addChatMessage(String groupId, String senderId, String senderName, String content) {
        String msg = MsgUtil.combineMsg(senderId, senderName, content);
        writeChatMsg(groupId, msg);
    }

    public static void addChatMessage(String groupId, String contentFromWrapper) {
        writeChatMsg(groupId, contentFromWrapper);
    }

    private static void writeChatMsg(String id, String content) {
        // 判断是群聊还是私聊
        // 通过 ServerGroups 来判断，如果在群组Map中，则是群聊，否则认为是私聊
        boolean isGroup = false;
        if (ServerData.getInstance().getServerGroups() != null) {
            isGroup = ServerData.getInstance().getServerGroups().containsKey(id);
        }

        Path path = getChatDataPath(id, isGroup);
        File file = path.toFile();

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw)) {
            // 写入文本并换行
            bw.write(content);
            bw.newLine(); // 跨平台换行
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取某个群的所有聊天记录
     *
     * @return 历史消息列表，无内容/文件不存在返回空List，绝不返回null
     */
    public static List<String> loadGroupChatMsg(String groupId) {
        // 判断是群聊还是私聊
        boolean isGroup = false;
        if (ServerData.getInstance().getServerGroups() != null) {
            isGroup = ServerData.getInstance().getServerGroups().containsKey(groupId);
        }

        Path path = getChatDataPath(groupId, isGroup);

        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (NoSuchFileException e) {
            lines = new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (lines == null) {
            lines = new ArrayList<>();
        }
        return lines;
    }
}