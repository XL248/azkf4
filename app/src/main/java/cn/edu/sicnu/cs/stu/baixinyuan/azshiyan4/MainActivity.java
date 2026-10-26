package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.util.SystemBars;
import com.google.android.material.button.MaterialButton;

import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.data.Player;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.data.PlayerStore;

/**
 * 主 Activity：显示当前玩家的基本信息，并提供进入「信息修改」与「翻牌游戏」的入口。
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        SystemBars.apply(findViewById(R.id.root));

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
