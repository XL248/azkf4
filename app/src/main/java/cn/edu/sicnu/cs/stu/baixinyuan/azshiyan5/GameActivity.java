package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import cn.edu.sicnu.cardgame.Card;
import cn.edu.sicnu.cardgame.CardMatchingGame;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5.data.Player;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5.data.PlayerStore;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5.ui.CardAdapter;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5.util.SystemBars;

/**
 * 翻牌游戏 Activity，实验五改造后的控制器（Controller）：
 * <ul>
 *   <li>视图用 {@link RecyclerView} + {@link CardAdapter}（不再手工 addView）；</li>
 *   <li>点击事件由 Adapter 通过回调交回本类，再交给 Model 计算；</li>
 *   <li>支持横竖屏不同牌数：竖屏 3 列 × 8 行 = 24 张，横屏 4 列 × 3 行 = 12 张。</li>
 * </ul>
 */
public class GameActivity extends AppCompatActivity implements CardAdapter.OnCardClickListener {

    /** 竖屏：4 列 6 行 = 24 张 */
    private static final int PORTRAIT_SPAN = 4;
    private static final int PORTRAIT_CARDS = 24;
    /** 横屏：6 列 2 行 = 12 张 */
    private static final int LANDSCAPE_SPAN = 6;
    private static final int LANDSCAPE_CARDS = 12;

    /** 错配时三张牌保持翻开的时间（毫秒） */
    private static final long MISMATCH_REVEAL_MS = 750L;

    private CardMatchingGame game;
    private CardAdapter adapter;

    private TextView scoreText;
    private TextView bestText;
    private TextView matchedText;
    private TextView statusText;
    private RecyclerView board;

    /** 错配展示期间锁住点击，避免玩家提前点下一张 */
    private boolean locked;
    /** 延时翻回的任务，重开一局或旋转时要取消 */
    @Nullable
    private Runnable pendingClear;

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

        game = new CardMatchingGame(cardCount());
        adapter = new CardAdapter(game, this);
        board.setLayoutManager(new GridLayoutManager(this, spanCount()));
        board.setAdapter(adapter);
        // 等 RecyclerView 测量完成后再按可用高度分配格子高度
        board.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                applyItemHeight();
            }
        });

        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        ((MaterialButton) findViewById(R.id.button_restart)).setOnClickListener(v -> startNewGame());

        startNewGame();
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private int spanCount() {
        return isLandscape() ? LANDSCAPE_SPAN : PORTRAIT_SPAN;
    }

    private int cardCount() {
        return isLandscape() ? LANDSCAPE_CARDS : PORTRAIT_CARDS;
    }

    /** 开始新的一局（牌数由当前屏幕方向决定） */
    private void startNewGame() {
        cancelPendingClear();
        locked = false;
        game.reset(cardCount());
        adapter.notifyAllCardsChanged();
        applyItemHeight();
        board.post(this::applyItemHeight);
        updateHeader();
        statusText.setText(R.string.status_hint);
    }

    /** 按「可用高度 / 行数」算出每个格子的高度，保证牌面铺满且规整 */
    private void applyItemHeight() {
        if (adapter == null || board == null) {
            return;
        }
        int available = board.getHeight() - board.getPaddingTop() - board.getPaddingBottom();
        if (available <= 0) {
            // 首帧还没测量完：先用屏幕高度估算，布局完成后的回调会再修正
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            int reserved = getResources().getDimensionPixelSize(R.dimen.button_height) + 220;
            available = metrics.heightPixels - reserved;
        }
        int rows = Math.max(1, cardCount() / spanCount());
        adapter.setItemHeight(Math.max(1, available / rows));
        board.post(board::requestLayout);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyItemHeight();
        }
    }

    /** Adapter 的回调：用户点了第 index 张牌 */
    @Override
    public void onCardClick(int index) {
        if (locked || index < 0 || index >= game.getCardCount()) {
            return;
        }
        Card card = game.cardAtIndex(index);
        if (card.isMatched) {
            return;
        }

        game.chooseCardAtIndex(index);
        adapter.notifyAllCardsChanged();
        updateHeader();

        if (game.isFinished()) {
            statusText.setText(R.string.status_finished);
            return;
        }
        if (game.getLastResult() == CardMatchingGame.Result.MISMATCH) {
            // 三张点数不同：先让玩家看清牌面，延时后统一翻回去
            locked = true;
            statusText.setText(R.string.status_mismatch);
            pendingClear = () -> {
                game.clearChosen();
                adapter.notifyAllCardsChanged();
                statusText.setText(R.string.status_hint);
                locked = false;
                pendingClear = null;
            };
            board.postDelayed(pendingClear, MISMATCH_REVEAL_MS);
        } else {
            statusText.setText(R.string.status_hint);
        }
    }

    private void cancelPendingClear() {
        if (pendingClear != null) {
            board.removeCallbacks(pendingClear);
            pendingClear = null;
        }
    }

    /** 刷新分数、最高分、已配对数 */
    private void updateHeader() {
        scoreText.setText(getString(R.string.value_score, game.getScore()));
        matchedText.setText(getString(R.string.value_matched, game.getMatchedCount(), game.getCardCount()));
        Player player = PlayerStore.get();
        player.updateHighScore(game.getScore());
        bestText.setText(getString(R.string.value_best, player.getHighScore()));
    }

    /** 横竖屏切换：换列数、换牌数，重新发牌（分数重置） */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        cancelPendingClear();
        ((GridLayoutManager) board.getLayoutManager()).setSpanCount(spanCount());
        startNewGame();
        String orientation = getString(isLandscape()
                ? R.string.orientation_landscape : R.string.orientation_portrait);
        statusText.setText(getString(R.string.status_rotated, orientation, cardCount()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelPendingClear();
        if (board != null) {
            board.setAdapter(null);
        }
    }
}
