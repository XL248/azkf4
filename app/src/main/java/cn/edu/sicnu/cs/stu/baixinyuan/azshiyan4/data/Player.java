package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.data;

/**
 * 玩家信息（姓名、性别、电话、最高分）。
 * 按实验要求：修改后的信息只保存在内存中，不写入文件。
 */
public class Player {

    private String name;
    private String gender;
    private String phone;
    private int highScore;

    public Player(String name, String gender, String phone, int highScore) {
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.highScore = highScore;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getHighScore() {
        return highScore;
    }

    /** 只有超过历史最高分才更新 */
    public boolean updateHighScore(int score) {
        if (score > highScore) {
            highScore = score;
            return true;
        }
        return false;
    }
}
