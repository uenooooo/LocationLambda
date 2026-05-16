# LocationLambda

場所への到着・退出をきっかけに通知し、通知からURLまたはアプリを開けるAndroidアプリです。

## 機能

- 地図から通知地点を選択
- 通知半径を100mから300mで設定
- 到着、退出、または両方をトリガーに設定
- 通知後の操作を選択
  - URLを開く
  - インストール済みアプリを開く
  - 通知のみ
- ルールごとの有効/無効、クールダウン設定
- 端末再起動・アプリ更新後のジオフェンス再登録
- debugビルドのみデバッグログ表示

## 技術構成

- Kotlin
- Jetpack Compose / Material 3
- Google Maps Compose
- Google Play services Location
- Gradle Kotlin DSL

## 動作環境

- Android 10以上
- 位置情報権限
- バックグラウンド位置情報権限
- Android 13以上は通知権限

## セットアップ

`local.properties` に Google Maps API key を設定します。

```properties
MAPS_API_KEY=your_api_key
```

Google Cloud側では Maps SDK for Android を有効化してください。

## ビルド

```powershell
.\gradlew.bat assembleDebug
```

release APK:

```powershell
.\gradlew.bat renameReleaseApk
```

生成先:

```text
app/build/outputs/apk/distribution/LocationLambda-v1.0.apk
```

release署名を使う場合は `local.properties` に追加します。

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=...
RELEASE_KEY_PASSWORD=...
```

## 注意

- 位置通知後のURL/アプリ起動は通知タップ時に行います。
- 登録できる有効なジオフェンスルールは最大5件です。
- `local.properties` と署名ファイルはGit管理しないでください。

## ライセンス

未設定
