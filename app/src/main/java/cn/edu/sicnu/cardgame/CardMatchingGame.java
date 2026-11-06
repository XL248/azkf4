package cn.edu.sicnu.cardgame;

import java.util.ArrayList;
import java.util.List;

/**
 * 翻牌游戏模型（实验五：3 张牌配对规则）。
 *
 * <p>规则：一次翻开 3 张牌，若三张点数完全相同则配对成功（3 张一起锁定），
 * 否则算错配，三张牌保持翻开等待界面翻回。花色不参与判定。</p>
 *
 * <p>计分：每翻开 1 张 -1；配对成功 +4 × 3；错配 -2 × 2。</p>
 *
 * <p>本类不依赖任何 Android 组件（MVC 的 Model 层）。</p>
 */
public class CardMatchingGame {

    /** 一局配对所需的牌数（3 张牌模式） */
    public static final int MATCH_GROUP_SIZE = Card.MATCH_GROUP_SIZE;

    private static final int MATCH_BONUS = 4;
    private static final int MISMATCH_PENALTY = 2;
    private static final int COST_TO_CHOOSE = 1;
    /** reset() 无参时的默认张数（对应竖屏 3 列 × 8 行） */
    private static final int DEFAULT_CARD_COUNT = 24;

    /** 本次 chooseCardAtIndex 的结果，供 Controller 决定是否需要延时翻回 */
    public enum Result {
        /** 还在凑 3 张，未结算 */
        NONE,
        /** 本次凑齐 3 张且配对成功 */
        MATCHED,
        /** 本次凑齐 3 张但点数不同 */
        MISMATCH
    }

    private final List<Card> cards = new ArrayList<>();
    private int score;
    private int lastCardCount = DEFAULT_CARD_COUNT;
    private Result lastResult = Result.NONE;

    /** 按张数从牌堆发一副必定能全部配对的牌 */
    public CardMatchingGame(int count) {
        reset(count);
    }

    /** 直接用给定的牌构造（单元测试用），不会重新发牌 */
    public CardMatchingGame(List<Card> givenCards) {
        if (givenCards != null) {
            cards.addAll(givenCards);
        }
        lastCardCount = cards.size();
        score = 0;
        lastResult = Result.NONE;
    }

    /**
     * 【实验五新增】重开一局：按上一次的张数重新发牌、分数清零、所有牌翻回。
     */
    public void reset() {
        reset(lastCardCount);
    }

    /**
     * 【实验五新增】按指定张数重开一局。
     *
     * @param count 牌数，必须是 {@link #MATCH_GROUP_SIZE} 的整数倍
     */
    public void reset(int count) {
        List<Card> dealt = new Deck().drawMatchedCards(count, MATCH_GROUP_SIZE);
        cards.clear();
        cards.addAll(dealt);
        lastCardCount = cards.size();
        score = 0;
        lastResult = Result.NONE;
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

    public Result getLastResult() {
        return lastResult;
    }

    /** 当前翻开（含已配对）的牌数 */
    public int getChosenCount() {
        int count = 0;
        for (Card card : cards) {
            if (card.isChosen) {
                count++;
            }
        }
        return count;
    }

    /** 当前翻开但还没配对的牌数（0..3），凑满 3 张才结算 */
    public int getChosenUnmatchedCount() {
        int count = 0;
        for (Card card : cards) {
            if (card.isChosen && !card.isMatched) {
                count++;
            }
        }
        return count;
    }

    public int getMatchedCount() {
        int count = 0;
        for (Card card : cards) {
            if (card.isMatched) {
                count++;
            }
        }
        return count;
    }

    public boolean isFinished() {
        for (Card card : cards) {
            if (!card.isMatched) {
                return false;
            }
        }
        return !cards.isEmpty();
    }

    /**
     * 【实验五】处理一次点击：3 张牌配对。
     *
     * <ul>
     *   <li>已配对的牌不可再操作；</li>
     *   <li>已翻开但未配对的牌再点一次 = 收回；</li>
     *   <li>凑满 3 张未配对的翻开牌时结算，并把结果写入 {@link #getLastResult()}。</li>
     * </ul>
     */
    public void chooseCardAtIndex(int index) {
        lastResult = Result.NONE;
        if (index < 0 || index >= cards.size()) {
            return;
        }
        Card card = cards.get(index);
        if (card.isMatched) {
            return;
        }
        if (card.isChosen) {
            card.isChosen = false;
            return;
        }

        card.isChosen = true;
        score -= COST_TO_CHOOSE;

        // 收集本次「已翻开且未配对」的牌
        List<Card> opened = new ArrayList<>(MATCH_GROUP_SIZE);
        for (Card other : cards) {
            if (other.isChosen && !other.isMatched) {
                opened.add(other);
            }
        }
        if (opened.size() < MATCH_GROUP_SIZE) {
            return;
        }

        // 用「除自己以外」的另外 2 张做判定，避免自己和自己比较
        List<Card> others = new ArrayList<>(MATCH_GROUP_SIZE - 1);
        for (Card other : opened) {
            if (other != card) {
                others.add(other);
            }
        }
        if (others.size() != MATCH_GROUP_SIZE - 1) {
            return;
        }

        int matchScore = card.match(others.toArray(new Card[0]));
        if (matchScore > 0) {
            score += matchScore * MATCH_GROUP_SIZE;
            for (Card other : opened) {
                other.isMatched = true;
            }
            lastResult = Result.MATCHED;
        } else {
            score -= MISMATCH_PENALTY * (MATCH_GROUP_SIZE - 1);
            lastResult = Result.MISMATCH;
            // 三张保持翻开，等界面延时后调用 clearChosen()
        }
    }

    /** 把没配对的翻开牌全部翻回去（错配展示结束后由界面调用） */
    public void clearChosen() {
        for (Card card : cards) {
            if (!card.isMatched) {
                card.isChosen = false;
            }
        }
        lastResult = Result.NONE;
    }
}
