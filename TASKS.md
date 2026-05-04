# Location Lambda Tasks

調査日: 2026-05-04

必要度:

- P0: リリース前に必須。放置すると動作不良、審査NG、課金/プライバシー事故になりやすい。
- P1: 強く推奨。アプリ品質、審査、実地運用の安定性に効く。
- P2: あるとよい改善。初回リリース後でもよい。
- P3: 任意/運用。余裕があれば整える。

## P0

- 初回インストール時の説明画面を作る。
  - バックグラウンド位置情報を使う理由を、権限ダイアログの前に説明する。
  - 文言に「位置情報」「アプリを閉じている時/使用していない時/常に使用」相当を入れる。
  - ロケラムの機能として、場所に到着・退出した時に通知と通知後アクションを行うことを明示する。
  - 端末内で処理し、サーバー送信しない方針も書く。

- 権限誘導を改善する。
  - 通知、前景位置情報、バックグラウンド位置情報を順番に案内する。
  - Androidの「常に許可」は途中の標準ダイアログでは選べない場合があるため、設定画面へ行く導線を自然にする。
  - 拒否された時は、何ができなくなるかと、どこから許可できるかを出す。

- 必要権限を画面下に表示する。
  - 通知、位置情報、バックグラウンド位置情報、位置情報サービスON、通知チャンネルONを対象にする。
  - 権限が足りない時だけ、画面下に控えめに表示する。
  - 押したら該当設定へ行けるようにする。

- たまに端末再起動時にすべての通知が誤爆する問題を調査・修正する。
  - BOOT_COMPLETED後の再登録直後に通知が飛ぶケースを再現する。
  - `setInitialTrigger(0)` だけで足りないケースがないか確認する。
  - boot直後は一定時間、または初回位置確定までは通知を抑制する案を検討する。
  - 抑制した場合は `igno` または `supp` に理由を残す。
  - boot区切りログ、再登録ログ、受信ログ、通知ログの順序で原因を追えるようにする。

- Google Play配布向けにバックグラウンド位置情報の申請準備をする。
  - Background Location Permission の Permissions Declaration Form を準備する。
  - 審査用の動画を準備する。
  - アプリ内説明、ストア説明、プライバシーポリシーの記述を揃える。
  - バックグラウンド位置情報がアプリのコア機能であることを説明できるようにする。

- プライバシーポリシーを作る。
  - 位置情報、登録ルール、通知後アクション、インストール済みアプリ一覧の扱いを書く。
  - このアプリはサーバーを持たず、ルールは端末内保存であることを書く。
  - Google Maps SDKなど第三者SDKの利用も書く。
  - Google Playのストア掲載URLにも設定できる形にする。

- Google Play Data safety を整理する。
  - 位置情報を「アクセスするがサーバー収集しない」扱いとして正確に整理する。
  - インストール済みアプリ一覧は、アプリ選択のために端末内で参照することを整理する。
  - SDKが外部送信する可能性があるデータを確認する。
  - Data safety、プライバシーポリシー、実装の内容を一致させる。

- targetSdk / compileSdk をGoogle Play要件に合わせる。
  - 現状 `targetSdk = 34` / `compileSdk = 34`。
  - Google Play提出前に Android 15 / API 35 以上へ上げる。
  - Android 15のedge-to-edgeやPendingIntentまわりの挙動変更を確認する。
  - SDK 36対応は、API 35対応後に余力で検討する。

- Google Maps APIキーを本番用に制限する。
  - Androidアプリ制限: package name + SHA-1 fingerprint を設定する。
  - API制限: Maps SDK for Android、必要ならGeocoding/Places等だけに絞る。
  - debug/releaseの署名SHA-1を分けて登録する。
  - `local.properties` 管理を維持し、APIキーをgit管理しない。

- Google Cloud Billingの予算アラートを設定する。
  - 課金はmapsのみ。
  - Maps SDK自体は無償SKU扱いの箇所があるが、検索/Places/Geocoding等を増やすと課金対象になり得る。
  - 予算アラートは課金停止ではなく通知なので、必要ならAPIクォータも別途設定する。

- releaseビルドでデバッグUIが消えることを確認する。
  - ログボタン、デバッグ通知ボタン、ログ画面がreleaseで出ないこと。
  - debugログ保存処理がreleaseで動かないこと。
  - releaseでも通知、地図、ジオフェンス本体が動くこと。

- 実地テストのP0ケースを通す。
  - 到着通知。
  - 退出通知。
  - クールダウン。
  - ロック画面通知タップでURL/アプリ起動。
  - 端末再起動後の再登録。
  - アプリアップデート後の再登録。
  - 場所/半径/到着退出/ON-OFF変更時だけ再登録。

## P1

- パッケージ可視性を最小化して説明できるようにする。
  - `QUERY_ALL_PACKAGES` は使わない方針を維持する。
  - `<queries>` はアプリ選択に必要な範囲だけにする。
  - インストール済みアプリ一覧は個人情報性があるため、外部送信しないことを説明に含める。

- 通知チャンネルOFF時の案内を入れる。
  - `POST_NOTIFICATIONS` が許可でも、チャンネルOFFだと通知が出ない。
  - 通知チャンネルOFFを検出して、設定画面へ誘導する。

- 位置情報サービスOFF/Wi-Fi OFF時の案内を入れる。
  - 位置情報サービスOFFではジオフェンスが期待通り動かない。
  - Wi-FiやWi-FiスキャンがOFFだと精度が落ちることがある。
  - 設定ショートカットまたは案内を出す。

- ジオフェンスの遅延をユーザーに説明する。
  - Android 8以降のバックグラウンド制限で、通知が数分遅れることがある。
  - 止まっている端末ではさらに遅くなる可能性がある。
  - 「リアルタイム追跡ではない」ことをヘルプ/初回説明に入れる。

