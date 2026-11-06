package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cn.edu.sicnu.cardgame.Card;
import cn.edu.sicnu.cardgame.CardMatchingGame;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5.R;

/**
 * 【实验五】RecyclerView 的适配器（MVC 中的 View 与 Controller 的桥梁）。
 *
 * <p>职责：
 * <ol>
 *   <li>把 Model（{@link CardMatchingGame}）里的牌渲染成 {@link CardView}；</li>
 *   <li>重载 {@link #onCreateViewHolder} / {@link #onBindViewHolder} / {@link #getItemCount}；</li>
 *   <li>通过 {@link OnCardClickListener} 把点击回调给 Activity（控制器），
 *       Adapter 自己不修改模型。</li>
 * </ol>
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardHolder> {

    /** 点击回调：把「点的是第几张牌」交回控制器处理 */
    public interface OnCardClickListener {
        void onCardClick(int position);
    }

    private final CardMatchingGame game;
    private final OnCardClickListener listener;
    /** 每个格子的高度（像素）；<= 0 时用屏幕高度估算，避免首帧高度为 0 */
    private int itemHeight = 0;

    public CardAdapter(CardMatchingGame game, OnCardClickListener listener) {
        this.game = game;
        this.listener = listener;
    }

    /** 由 Activity 按「可用高度 / 行数」算好每个格子的像素高度，保证铺满且规整 */
    public void setItemHeight(int itemHeight) {
        if (itemHeight > 0 && this.itemHeight != itemHeight) {
            this.itemHeight = itemHeight;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    public int getItemHeight() {
        return itemHeight;
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new CardHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CardHolder holder, int position) {
        holder.bind(game.cardAtIndex(position));
    }

    @Override
    public int getItemCount() {
        return game.getCardCount();
    }

    /** 一次刷新所有牌（重新发牌、错配翻回后调用） */
    public void notifyAllCardsChanged() {
        notifyItemRangeChanged(0, getItemCount());
    }

    class CardHolder extends RecyclerView.ViewHolder {

        private final CardView cardView;

        CardHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            itemView.setOnClickListener(v -> {
                if (listener == null) {
                    return;
                }
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onCardClick(position);
                }
            });
        }

        void bind(Card card) {
            int height = itemHeight > 0
                    ? itemHeight
                    : itemView.getResources().getDisplayMetrics().heightPixels / 8;
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            if (params.height != height) {
                params.height = height;
                itemView.setLayoutParams(params);
            }
            cardView.bind(card);
            cardView.setMatched(card.isMatched);
            // 与当前显示状态不同才播放翻转动画，复用时不会重复动画
            cardView.setFaceUp(card.isChosen, card.isChosen != cardView.isFaceUp());
            itemView.setClickable(!card.isMatched);

            boolean faceUp = card.isChosen || card.isMatched;
            itemView.setContentDescription(faceUp
                    ? itemView.getContext().getString(R.string.card_face_desc,
                            card.getRank(), card.getSuit())
                    : itemView.getContext().getString(R.string.card_back_desc));
        }
    }
}
