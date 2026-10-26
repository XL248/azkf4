package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.util.SystemBars;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sicnu.cardgame.Card;
import cn.edu.sicnu.cardgame.CardMatchingGame;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.data.Player;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.data.PlayerStore;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.ui.CardView;

/**
 * 翻牌游戏 Activity，同时充当控制器（Controller）：
 * 接收用户点击 → 交给模型计算 → 再把模型状态同步回视图。
 */
public class GameActivity extends AppCompatActivity {

    /** 不匹配后延时翻回的任务，重开一局时要取消 */
    @Nullable
    private Runnable pendingClear;


    /** 一局的牌数：4 行 4 列 */
    private static final int COLUMN_COUNT = 4;
    private static final int CARD_COUNT = 16;
    /** 不匹配时两张牌保持翻开的时间（毫秒） */
    private static final long MISMATCH_REVEAL_MS = 750L;

    private CardMatchingGame game;
    private final List<CardView> cardViews = new ArrayList<>();

    private TextView scoreText;
    private TextView bestText;
    private TextView matchedText;
    private TextView statusText;
    private GridLayout board;
    /** 不匹配展示期间锁住点击，避免玩家点到第三张牌 */
    private boolean locked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_game);
        SystemBars.apply(findViewById(R.id.root));

        scoreText = findViewById(R.id.text_score);
        bestText = findViewById(R.id.text_best);
        matchedText = findViewById(R.id.text_matched);
        statusText = findViewById(R.id.text_status);

        board = findViewById(R.id.board);
        board.setColumnCount(COLUMN_COUNT);
        board.setRowCount(CARD_COUNT / COLUMN_COUNT);
        buildCards(board);

        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        MaterialButton restartButton = findViewById(R.id.button_restart);
        restartButton.setOnClickListener(v -> startNewGame());

        startNewGame();
    }

    /** 创建 16 个卡片视图并放进网格 */
    private void buildCards(GridLayout board) {
        int margin = getResources().getDimensionPixelSize(R.dimen.card_gap);
        cardViews.clear();
        for (int i = 0; i < CARD_COUNT; i++) {
            CardView cardView = new CardView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(i % COLUMN_COUNT, 1f);
            params.rowSpec = GridLayout.spec(i / COLUMN_COUNT, 1f);
            params.setMargins(margin, margin, margin, margin);
            final int index = i;
            cardView.setOnClickListener(v -> onCardClicked(index));
            board.addView(cardView, params);
            cardViews.add(cardView);
        }
    }

    /** 开始新的一局 */
    private void startNewGame() {
        cancelPendingClear();
        locked = false;
        game = new CardMatchingGame(CARD_COUNT);
        for (int i = 0; i < cardViews.size(); i++) {
            cardViews.get(i).bind(game.cardAtIndex(i));
        }
        syncViews(false);
        updateHeader();
        statusText.setText(R.string.status_hint);
    }

    /** 用户点击某一张牌 */
    private void onCardClicked(int index) {
        if (locked) {
            return;
        }
        Card card = game.cardAtIndex(index);
        if (card.isMatched) {
            return;
        }
        int matchedBefore = game.getMatchedCount();
        game.chooseCardAtIndex(index);
        syncViews(true);
        updateHeader();

        if (game.isFinished()) {
            statusText.setText(R.string.status_finished);
            return;
        }
        // 两张牌点数不同：先让玩家看清牌面，延时后把两张都翻回去
        if (game.getMatchedCount() == matchedBefore && game.getChosenUnmatchedCount() >= 2) {
            locked = true;
            statusText.setText(R.string.status_mismatch);
            pendingClear = () -> {
                game.clearChosen();
                syncViews(true);
                statusText.setText(R.string.status_hint);
                locked = false;
                pendingClear = null;
            };
            board.postDelayed(pendingClear, MISMATCH_REVEAL_MS);
        } else {
            statusText.setText(R.string.status_hint);
        }
    }

    /** 取消还没执行的「翻回去」延时任务（重开一局时用） */
    private void cancelPendingClear() {
        if (pendingClear != null) {
            board.removeCallbacks(pendingClear);
            pendingClear = null;
        }
    }

    /** 把模型状态同步到界面 */
    private void syncViews(boolean animate) {
        for (int i = 0; i < cardViews.size(); i++) {
            Card card = game.cardAtIndex(i);
            CardView cardView = cardViews.get(i);
            cardView.bind(card);
            cardView.setMatched(card.isMatched);
            boolean up = card.isChosen || card.isMatched;
            cardView.setFaceUp(up, animate && up != cardView.isFaceUp());
            cardView.setContentDescription(getString(
                    (card.isChosen || card.isMatched) ? R.string.card_face_desc : R.string.card_back_desc,
                    card.getRank(), card.getSuit()));
        }
    }

    /** 刷新分数、最高分、已配对数 */
    private void updateHeader() {
        scoreText.setText(getString(R.string.value_score, game.getScore()));
        matchedText.setText(getString(R.string.value_matched, game.getMatchedCount()));
        Player player = PlayerStore.get();
        player.updateHighScore(game.getScore());
        bestText.setText(getString(R.string.value_best, player.getHighScore()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewGroup parent = findViewById(R.id.board);
        if (parent != null) {
            parent.removeAllViews();
        }
    }
}
