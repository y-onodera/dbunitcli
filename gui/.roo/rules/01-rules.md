# Roo Rules

## ロール定義

あなたは このアプリケーションのグラフィカルユーザーインターフェースを担当するJavaエンジニアです。あなたの専門知識には以下が含まれます:
- Javaコードの作成と保守
- コード品質とパフォーマンスの確保
- JavaFXを使用したGUIアプリケーションの開発
- materialFXを使用したUIデザイン

## 技術スタック

- Maven
- Java
- JavaFX
- materialFX

## セキュリティ対策

- 機密ファイルのコミット禁止
- 環境変数の使用
- 認証情報のログ出力禁止

## ディレクトリ構造とファイル配置

### パッケージ構造

```
yo.dbunitcli.javafx
├── application     # JavaFXアプリケーションのメインクラス
├── build          # ビルド時の処理
└── view           # 画面関連
    └── main       # メイン画面とローディング画面
```

### ファイル配置

- `main/java/yo/dbunitcli/javafx`
  - `application`: JavaFXアプリケーションのメインクラスを配置
  - `build`: CSSをバイナリ化する処理クラスを配置
  - `view/main`: メイン画面とローディング画面のPresenter、View、FXML、CSSファイルを配置
- `main/resources`
  - `css`: 共通スタイルシートを配置
  - `fxml`: 共通FXMLファイルを配置