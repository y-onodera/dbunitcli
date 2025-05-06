# コマンドライン引数の連携

このドキュメントでは、フロントエンドとバックエンドの間でのコマンドライン引数の受け渡しについて説明します。

## 1. 引数の定義

### 1.1 Tauri設定 (tauri.conf.json)
```json
{
  "plugins": {
    "cli": {
      "args": [
        {"name": "port", "takesValue": true},
        {"name": "workspace", "takesValue": true},
        {"name": "dataset.base", "takesValue": true},
        {"name": "result.base", "takesValue": true}
      ]
    }
  }
}
```

### 1.2 フロントエンド型定義 (EnviromentProvider.tsx)
```typescript
export type Enviroment = {
  apiUrl: string;
  workspace: string;
  dataset_base: string;
  result_base: string;
  loaded: boolean;
};
```

## 2. 値の伝播

### 2.1 起動時の処理
1. Tauriが起動引数を解析
2. Rustがプラグインから値を取得
3. Javaプロセスに引数として受け渡し
4. フロントエンドがEnviromentProviderで値を保持

### 2.2 変換マッピング
```rust
// Rustでの変換
add_arg(&matches, &mut args, "port", "-Dmicronaut.server.port=", "8080");
add_arg(&matches, &mut args, "workspace", "-Dyo.dbunit.cli.workspace=", ".");
add_arg(&matches, &mut args, "dataset.base", "-Dyo.dbunit.cli.dataset.base=", ".");
add_arg(&matches, &mut args, "result.base", "-Dyo.dbunit.cli.result.base=", ".");
```

## 3. フロントエンドでの利用

### 3.1 環境情報の取得
```typescript
const environment = useEnviroment();
const workspace = environment.workspace;
const datasetBase = environment.dataset_base;
const resultBase = environment.result_base;
```

### 3.2 API URLの構築
```typescript
const apiUrl = `${`http://localhost:${port}` as string}/dbunit-cli/`;
```

## 4. バックエンドでの利用

### 4.1 Javaプロセスへの受け渡し
```rust
Command::new("backend/dbunit-cli-sidecar.exe")
    .args(args)  // コマンドライン引数を含む
    .spawn()
```

### 4.2 システムプロパティ変換
- port → micronaut.server.port
- workspace → yo.dbunit.cli.workspace
- dataset.base → yo.dbunit.cli.dataset.base
- result.base → yo.dbunit.cli.result.base

## 5. デフォルト値

### 5.1 値の優先順位
1. コマンドライン引数
2. 環境変数
3. デフォルト値
   - port: 8080
   - workspace: "."
   - dataset.base: "."
   - result.base: "."