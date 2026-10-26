package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.data;

/**
 * 全局共享的玩家数据（内存单例）。
 * 三个 Activity 都从这里读写，避免在 Activity 之间反复传值；
 * 按实验要求不落盘，进程结束即恢复默认值。
 */
public final class PlayerStore {

    private static Player player;

    private PlayerStore() {
    }

    public static Player get() {
        return player;
    }

    /** 由主界面在第一次进入时用字符串资源里的默认值初始化 */
    public static void init(String defaultName, String defaultGender, String defaultPhone) {
        if (player == null) {
            player = new Player(defaultName, defaultGender, defaultPhone, 0);
        }
    }
}
