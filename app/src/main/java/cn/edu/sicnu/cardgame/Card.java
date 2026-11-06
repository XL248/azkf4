package cn.edu.sicnu.cardgame;

/**
 * 一张扑克牌（模型层：不依赖任何 Android 组件）。
 * 由实验附件 Card.kt 等价改写为 Java，实验五改为「3 张牌」配对规则。
 */
public class Card {

    /** 一局配对所需的牌数：实验五为 3 张同点数 */
    public static final int MATCH_GROUP_SIZE = 3;

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
     * 【实验五】3 张牌的配对判定：{@code otherCards} 必须正好是另外 2 张，
     * 且三张牌的点数全部相同才算配对成功，返回 4 分；否则返回 0 分。
     * 花色不参与判定（♥A ♦A ♠A 算一组）。
     */
    public int match(Card[] otherCards) {
        if (otherCards == null || otherCards.length != MATCH_GROUP_SIZE - 1) {
            return 0;
        }
        for (Card otherCard : otherCards) {
            if (otherCard == null || !otherCard.rank.equals(rank)) {
                return 0;
            }
        }
        return 4;
    }

    /** 收回这张牌（配对成功后保留翻开状态，不调用本方法） */
    public void turnDown() {
        isChosen = false;
    }

    @Override
    public String toString() {
        return suit + rank;
    }
}
