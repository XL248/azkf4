package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 边到边（edge-to-edge）适配小工具：
 * 在 Android 15+ 上系统会强制内容延伸到状态栏 / 导航栏下方，
 * 这里给根布局补上和系统栏等高的内边距，避免控件被遮挡。
 */
public final class SystemBars {

    private SystemBars() {
    }

    public static void apply(View root) {
        final int baseLeft = root.getPaddingLeft();
        final int baseTop = root.getPaddingTop();
        final int baseRight = root.getPaddingRight();
        final int baseBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(baseLeft + bars.left,
                    baseTop + bars.top,
                    baseRight + bars.right,
                    baseBottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
