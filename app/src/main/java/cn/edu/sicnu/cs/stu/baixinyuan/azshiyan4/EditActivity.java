package cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.util.SystemBars;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.data.Player;
import cn.edu.sicnu.cs.stu.baixinyuan.azshiyan4.data.PlayerStore;

/**
 * 信息修改 Activity：修改姓名、性别、电话。
 * 按实验要求，修改后的结果只保存在内存中，不写入文件。
 */
public class EditActivity extends AppCompatActivity {

    private TextInputEditText nameInput;
    private TextInputEditText phoneInput;
    private RadioGroup genderGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_edit);
        SystemBars.apply(findViewById(R.id.root));

        nameInput = findViewById(R.id.input_name);
        phoneInput = findViewById(R.id.input_phone);
        genderGroup = findViewById(R.id.group_gender);

        Player player = PlayerStore.get();
        nameInput.setText(player.getName());
        phoneInput.setText(player.getPhone());
        RadioButton male = findViewById(R.id.radio_male);
        RadioButton female = findViewById(R.id.radio_female);
        if (getString(R.string.gender_female).equals(player.getGender())) {
            female.setChecked(true);
        } else {
            male.setChecked(true);
        }

        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        MaterialButton saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(v -> save());

        MaterialButton cancelButton = findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(v -> finish());
    }

    private void save() {
        String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
        String phone = phoneInput.getText() == null ? "" : phoneInput.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.error_name_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, R.string.error_phone_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        String gender = getString(R.string.gender_male);
        if (genderGroup.getCheckedRadioButtonId() == R.id.radio_female) {
            gender = getString(R.string.gender_female);
        }

        Player player = PlayerStore.get();
        player.setName(name);
        player.setPhone(phone);
        player.setGender(gender);

        setResult(RESULT_OK);
        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
