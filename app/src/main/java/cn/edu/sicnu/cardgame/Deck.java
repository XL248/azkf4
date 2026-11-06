package cn.edu.sicnu.cardgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 一副牌（模型层）。
 * 实验五新增 {@link #drawMatchedCards(int, int)}：保证每 {@code groupSize} 张点数相同，
 * 一次发满 {@code count} 张时永远可以全部配对完成。
 */
public class Deck {

    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        for (String suit : Card.validSuits) {
            for (String rank : Card.rankStrings) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public int size() {
        return cards.size();
    }

    /** 随机抽一张（抽走即从牌堆移除） */
    public Card drawRandomCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove((int) (Math.random() * cards.size()));
    }

    /** 兼容旧调用：2 张同点数为一组 */
    public List<Card> drawMatchedCards(int count) {
        return drawMatchedCards(count, 2);
    }

    /**
     * 发出 {@code count} 张牌，每 {@code groupSize} 张点数相同、花色各不相同。
     * 例：count=24、groupSize=3 → 8 组 × 3 张。
     *
     * @throws IllegalArgumentException count 不是 groupSize 的整数倍，或 groupSize 超出点数上限
     */
    public List<Card> drawMatchedCards(int count, int groupSize) {
        if (groupSize < 2 || groupSize > Card.validSuits.length) {
            throw new IllegalArgumentException("groupSize 必须在 2.." + Card.validSuits.length + " 之间");
        }
        if (count <= 0 || count % groupSize != 0) {
            throw new IllegalArgumentException("count 必须是 groupSize 的整数倍");
        }
        int groups = count / groupSize;
        if (groups > Card.rankStrings.length) {
            throw new IllegalArgumentException("牌堆里没有 " + groups + " 个不同点数");
        }

        List<String> ranks = new ArrayList<>();
        Collections.addAll(ranks, Card.rankStrings);
        Collections.shuffle(ranks);

        List<String> suits = new ArrayList<>();
        Collections.addAll(suits, Card.validSuits);

        List<Card> picked = new ArrayList<>(count);
        for (int g = 0; g < groups; g++) {
            String rank = ranks.get(g);
            Collections.shuffle(suits);
            for (int i = 0; i < groupSize; i++) {
                picked.add(new Card(suits.get(i), rank));
            }
        }
        Collections.shuffle(picked);
        return picked;
    }
}
