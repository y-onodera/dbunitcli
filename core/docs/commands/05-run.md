# Runコマンド

## 基本機能
様々なタイプのスクリプト（SQL、バッチファイル、Antスクリプトなど）を実行します。テンプレート機能と組み合わせることで、動的なスクリプト実行が可能です。

## 引数
* -scriptType: スクリプトタイプ(cmd/bat/sql/ant)
* src.* : 実行するスクリプトファイルの設定 - [データソース設定](../options/02-data-source.md)

## スクリプトタイプ別の設定

### cmd/bat: コマンド/バッチスクリプト実行
* -baseDir: 実行時の基準ディレクトリ
* src.srcType: 自動的に'file'に設定
* src.extension: スクリプトの拡張子（cmdまたはbat）

### sql: SQL実行
* jdbc.*: [データベース接続設定](../options/03-jdbc.md)
* template.*: [SQLテンプレート設定](../options/05-template.md#templaterenderoption-template)
* src.srcType: 自動的に'file'に設定
* src.extension: 自動的に'sql'に設定

### ant: Ant実行
* ant.target: 実行するAntターゲット
* -baseDir: Ant実行時の基準ディレクトリ
* src.srcType: 自動的に'file'に設定
* src.extension: 自動的に'xml'に設定

## 使用例
```bash
# SQLファイルの実行
dbunit run -scriptType sql \
  -src.src "query.sql" \
  -jdbc.url "jdbc:mysql://localhost/testdb" \
  -jdbc.user "root" -jdbc.pass "password"

# Antスクリプトの実行
dbunit run -scriptType ant \
  -src.src "build.xml" \
  -ant.target "compile" \
  -baseDir "./project"
```

## 注意事項
- スクリプト実行時は適切な権限が必要です
- データベース操作を含むスクリプトは慎重に実行してください
- 長時間実行されるスクリプトの場合、タイムアウト設定を考慮してください