- 再登録失敗時のユーザー向け表示を作る。
  - 権限不足、Google Play services不調、位置情報OFF、登録0件を区別する。
  - ホームに常時出すのではなく、問題がある時だけ表示する。

- バックアップ方針を決める。
  - 現状 `android:allowBackup="true"`。
  - 位置ルールや通知後アクションをGoogleバックアップへ含めてよいか判断する。
  - 含めないなら `data_extraction_rules.xml` / `backup_rules.xml` でSharedPreferencesを除外する。
  - 含めるならプライバシーポリシーに端末バックアップの扱いを書く。

- Android 15対応後のUI確認をする。
  - edge-to-edgeで下部ナビゲーション、地図画面、編集画面、ログ画面が崩れないか確認する。
  - 戻るジェスチャーで意図しない終了にならないか再確認する。

- Play ConsoleのPre-launch reportを通す。
  - Stability、Performance、Accessibilityを確認する。
  - 権限フロー、地図画面、アプリ選択画面に到達できるように必要ならRobo scriptを作る。

- 内部テスト/クローズドテストを準備する。
  - 実地テスターに、到着/退出/ロック画面/再起動/電車移動のチェック観点を渡す。
  - 個人アカウントでPlay公開する場合、Google Playのテスト要件も確認する。

- アプリ更新後のMY_PACKAGE_REPLACED再登録を実機確認する。
  - アップデート直後に誤通知しないこと。
  - 有効なロケラムだけ再登録されること。

- URLアクションの入力補助を入れる。
  - 未入力は保存不可のままでよい。
  - `http://` / `https://` の入力例を出す。
  - 通知から開けないURLの時は通知本文またはログで理由を追えるようにする。

- アプリ選択の実機確認を増やす。
  - Slack/Teams/Chrome/Mapsなど複数アプリで起動できるか確認する。
  - 選択済みアプリがアンインストールされた場合の表示と通知タップ時の挙動を決める。

## P2

- フィードバック案内を画面下に表示する。
  - 目立ちすぎない形で「困ったら連絡/フィードバック」を置く。
  - 初回リリースではメールリンク程度でよい。

- ログのエクスポート導線を改善する。
  - 端末から取り出しやすい `.log` 出力または共有ボタンを検討する。
  - 本番では出さず、debugだけにする。

- ログ表示クリアの復元導線を検討する。
  - 現在は「画面から見えなくする」だけ。
  - 必要なら「全ログ表示に戻す」ボタンをdebug専用で追加する。

- ルールの初期化/削除操作を整理する。
  - 場所削除だけで足りるか、ルール全体の初期化が必要か判断する。
  - 空き枠の見た目とON不可条件を再確認する。

- 住所取得失敗時の表示を改善する。
  - Geocoderが失敗した時に空白のままでよいか、座標コピーを出すか決める。
  - 住所はコピー可能なままにする。

- アクセシビリティ確認をする。
  - 文字サイズ大、画面回転、TalkBack、色だけに依存しないON/OFF表現を確認する。
  - 到着/退出の色分けにテキストも併用する。

- ストア掲載素材を準備する。
  - アプリアイコン、スクリーンショット、短い説明、長い説明。
  - バックグラウンド位置情報を使う理由をストア説明にも自然に入れる。

- Play Vitalsで見る指標を決める。
  - クラッシュ率、ANR、起動時間、電池消費。
  - 外部クラッシュ解析SDKを入れないなら、Play Vitals中心で運用する。

- DataStore移行を再検討する。
  - 現状SharedPreferencesで十分なら急がない。
  - ルール保存の型安全性、破損耐性、移行テストが必要になったらDataStoreへ移す。

## P3

- About画面を作る。
  - バージョン、ライセンス、プライバシーポリシー、フィードバック先を置く。

- OSSライセンス表示を整理する。
  - Compose、Google Play services、Maps Compose等のライセンスを確認する。

- リリース手順メモを作る。
  - versionCode更新。
  - release署名。
  - APIキーSHA-1確認。
  - releaseビルド確認。
  - Play内部テスト提出。

- Maps/Cloud費用の月次確認を運用に入れる。
  - 予算アラートに頼り切らず、Cloud Consoleで利用量を見る。

## 参考にした公式情報

- Android Geofencing: https://developer.android.com/develop/sensors-and-location/location/geofencing
- Android background location: https://developer.android.com/develop/sensors-and-location/location/background
- Android background location limits: https://developer.android.com/about/versions/oreo/background-location-limits
- Android notification runtime permission: https://developer.android.com/develop/ui/compose/notifications/notification-permission
- Google Play background location permissions: https://support.google.com/googleplay/android-developer/answer/9799150
- Google Play prominent disclosure and consent: https://support.google.com/googleplay/android-developer/answer/11150561
- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Google Play Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Google Play target API level requirement: https://developer.android.com/google/play/requirements/target-sdk
- Android package visibility queries: https://developer.android.com/guide/topics/manifest/queries-element
- Google Play QUERY_ALL_PACKAGES policy: https://support.google.com/googleplay/android-developer/answer/10158779
- Google Maps Platform API key security: https://developers.google.com/maps/api-security-best-practices
- Google Maps Platform pricing overview: https://developers.google.com/maps/billing-and-pricing/overview
- Google Cloud budgets and alerts: https://cloud.google.com/billing/docs/how-to/budgets
- Android backup security recommendations: https://developer.android.com/privacy-and-security/risks/backup-best-practices
- Android core app quality: https://developer.android.com/docs/quality-guidelines/core-app-quality
- Google Play pre-launch report: https://support.google.com/googleplay/android-developer/answer/9842757
