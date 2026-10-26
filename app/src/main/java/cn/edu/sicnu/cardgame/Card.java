package cn.edu.sicnu.cardgame;

/**
 * 一张扑克牌（模型层：不依赖任何 Android 组件）。
 * 由实验附件 Card.kt 等价改写为 Java。
 */
public class Card {

    /** 点数：A 2 3 ... K */
    public static final String[] rankStrings =
            {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

    /** 花色：红桃 方块 黑桃 梅花 */
    public static final String[] validSuits = {"\u2665", "\u2666", "\u2660", "\u2663"};

    private final String suit;
    private final String rank;

    /** 是否被选中（翻开） */
    public boolean isChosen;
    /** 是否已配对成功 */
    public boolean isMatched;

    public Card(String suit, String rank) {
        this(suit, rank, false, false);
    }

    public Card(String suit, String rank, boolean isChosen, boolean isMatched) {
        this.suit = suit;
        this.rank = rank;
        this.isChosen = isChosen;
        this.isMatched = isMatched;
    }

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    /** 是否红色花色（红桃、方块），用于界面着色 */
    public boolean isRed() {
        return validSuits[0].equals(suit) || validSuits[1].equals(suit);
    }

    /**
     * 与其它牌比较，返回得分：**点数相同**才算配对，得 4 分；否则 0 分。
     * （花色不同不影响配对结果，只有点数才是配对依据）
     */
    public int match(Card[] otherCards) {
        int score = 0;
        if (otherCards != null && otherCards.length == 1) {
            Card otherCard = otherCards[0];
            if (otherCard.rank.equals(rank)) {
                score = 4;
            }
        }
        return score;
    }

    @Override
    public String toString() {
        return suit + rank;
    }
}
