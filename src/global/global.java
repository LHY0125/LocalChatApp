package global;

/**
 * 全局配置类
 * 全局变量均在此处，不要随意更改！！！局部变量命名不要有冲突
 */
public class global {
    // ====================== 1. 网络与基础配置 ======================
    public static final int SERVER_PORT = 1145; // 服务端监听端口，两端统一
    public static final String LOCAL_HOST = "127.0.0.1"; // 本地IP，客户端连接服务端用
    public static final String SERVER_ACCOUNT = "admin"; // 服务端账户名
    public static final int AUTO_SAVE_PERIOD = 1200; // 自动保存周期（单位：秒）
    public static final String DATA_SPLIT = "\uE000"; // 数据分隔符

    // ====================== 2. 客户端请求操作码 (OPT) ======================
    // 注：常量值需保持唯一，避免冲突

    // ------ 账户注册与登录 (1-4) ------
    public static final int OPT_REGISTER = 1; // 注册
    public static final int OPT_REGISTER_SUCCESS = 11; // 注册成功
    public static final int OPT_REGISTER_FAILED_ACC = 12; // 注册失败：账号存在
    public static final int OPT_REGISTER_FAILED_FORMAT = 13;// 注册失败：格式错误

    public static final int OPT_LOGIN = 2; // 登录
    public static final int OPT_LOGIN_SUCCESS = 25; // 登录成功
    public static final int OPT_ERROR_NOT_LOGIN = 21; // 错误：还未登录
    public static final int OPT_LOGIN_FAILED_PWD = 22; // 登录失败：密码错误
    public static final int OPT_LOGIN_FAILED_ACC = 23; // 登录失败：账号错误
    public static final int OPT_LOGIN_FAILED_REPEATED = 24; // 登录失败：重复登录

    public static final int OPT_LOGOUT = 3; // 登出
    public static final int OPT_DELETE_ACCOUNT = 4; // 注销/删除账号

    // ------ 用户信息维护 (41-49) ------
    public static final int OPT_UPDATE_NICKNAME = 41; // 更新昵称
    public static final int OPT_UPDATE_PASSWORD = 42; // 更新密码
    public static final int OPT_USER_UPDATE_NAME_FAILED = 411; // 更新昵称失败
    public static final int OPT_USER_UPDATE_PASSWORD_FAILED = 412; // 更新密码失败
    public static final int OPT_USER_UPDATE_NAME_FAILED_WRONG_FORMAT = 413; // 更新昵称失败：格式错误

    // ------ 群组功能 (5-7) ------
    public static final int OPT_GROUP_CREATE = 5; // 创建群聊
    public static final int OPT_GROUP_CREATE_SUCCESS = 51; // 创建群聊成功

    public static final int OPT_GROUP_INVITE = 6; // 邀请加入群聊
    public static final int OPT_GROUP_INVITE_AGREE = 61; // 同意加入群聊
    public static final int OPT_GROUP_INVITE_REFUSE = 62; // 拒绝加入群聊
    public static final int OPT_GROUP_INVITE_OFFLINE = 63; // 邀请加入群聊失败：用户离线

    public static final int OPT_GROUP_JOIN = 64; // 申请加入群聊
    public static final int OPT_GROUP_JOIN_SUCCESS = 65; // 申请加入群聊成功
    public static final int OPT_GROUP_JOIN_FAILED = 66; // 申请加入群聊失败

    public static final int OPT_GROUP_QUIT = 7; // 退出群聊
    public static final int OPT_GROUP_DISBAND = 71; // 解散群聊
    public static final int OPT_GROUP_UPDATE_NAME = 72; // 更新群聊名字
    public static final int OPT_GROUP_UPDATE_OWNER = 73; // 更新群聊拥有者

    // ------ 好友功能 (67-69) ------
    public static final int OPT_FRIEND_ADD = 67; // 申请添加好友
    public static final int OPT_FRIEND_ADD_SUCCESS = 68; // 申请添加好友成功
    public static final int OPT_FRIEND_ADD_FAILED = 69; // 申请添加好友失败
    public static final int OPT_FRIEND_ADD_AGREE = 681; // 同意添加好友
    public static final int OPT_FRIEND_ADD_REFUSE = 682; // 拒绝添加好友

    // ------ 聊天消息 (8) ------
    public static final int OPT_CHAT = 8; // 群聊消息
    public static final int OPT_PRIVATE_CHAT = 81; // 私聊消息

    // ------ 数据初始化与同步 (9) ------
    public static final int OPT_INIT_CHAT = 9; // 初始化：群聊历史消息
    public static final int OPT_INIT_USER = 91; // 初始化：在线用户列表
    public static final int OPT_INIT_GROUP = 92; // 初始化：群组列表
    public static final int SERVER_MESSAGE = 93; // 服务器系统消息

    // 扩展用户信息 (v2)
    public static final int OPT_INIT_USER_DETAIL = 94; // 初始化：用户详细信息
    public static final int OPT_UPDATE_USER_DETAIL = 95; // 更新：用户详细信息

    // ------ 系统控制 ------
    public static final int OPT_EXIT = 999; // 服务器关闭通知
    public static final int OPT_QUEST_WRONG = 404; // 请求错误

    // ====================== 3. 响应提示信息 (MSG) ======================
    // 配合结果码，客户端直接展示给用户
    public static final String MSG_SUCCESS = "操作成功";
    public static final String MSG_ACCOUNT_EXIST = "注册失败：该账户已存在（账户唯一）";
    public static final String MSG_ACCOUNT_NOT_EXIST = "登录失败：该账户不存在";
    public static final String MSG_PWD_ERROR = "登录失败：密码与账户不匹配";
    public static final String MSG_UNKNOWN_OPT = "请求失败：未知的操作类型";
    public static final String MSG_DATA_ERROR = "请求失败：数据格式错误";

    // ====================== 4. 聊天应用配置 ======================
    public static final int MAX_MSG_SEND_GAP = 10; // 消息最大发送事件间隔/秒
    public static final int DISCONNECT_TIMEOUT = 30; // 断开连接超时时间/秒

    public static final String DEFAULT_GROUP_ID = "group_default"; // 默认群ID
    public static final String CHAT_MSG_PREFIX = "【系统消息】"; // 系统消息前缀

    // ====================== 5. 运行时状态标识 ======================
    public final static boolean IS_ONLINE = true;
    public final static boolean IS_OFFLINE = false;
}