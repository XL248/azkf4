package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.sicnu.cardgame.Card;
import cn.edu.sicnu.cardgame.CardMatchingGame;

/**
 * 翻牌游戏模型层的单元测试（实验五：3 张牌配对规则，不依赖 Android 环境）。
 */
public class CardMatchingGameTest {

    private Card heartA;
    private Card diamondA;
    private Card spadeA;
    private Card spadeK;
    private List<Card> cards;

    @Before
    public void setUp() {
        // 三张点数相同（A），另一张不同（K）
        heartA = new Card("\u2665", "A");
        diamondA = new Card("\u2666", "A");
        spadeA = new Card("\u2660", "A");
        spadeK = new Card("\u2660", "K");
        cards = Arrays.asList(heartA, diamondA, spadeA, spadeK);
    }

    @Test
    public void chooseCard_flipsCardAndCostsOnePoint() {
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);

        assertTrue("点击后卡牌应处于翻开状态", heartA.isChosen);
        assertFalse("未配上对", heartA.isMatched);
        assertEquals("每翻开一张牌扣 1 分", -1, game.getScore());
        assertEquals("还没凑满 3 张，未结算", CardMatchingGame.Result.NONE, game.getLastResult());
    }

    @Test
    public void chooseSameCardAgain_turnsItBackWithoutExtraCost() {
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(0);

        assertFalse("再次点击应收回卡片", heartA.isChosen);
        assertEquals("收回不额外扣分", -1, game.getScore());
    }

    @Test
    public void matchThreeCardsWithSameRank_marksAllMatchedAndAddsScore() {
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);
        game.chooseCardAtIndex(2);

        assertTrue(heartA.isMatched);
        assertTrue(diamondA.isMatched);
        assertTrue(spadeA.isMatched);
        assertEquals("三张同点数应结算为配对成功",
                CardMatchingGame.Result.MATCHED, game.getLastResult());
        // -1 × 3（翻开三张） + 4 × 3（配对奖励）
        assertEquals(9, game.getScore());
        assertEquals(3, game.getMatchedCount());
    }

    @Test
    public void mismatch_penalisesAndThreeCardsStayVisibleUntilCleared() {
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);   // ♥A
        game.chooseCardAtIndex(1);   // ♦A
        game.chooseCardAtIndex(3);   // ♠K：第三张不同，错配

        // 错配后三张牌先都保持翻开（界面借此展示牌面），随后统一翻回去
        assertTrue(heartA.isChosen);
        assertTrue(diamondA.isChosen);
        assertTrue(spadeK.isChosen);
        assertEquals("三张不同点数应结算为错配",
                CardMatchingGame.Result.MISMATCH, game.getLastResult());
        // -1 × 3（翻开三张） - 2 × 2（错配惩罚）
        assertEquals(-7, game.getScore());
        assertEquals(0, game.getMatchedCount());

        game.clearChosen();
        assertFalse("clearChosen 后第一张翻回去", heartA.isChosen);
        assertFalse("clearChosen 后第二张翻回去", diamondA.isChosen);
        assertFalse("clearChosen 后第三张翻回去", spadeK.isChosen);
    }

    @Test
    public void twoSameOneDifferent_isNotAMatch() {
        CardMatchingGame game = new CardMatchingGame(Arrays.asList(
                new Card("\u2665", "A"), new Card("\u2666", "A"), new Card("\u2663", "Q")));

        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);
        game.chooseCardAtIndex(2);

        assertEquals("只有两张相同不算配对", 0, game.getMatchedCount());
        assertEquals(-7, game.getScore());
    }

    @Test
    public void sameSuitDifferentRank_isNotAMatch() {
        // 花色全相同但点数不同：依然不是配对
        CardMatchingGame game = new CardMatchingGame(Arrays.asList(
                new Card("\u2665", "A"), new Card("\u2665", "K"), new Card("\u2665", "Q")));

        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);
        game.chooseCardAtIndex(2);

        assertEquals("花色相同但点数不同，不应配对", 0, game.getMatchedCount());
        assertEquals(CardMatchingGame.Result.MISMATCH, game.getLastResult());
    }

    @Test
    public void choosingMatchedCard_hasNoEffect() {
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);
        game.chooseCardAtIndex(2);
        int scoreAfterMatch = game.getScore();

        game.chooseCardAtIndex(0);
        assertEquals("已配对的牌不可再操作", scoreAfterMatch, game.getScore());
        assertTrue(heartA.isMatched);
    }

    @Test
    public void chosenUnmatchedCount_ignoresMatchedCards() {
        // 回归测试：配对成功后三张仍然 isChosen=true，
        // 若用「翻开总数」判断，之后每次单击都会被误判成凑满 3 张。
        CardMatchingGame game = new CardMatchingGame(cards);
        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);
        game.chooseCardAtIndex(2);
        assertEquals("配对后已翻开的牌有 3 张", 3, game.getChosenCount());
        assertEquals("但未配对的翻开数为 0", 0, game.getChosenUnmatchedCount());

        game.chooseCardAtIndex(3);
        assertEquals("再翻一张：未配对数为 1", 1, game.getChosenUnmatchedCount());
    }

    @Test
    public void reset_redealsFullDeckAndZeroesScore() {
        CardMatchingGame game = new CardMatchingGame(24);
        assertEquals(24, game.getCardCount());

        game.chooseCardAtIndex(0);
        game.chooseCardAtIndex(1);
        game.chooseCardAtIndex(2);

        game.reset();
        assertEquals("reset 后分数归零", 0, game.getScore());
        assertEquals("reset 后张数不变", 24, game.getCardCount());
        assertEquals("reset 后没有已配对的牌", 0, game.getMatchedCount());
        assertEquals("reset 后没有翻开的牌", 0, game.getChosenCount());
        assertEquals(CardMatchingGame.Result.NONE, game.getLastResult());
    }

    @Test
    public void resetWithCount_usesRequestedCardCount() {
        CardMatchingGame game = new CardMatchingGame(24);
        game.reset(12);
        assertEquals("横屏 12 张", 12, game.getCardCount());
        assertEquals(0, game.getScore());
    }

    @Test
    public void newGameDeck_isAlwaysFullyMatchable() {
        for (int round = 0; round < 30; round++) {
            CardMatchingGame game = new CardMatchingGame(24);
            assertEquals(24, game.getCardCount());

            // 每个点数必须正好出现 3 次，否则一定有牌配不上
            Map<String, Integer> rankCount = new HashMap<>();
            for (int i = 0; i < game.getCardCount(); i++) {
                String rank = game.cardAtIndex(i).getRank();
                rankCount.put(rank, rankCount.containsKey(rank) ? rankCount.get(rank) + 1 : 1);
            }
            for (Map.Entry<String, Integer> entry : rankCount.entrySet()) {
                assertEquals("点数 " + entry.getKey() + " 必须成组出现（每组 3 张）",
                        0, entry.getValue() % Card.MATCH_GROUP_SIZE);
            }

            // 按点数贪心三连配对，必须能把 24 张全部配完
            boolean[] done = new boolean[game.getCardCount()];
            for (int i = 0; i < game.getCardCount(); i++) {
                if (done[i]) {
                    continue;
                }
                List<Integer> group = new ArrayList<>();
                group.add(i);
                for (int j = i + 1; j < game.getCardCount() && group.size() < Card.MATCH_GROUP_SIZE; j++) {
                    if (!done[j] && game.cardAtIndex(i).getRank().equals(game.cardAtIndex(j).getRank())) {
                        group.add(j);
                    }
                }
                if (group.size() < Card.MATCH_GROUP_SIZE) {
                    continue;
                }
                for (int index : group) {
                    game.chooseCardAtIndex(index);
                    done[index] = true;
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
        game.chooseCardAtIndex(2);
        assertFalse("还剩一张 ♠K 未配对", game.isFinished());
    }
}
