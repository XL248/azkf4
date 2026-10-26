package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import cn.edu.sicnu.cardgame.Card;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.R;

/**
 * 单张卡牌的视图（View）：
 * 只负责「把一张 Card 画出来」，不包含任何游戏规则。
 */
public class CardView extends View {

    private static final long FLIP_DURATION = 190L;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint patternPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private String rank = "";
    private String suit = "";
    private boolean faceUp = false;
    private boolean matched = false;

    /** 翻转进度：0 = 背面朝上，1 = 正面朝上 */
    private float flip = 0f;
    @Nullable
    private ValueAnimator animator;

    // 颜色与绘制对象全部缓存，onDraw 里不允许再 new（否则每帧分配会引发 GC 卡顿）
    private int colorBackStart;
    private int colorBackEnd;
    private int colorBackStroke;
    private int colorFace;
    private int colorBorder;
    private int colorMatchedBorder;
    private int colorMatchedOverlay;
    private int colorRed;
    private int colorDark;
    @Nullable
    private LinearGradient backShader;
    @Nullable
    private android.graphics.Path diamondPath;
    private float diamondSize = -1f;
    private final Paint.FontMetrics fontMetrics = new Paint.FontMetrics();

    public CardView(Context context) {
        this(context, null);
    }

    public CardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dp(1.2f));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        colorBackStart = ContextCompat.getColor(context, R.color.card_back_start);
        colorBackEnd = ContextCompat.getColor(context, R.color.card_back_end);
        colorBackStroke = ContextCompat.getColor(context, R.color.card_back_stroke);
        colorFace = ContextCompat.getColor(context, R.color.card_face);
        colorBorder = ContextCompat.getColor(context, R.color.card_border);
        colorMatchedBorder = ContextCompat.getColor(context, R.color.card_matched_border);
        colorMatchedOverlay = ContextCompat.getColor(context, R.color.card_matched_overlay);
        colorRed = Color.parseColor("#C62828");
        colorDark = Color.parseColor("#23303F");
        patternPaint.setColor(colorBackStroke);
        patternPaint.setAlpha(38);
        patternPaint.setStrokeWidth(dp(1f));
        setFocusable(true);
    }

    /** 绑定模型数据（不负责翻转动画） */
    public void bind(Card card) {
        String newRank = card.getRank();
        String newSuit = card.getSuit();
        if (newRank.equals(rank) && newSuit.equals(suit)) {
            return;
        }
        rank = newRank;
        suit = newSuit;
        invalidate();
    }

    /** 是否是红色的花色 */
    public boolean isRedSuit() {
        return suit.contains("♥") || suit.contains("♦");
    }

    public void setMatched(boolean matched) {
        if (this.matched == matched) {
            return;
        }
        this.matched = matched;
        invalidate();
    }

    /** 当前是否正面朝上（界面据此判断要不要播翻转动画） */
    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean up, boolean animate) {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        float target = up ? 1f : 0f;
        if (!animate || Math.abs(target - flip) < 0.01f) {
            flip = target;
            faceUp = up;
            invalidate();
            return;
        }
        final float from = flip;
        animator = ValueAnimator.ofFloat(from, target);
        animator.setDuration(FLIP_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            flip = (float) a.getAnimatedValue();
            faceUp = flip >= 0.5f;
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        float radius = Math.min(w, h) * 0.14f;
        rect.set(dp(1.5f), dp(1.5f), w - dp(1.5f), h - dp(1.5f));
        if (Math.abs(rect.width() - cachedWidth) > 0.5f || Math.abs(rect.height() - cachedHeight) > 0.5f) {
            cachedWidth = rect.width();
            cachedHeight = rect.height();
            backShader = null;
            diamondPath = null;
        }

        // 卡片水平压缩模拟 3D 翻转
        float scaleX = Math.abs((float) Math.cos(Math.PI * flip));
        scaleX = Math.max(scaleX, 0.06f);
        int save = canvas.save();
        canvas.scale(scaleX, 1f, w / 2f, h / 2f);

        if (flip < 0.5f) {
            drawBack(canvas, radius);
        } else {
            drawFace(canvas, radius);
        }
        canvas.restoreToCount(save);

        if (matched) {
            strokePaint.setColor(colorMatchedBorder);
            strokePaint.setStrokeWidth(dp(2.4f));
            canvas.drawRoundRect(rect, radius, radius, strokePaint);
        }
    }

    private void drawBack(Canvas canvas, float radius) {
        if (backShader == null) {
            backShader = new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    colorBackStart, colorBackEnd, Shader.TileMode.CLAMP);
        }
        fillPaint.setShader(backShader);
        canvas.drawRoundRect(rect, radius, radius, fillPaint);
        fillPaint.setShader(null);

        // 背面斜纹
        int save = canvas.save();
        canvas.clipRect(rect);
        int step = (int) dp(9f);
        for (int x = (int) rect.left - (int) rect.height(); x < rect.right; x += step) {
            canvas.drawLine(x, rect.bottom, x + rect.height(), rect.top, patternPaint);
        }
        canvas.restoreToCount(save);

        strokePaint.setColor(colorBackStroke);
        strokePaint.setStrokeWidth(dp(1.2f));
        canvas.drawRoundRect(rect, radius, radius, strokePaint);

        // 中间的小菱形
        fillPaint.setColor(colorBackStroke);
        fillPaint.setAlpha(90);
        float cx = rect.centerX();
        float cy = rect.centerY();
        float d = Math.min(rect.width(), rect.height()) * 0.16f;
        if (diamondPath == null || Math.abs(d - diamondSize) > 0.5f) {
            diamondSize = d;
            diamondPath = new android.graphics.Path();
            diamondPath.moveTo(cx, cy - d);
            diamondPath.lineTo(cx + d, cy);
            diamondPath.lineTo(cx, cy + d);
            diamondPath.lineTo(cx - d, cy);
            diamondPath.close();
            diamondQuad.set(rect);
        } else if (!rect.equals(diamondQuad)) {
            diamondQuad.set(rect);
            diamondPath.offset(rect.centerX() - cx, rect.centerY() - cy);
        }
        canvas.drawPath(diamondPath, fillPaint);
        fillPaint.setAlpha(255);
    }

    private void drawFace(Canvas canvas, float radius) {
        fillPaint.setColor(colorFace);
        canvas.drawRoundRect(rect, radius, radius, fillPaint);

        if (matched) {
            fillPaint.setColor(colorMatchedOverlay);
            canvas.drawRoundRect(rect, radius, radius, fillPaint);
        }

        strokePaint.setColor(colorBorder);
        strokePaint.setStrokeWidth(dp(1.2f));
        canvas.drawRoundRect(rect, radius, radius, strokePaint);

        float size = Math.min(rect.width(), rect.height());
        textPaint.setColor(isRedSuit() ? colorRed : colorDark);

        // 点数
        textPaint.setTextSize(size * 0.44f);
        float cx = rect.centerX();
        float cy = rect.centerY();
        textPaint.getFontMetrics(fontMetrics);
        float baseline = cy - (fontMetrics.ascent + fontMetrics.descent) / 2f - size * 0.06f;
        canvas.drawText(rank, cx, baseline, textPaint);

        // 花色
        textPaint.setTextSize(size * 0.26f);
        canvas.drawText(suit, cx, baseline + size * 0.30f, textPaint);

        // 左上角小标记
        textPaint.setTextSize(size * 0.15f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(rank, rect.left + size * 0.12f, rect.top + size * 0.26f, textPaint);
        canvas.drawText(suit, rect.left + size * 0.12f, rect.top + size * 0.44f, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private final RectF diamondQuad = new RectF();
    private float cachedWidth = -1f;
    private float cachedHeight = -1f;

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
