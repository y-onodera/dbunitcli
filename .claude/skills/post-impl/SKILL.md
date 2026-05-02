---
description: 実装後の品質確認。変更・追加・修正されたすべてのコードファイルに /simplify を適用する。
TRIGGER when: コードファイル（.ts, .tsx, .js, .jsx, .java, .rs 等）またはpom.xmlの変更を伴うタスクの**全編集が完了した時点で1回**。
DO NOT TRIGGER when: 読み取りのみ、質問への回答のみ、計画作成のみ、コード未変更時。/simplify または /post-impl 実行中の個別Edit後。
---

変更・追加・修正されたすべてのコードファイルに対して `/simplify` を実行すること。
