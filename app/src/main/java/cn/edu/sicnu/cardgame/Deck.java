package cn.edu.sicnu.cardgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 一副 52 张的牌堆，随机抽牌（模型层）。
 * 由实验附件 Deck.kt 等价改写为 Java。
 */
public class Deck {

    private final List<Card> cards = new ArrayList<>();
    private final Random random;

    public Deck() {
        this(new Random());
    }

    /** 允许测试注入固定种子的随机数，保证结果可复现 */
    public Deck(Random random) {
        this.random = random;
        for (String suit : Card.validSuits) {
            for (String rank : Card.rankStrings) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    /** 剩余的牌数 */
    public int size() {
        return cards.size();
    }

    /**
     * 按「成对」规则发牌：返回 count 张牌，保证每个出现的点数都是 2 张，
     * 因此整局一定能把 16 张全部配对完（不会出现落单的牌）。
     */
    public List<Card> drawMatchedCards(int count) {
        List<Card> picked = new ArrayList<>();
        int pairs = Math.max(1, count / 2);
        List<String> ranks = new ArrayList<>(Arrays.asList(Card.rankStrings));
        Collections.shuffle(ranks, random);
        List<String> suits = new ArrayList<>(Arrays.asList(Card.validSuits));
        for (int i = 0; i < pairs && i < ranks.size(); i++) {
            Collections.shuffle(suits, random);
            picked.add(new Card(suits.get(0), ranks.get(i)));
            picked.add(new Card(suits.get(1), ranks.get(i)));
        }
        Collections.shuffle(picked, random);
        return picked;
    }

    /** 随机抽一张牌，牌堆空了返回 null */
    public Card drawRandomCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(random.nextInt(cards.size()));
    }
}
