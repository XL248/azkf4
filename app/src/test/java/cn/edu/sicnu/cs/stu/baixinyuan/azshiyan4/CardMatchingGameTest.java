package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import cn.edu.sicnu.cardgame.Card;
import cn.edu.sicnu.cardgame.CardMatchingGame;

/**
 * 翻牌游戏模型层的单元测试（不依赖 Android 环境，可在 JVM 上直接跑）。
 */
public class CardMatchingGameTest {

    private Card card1;
    private Card card2;
    private Card card3;
    private List<Card> cards;

    @Before
    public void setUp() {
        // 两张点数相同、一张完全不同
        card1 = new Card("\u2665", "A");
        card2 = new Card("\u2665", "A");
        card3 = new Card("\u2660", "K");
        cards = Arrays.asList(card1, card2, card3);
    }

    @Test
    public void chooseCard_flipsCardAndCostsOnePoint() {
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);

        assertTrue("点击后卡牌应处于翻开状态", card1.isChosen);
        assertFalse("未配上对", card1.isMatched);
        assertEquals("每翻开一张牌扣 1 分", -1, game.getScore());
    }

    @Test
    public void chooseSameCardAgain_turnsItBackWithoutExtraCost() {
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(0);

        assertFalse("再次点击应收回卡片", card1.isChosen);
        assertEquals("收回不额外扣分", -1, game.getScore());
    }

    @Test
    public void matchTwoCardsWithSameRank_marksBothMatchedAndAddsScore() {
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);

        assertTrue(card1.isMatched);
        assertTrue(card2.isMatched);
        // -1（第一张） + 4*4（配对奖励） -1（第二张选择代价）
        assertEquals(14, game.getScore());
        assertEquals(2, game.getMatchedCount());
    }

    @Test
    public void mismatch_penalisesAndBothCardsStayVisibleUntilCleared() {
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);   // 打开 ♥A
        game.chooseCardAtIndex(2);   // 与 ♠K 比较：不匹配

        // 不匹配后两张牌先都保持翻开（界面借此展示牌面），随后统一翻回去
        assertTrue("第一张牌暂时保持翻开", card1.isChosen);
        assertTrue("第二张牌保持翻开", card3.isChosen);
        // -1 -2 -1
        assertEquals(-4, game.getScore());
        assertEquals(0, game.getMatchedCount());

        game.clearChosen();
        assertFalse("clearChosen 后第一张翻回去", card1.isChosen);
        assertFalse("clearChosen 后第二张翻回去", card3.isChosen);
    }

    @Test
    public void sameSuitDifferentRank_isNotAMatch() {
        // ♥A 与 ♥K：花色相同但点数不同，不能算配对
        Card heartKing = new Card("♥", "K");
        CardMatchingGame game = new CardMatchingGame(Arrays.asList(
                new Card("♥", "A"), heartKing, new Card("♠", "Q")));

        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);

        assertEquals("花色相同但点数不同，不应配对", 0, game.getMatchedCount());
        assertEquals(-4, game.getScore());
    }

    @Test
    public void choosingMatchedCard_hasNoEffect() {
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);
        int scoreAfterMatch = game.getScore();

        game.chooseCardAtIndex(0);
        assertEquals("已配对的牌不可再操作", scoreAfterMatch, game.getScore());
        assertTrue(card1.isMatched);
    }

    @Test
    public void chosenUnmatchedCount_ignoresMatchedCards() {
        // 回归测试：配对成功之后，两张牌仍然 isChosen=true，
        // 若用「翻开总数」判断不匹配，之后每次单击都会被误判成「翻了两张」。
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);
        assertEquals("配对后已翻开的牌有 2 张", 2, game.getChosenCount());
        assertEquals("但未配对的翻开数为 0", 0, game.getChosenUnmatchedCount());

        // 再翻一张别的牌：此时只有 1 张未配对，不该被当成不匹配
        game.chooseCardAtIndex(2);
        assertEquals(1, game.getChosenUnmatchedCount());
    }

    @Test
    public void newGameDeck_isAlwaysFullyMatchable() {
        for (int round = 0; round < 30; round++) {
            CardMatchingGame game = new CardMatchingGame(16);
            assertEquals(16, game.getCardCount());

            // 每个点数必须出现偶数次（本项目里正好 2 次），否则一定有牌配不上
            java.util.Map<String, Integer> rankCount = new java.util.HashMap<>();
            for (int i = 0; i < game.getCardCount(); i++) {
                String rank = game.cardAtIndex(i).getRank();
                rankCount.put(rank, rankCount.containsKey(rank) ? rankCount.get(rank) + 1 : 1);
            }
            for (java.util.Map.Entry<String, Integer> entry : rankCount.entrySet()) {
                assertEquals("点数 " + entry.getKey() + " 必须成对出现", 0, entry.getValue() % 2);
            }

            // 按点数贪心配对，必须能把 16 张全部配完
            boolean[] done = new boolean[game.getCardCount()];
            for (int i = 0; i < game.getCardCount(); i++) {
                if (done[i]) {
                    continue;
                }
                for (int j = i + 1; j < game.getCardCount(); j++) {
                    if (done[j]) {
                        continue;
                    }
                    if (game.cardAtIndex(i).getRank().equals(game.cardAtIndex(j).getRank())) {
                        game.chooseCardAtIndex(i);
                        game.chooseCardAtIndex(j);
                        done[i] = true;
                        done[j] = true;
                        break;
                    }
                }
            }
            assertTrue("第 " + round + " 局应能全部配对完成", game.isFinished());
        }
    }

    @Test
    public void isFinished_onlyWhenEveryCardMatched() {
        CardMatchingGame game = new CardMatchingGame(cards);
        assertFalse(game.isFinished());
        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);
        assertFalse("还剩一张未配对", game.isFinished());
    }
}
