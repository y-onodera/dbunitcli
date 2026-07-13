---
description: scaffoldコマンド（core）修正時の設計ガイド。target追加・雛型生成ロジック変更時に参照。
TRIGGER when: command/Scaffold*.java の変更、またはscaffoldのtarget追加を伴う実装タスクの開始時。
DO NOT TRIGGER when: 読み取りのみ、または他コマンド（Compare/Convert/Generate/Run/Parameterize）のみの変更。
---

Scaffoldは、Generateの固定成果物型（`isFixedTemplate()=true`）を`generateType=txt/xlsx/xls`でカスタマイズ生成できるよう、setting/template/dataset/parameterの雛型を書き出すコマンド。`-target`対応は現状`ddl`/`javaBean`/`xlsxSchema`/`fixedColumnDef`/`parameter`のみ。target追加はGenerate側の設計（`generate-command`スキルの4軸）に従属する。

## target追加可否を左右する3つの観点

1. **テンプレートが期待する`row`の意味が「列定義」か「実データレコード」か**: Scaffoldが作れるダミーdatasetは、対象テーブルの列名（と一部の実メタデータ、xlsxSchemaの`IS_PK`等）から生成した「1テーブル=1行/カラムの列定義行」のみで、実データの行そのものは持たない。組み込みテンプレートが`rows`をそのまま「列定義の一覧」として読む型（ddl/javaBean/xlsxSchema/fixedColumnDefの`row.COLUMN_NAME`等）はダミー行がそのまま意味を持つ。逆に`sql`のように`rows`を「対象テーブルの実レコード」として読み値をSQLへ埋め込むテンプレートは、他条件を満たしても対象外（列定義行を渡しても中身のないSQLにしかならない）。`loadData`/`useJdbcMetaData`（`generate-command`参照）はddl/javaBean/sqlで同一設定のため判別材料にならない点に注意
2. **`getFixedUnit()`がtable**: `-unitSetting`分割は`unit=table`でのみ効く。`dataset`固定（settings/xlsxTemplate）はまずGenerate側の変更を検討（`xlsxSchema`の`dataset`→`table`変更が前例）
3. **カラム単位データを最終形へ変換するロジックの所在**: `defaultSettingsPath()`の宣言的JSON（ddl/javaBean）ならコピーするだけで無改造流用できる。`wrapProducer()`側のJavaコード（xlsxSchema/fixedColumnDefの`ComparableXlsxSchemaMetaDataProducer`等）はgenerateType=txtには引き継げないが、そのproducerクラス自体をScaffold側から直接インスタンス化して再利用できる（専用サンプルunitSetting・専用ScaffoldTemplateは引き続き必要）

1,2が○かつ3がJSON側なら組み込みを完全無改造流用できる。3がwrapProducer()側ならproducerクラスの直接再利用＋専用実装が要る。1が✕なら土台からGenerate側の再設計が必要。判定の詳細・前例は`references/scaffold-targets.md`。

## target1つが持つ4種の雛型

setting（`defaultSettingsPath()`がある型のみ意味を持つ）/ template（組み込みor専用stg,txtをコピー）/ dataset（列名からダミー行、形はテンプレートに合わせる）/ parameter（基本`generateType=txt -unit=table`で再現、形が変わる場合のみ専用ロジック）。詳細は`references/scaffold-targets.md`。

## 実装チェックリスト

変更ファイル一覧・target別対応表は `references/scaffold-targets.md` 参照。

## 関連スキル

Generate側の設計は`generate-command`参照。固定成果物のunit/write()/テンプレート形を変更する際は同名targetのScaffold側が壊れないか要確認。
