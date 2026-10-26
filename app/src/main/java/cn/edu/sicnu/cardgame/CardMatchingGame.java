package cn.edu.sicnu.cardgame;

import java.util.ArrayList;
import java.util.List;

/**
 * 翻牌配对游戏的模型（Model）：
 * 只包含游戏规则与状态，不涉及任何界面代码，便于单元测试。
 * 由实验附件 CardMatchingGame.kt 等价改写为 Java。
 */
public class CardMatchingGame {

    /** 配对成功奖励系数 */
    public static final int MATCH_BONUS = 4;
    /** 配对失败扣分 */
    public static final int MISMATCH_PENALTY = 2;
    /** 每翻开一张牌的代价 */
    public static final int COST_TO_CHOOSE = 1;

    private int score = 0;
    private final List<Card> cards;

    /** 发一局牌：count 张，按点数成对发放，保证整局可以 100% 配对完 */
    public CardMatchingGame(int count) {
        cards = new ArrayList<>(new Deck().drawMatchedCards(count));
    }

    /** 供单元测试使用：直接指定这一局的牌 */
    public CardMatchingGame(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public int getScore() {
        return score;
    }

    public int getCardCount() {
        return cards.size();
    }

    public Card cardAtIndex(int index) {
        return cards.get(index);
    }

    /** 已翻开的牌数（用于界面显示） */
    public int getChosenCount() {
        int count = 0;
        for (Card card : cards) {
            if (card.isChosen) {
                count++;
            }
        }
        return count;
    }

    /** 已翻开但还没配对的牌数（不匹配判定的依据，已配对的牌不算） */
    public int getChosenUnmatchedCount() {
        int count = 0;
        for (Card card : cards) {
            if (card.isChosen && !card.isMatched) {
                count++;
            }
        }
        return count;
    }

    /** 已配对成功的牌数 */
    public int getMatchedCount() {
        int count = 0;
        for (Card card : cards) {
            if (card.isMatched) {
                count++;
            }
        }
        return count;
    }

    /** 是否所有牌都已配对 */
    public boolean isFinished() {
        return getMatchedCount() == cards.size();
    }

    /**
     * 翻开 / 收起指定下标的牌，并按规则更新分数：
     * 再次点击已翻开的牌会把它收回；与另一张已翻开的牌比较，
     * 点数相同则两张都标记为已配对并加分，否则扣分并**把两张牌都收回**。
     */
    public void chooseCardAtIndex(int index) {
        if (index < 0 || index >= cards.size()) {
            return;
        }
        Card card = cardAtIndex(index);
        if (card.isMatched) {
            return;
        }
        if (card.isChosen) {
            card.isChosen = false;
            return;
        }
        for (Card otherCard : cards) {
            if (otherCard != card && otherCard.isChosen && !otherCard.isMatched) {
                int matchScore = card.match(new Card[]{otherCard});
                if (matchScore > 0) {
                    score += matchScore * MATCH_BONUS;
                    otherCard.isMatched = true;
                    card.isMatched = true;
                    card.isChosen = true;
                } else {
                    score -= MISMATCH_PENALTY;
                    // 不匹配：两张牌先都保持翻开，由界面延时调用 clearChosen() 统一翻回去
                    otherCard.isChosen = true;
                    card.isChosen = true;
                }
                score -= COST_TO_CHOOSE;
                return;
            }
        }
        score -= COST_TO_CHOOSE;
        card.isChosen = true;
    }

    /** 把所有「翻开但没配对」的牌翻回去（界面在展示完不匹配的两张牌后调用） */
    public void clearChosen() {
        for (Card card : cards) {
            if (!card.isMatched) {
                card.isChosen = false;
            }
        }
    }
}
