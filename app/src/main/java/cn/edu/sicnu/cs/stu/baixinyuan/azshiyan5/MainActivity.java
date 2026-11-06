package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5.data.Player;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5.data.PlayerStore;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5.util.SystemBars;

/**
 * 主 Activity：显示当前玩家的基本信息，并提供进入「信息修改」与「翻牌游戏」的入口。
 *
 * <p>【实验五】新增菜单：右上角 ActionBar 的溢出菜单「关于」，点击弹出关于对话框。</p>
 */
public class MainActivity extends AppCompatActivity {

    private TextView nameValue;
    private TextView genderValue;
    private TextView phoneValue;
    private TextView highScoreValue;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> refreshPlayerInfo());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerStore.init(getString(R.string.default_name),
                getString(R.string.default_gender),
                getString(R.string.default_phone));

        // 实验五：保留 ActionBar 用来承载菜单项
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(R.string.title_main);
            bar.show();
        }

        setContentView(R.layout.activity_main);
        // ActionBar 已经占住顶部，这里不再叠加状态栏高度
        SystemBars.applyAvoidTop(findViewById(R.id.root));

        nameValue = findViewById(R.id.value_name);
        genderValue = findViewById(R.id.value_gender);
        phoneValue = findViewById(R.id.value_phone);
        highScoreValue = findViewById(R.id.value_high_score);

        MaterialButton editButton = findViewById(R.id.button_edit);
        editButton.setOnClickListener(v ->
                editLauncher.launch(new Intent(MainActivity.this, EditActivity.class)));

        MaterialButton playButton = findViewById(R.id.button_play);
        playButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, GameActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** 【实验五】「关于」对话框 */
    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.about_title)
                .setMessage(getString(R.string.about_desc)
                        + "\n\n" + getString(R.string.about_version, versionName())
                        + "\n" + getString(R.string.about_author))
                .setPositiveButton(R.string.about_close, null)
                .show();
    }

    private String versionName() {
        try {
            PackageInfo info = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            return info.versionName == null ? "" : info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPlayerInfo();
    }

    /** 用内存中的玩家数据刷新界面 */
    private void refreshPlayerInfo() {
        Player player = PlayerStore.get();
        nameValue.setText(player.getName());
        genderValue.setText(player.getGender());
        phoneValue.setText(player.getPhone());
        highScoreValue.setText(String.valueOf(player.getHighScore()));
    }
}
