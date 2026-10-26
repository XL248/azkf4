# 实验四：翻牌游戏（Card Matching Game）

Android 应用，按《实验四 + 翻牌游戏应用》要求实现，采用 **MVC** 分层：

* **Model**：`cn.edu.sicnu.cardgame`（`Card` / `Deck` / `CardMatchingGame`，纯 Java，可直接单元测试）
* **View**：`res/layout/*.xml` + 自绘控件 `ui/CardView`
* **Controller**：`MainActivity` / `EditActivity` / `GameActivity`

## 功能

| 界面 | 功能 |
| --- | --- |
| MainActivity | 显示姓名（默认 Komeiji Koishi，性别女）、电话、最高分；两个入口按钮 |
| EditActivity | 修改姓名 / 性别 / 电话，保存后返回主界面刷新（仅存内存，不落盘） |
| GameActivity | 4×4 共 16 张牌的翻牌配对，实时显示分数、最高分、已配对数，可重开一局 |

发牌规则：一局 16 张 = **8 对**（`Deck.drawMatchedCards()` 保证每个点数正好 2 张），所以整局一定可以全部配对完成，不会出现落单的牌。

游戏规则：每翻开一张牌 −1 分；**点数相同**（花色无关）才算配对、得 4 分奖励；点数不同 −2 分，两张牌先展示约 0.75 秒再一起翻回去。

## 目录

```
app/src/main/java/cn/edu/sicnu/
├── cardgame/                 # 模型层（Card / Deck / CardMatchingGame）
└── cs/stu/baixinyuan/azshiyan4/
    ├── MainActivity.java     # 主界面
    ├── EditActivity.java     # 信息修改
    ├── GameActivity.java     # 翻牌游戏（控制器）
    ├── data/                 # Player / PlayerStore（内存单例）
    ├── ui/CardView.java      # 自绘卡牌（背面斜纹、正面点数花色、翻转动画）
    └── util/SystemBars.java  # Android 15+ 边到边适配
app/src/test/java/.../CardMatchingGameTest.java   # 6 个模型单元测试
```

## 构建与运行

```bash
./gradlew :app:testDebugUnitTest   # 跑单元测试
./gradlew :app:assembleDebug       # 生成 app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

* minSdk 24 / targetSdk 37 / JDK 17
* 主题：`Theme.MaterialComponents.DayNight.DarkActionBar.Bridge`，日间/夜间两套配色（`values-night/`）
* 所有界面文字都在 `res/values/strings.xml`，没有硬编码字符串
