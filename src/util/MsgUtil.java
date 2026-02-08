package util;

import java.io.Serializable;
import java.util.regex.Pattern;

import static global.global.DATA_SPLIT;

/**
 * 工具类，用于将聊过天记录拆分/整合
 */
public class MsgUtil implements Serializable {
    public static final int SENDERID_POS = 0;
    public static final int SENDER_NAME_POS = 1;
    public static final int TIME_POS = 2;
    public static final int CONTENT_POS = 3;

    private static final Pattern SPLIT_PATTERN = Pattern.compile(Pattern.quote(DATA_SPLIT));

    public static String combineMsg(String senderId, String senderName, String content) {
        return senderId + DATA_SPLIT + senderName + DATA_SPLIT + content;
    }

    public static String[] splitMsg(String msg) {
        if (msg == null) {
            return new String[0];
        }

        // 使用 -1 作为 limit 参数，保留空字符串
        return SPLIT_PATTERN.split(msg, -1);
    }

    public static String getSenderName(String msg) {
        return splitMsg(msg)[SENDER_NAME_POS];
    }

    public static String getSenderId(String msg) {
        return splitMsg(msg)[SENDERID_POS];
    }

    public static String getTime(String msg) {
        return splitMsg(msg)[TIME_POS];
    }

    public static String getContent(String msg) {
        return splitMsg(msg)[CONTENT_POS];
    }
}