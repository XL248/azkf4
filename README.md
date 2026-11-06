# 实验五：RecyclerView 翻牌游戏应用

Android 应用，在实验四基础上按《实验五 + RecyclerView 翻牌游戏应用》要求改造，采用 **MVC** 分层：

* **Model**：`cn.edu.sicnu.cardgame`（`Card` / `Deck` / `CardMatchingGame`，纯 Java，可直接单元测试）
* **View**：`res/layout/*.xml` + 自绘控件 `ui/CardView` + `RecyclerView` 适配器 `ui/CardAdapter`
* **Controller**：`MainActivity` / `EditActivity` / `GameActivity`

## 实验五改了哪些地方

| 要求 | 实现位置 |
| --- | --- |
| ① MainActivity 加菜单，点菜单弹「关于」对话框 | `MainActivity.onCreateOptionsMenu` / `onOptionsItemSelected` / `showAboutDialog`，菜单资源 `res/menu/main_menu.xml`，对话框用 `MaterialAlertDialogBuilder` |
| ② View 用 RecyclerView 实现 24 张牌 | `GameActivity` + `res/layout/activity_game.xml` 的 `RecyclerView @id/board` + `GridLayoutManager` |
| ③ Adapter 继承 `RecyclerView.Adapter` 并做回调处理 | `ui/CardAdapter.java`：重载 `onCreateViewHolder` / `onBindViewHolder` / `getItemCount`，内部 `OnCardClickListener` 把点击回调给 Activity |
| ④ Model 增加 `reset` 方法 | `CardMatchingGame.reset()` / `reset(int count)` |
| ⑤ 支持横竖屏不同牌数 | `GameActivity.onConfigurationChanged` + `AndroidManifest` 的 `configChanges="orientation\|screenSize\|keyboardHidden"` |
| ⑥ Model 实现 3 张牌游戏 | `CardMatchingGame.chooseCardAtIndex`（凑满 3 张结算）+ `Card.match`（另 2 张点数全同才算配对） |

## 功能

| 界面 | 功能 |
| --- | --- |
| MainActivity | 显示姓名（默认 Komeiji Koishi，性别女）、电话、最高分；ActionBar 右上角菜单「关于」→ 弹出关于对话框；两个入口按钮 |
| EditActivity | 修改姓名 / 性别 / 电话，保存后返回主界面刷新（仅存内存，不落盘） |
| GameActivity | RecyclerView 翻牌配对：**竖屏 4 列 × 6 行 = 24 张，横屏 6 列 × 2 行 = 12 张**；实时显示分数、最高分、已配对数，可重开一局，旋转屏幕自动换布局并重发牌 |

发牌规则：`Deck.drawMatchedCards(count, 3)` 保证每个点数正好 **3 张**（24 张 = 8 组 × 3，12 张 = 4 组 × 3），所以整局一定可以全部配对完成，不会出现落单的牌。

游戏规则（3 张牌模式）：

* 每翻开一张牌 **−1** 分；
* 一次凑满 3 张且**点数全部相同**（花色无关）才算配对成功，三张一起锁定并 **+4 × 3 = 12** 分；
* 3 张点数不同算错配，**−2 × 2 = −4** 分，三张牌先展示约 0.75 秒再一起翻回去；
* 已配对的牌不可再点，错配展示期间锁住点击。

## 目录

```
app/src/main/java/cn/edu/sicnu/
├── cardgame/                 # 模型层（Card / Deck / CardMatchingGame）
└── cs/stu/baixinyuan/azshiyan5/
    ├── MainActivity.java     # 主界面（含「关于」菜单）
    ├── EditActivity.java     # 信息修改
    ├── GameActivity.java     # 翻牌游戏（控制器：RecyclerView + 横竖屏 + 错误翻回）
    ├── data/                 # Player / PlayerStore（内存单例）
    ├── ui/CardView.java      # 自绘卡牌（背面斜纹、正面点数花色、翻转动画）
    ├── ui/CardAdapter.java   # RecyclerView 适配器（含点击回调）
    └── util/SystemBars.java  # Android 15+ 边到边适配
app/src/main/res/
├── layout/{activity_main,activity_edit,activity_game,item_card}.xml
├── menu/main_menu.xml        # 溢出菜单「关于」
└── values/{strings,colors,dimens,themes}.xml
app/src/test/java/.../CardMatchingGameTest.java   # 12 个模型单元测试（3 张牌规则）
```

## 构建与运行

```bash
./gradlew :app:testDebugUnitTest   # 跑单元测试（12 个用例）
./gradlew :app:assembleDebug       # 生成 app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n cn.edu.sicnu.cs.stu.baixinyuan.azshiyan5/.MainActivity
```

* minSdk 24 / targetSdk 37 / JDK 17
* 主题：`Theme.MaterialComponents.DayNight.DarkActionBar.Bridge`，日间/夜间两套配色（`values-night/`）
* 依赖：`androidx.recyclerview:recyclerview:1.3.2`、`com.google.android.material:material:1.10.0`
* 所有界面文字都在 `res/values/strings.xml`，布局与代码中**没有硬编码字符串**

## 分支与提交（由本人操作）

```bash
git checkout -b Dev          # 在 Dev 分支上提交
git add .
git commit -m "实验五：RecyclerView 翻牌游戏"
git push -u origin Dev       # 然后在 gitee 上发起 Pull Request 合并到 Main
```
