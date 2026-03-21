# 実装後の手順

コードファイル（`.ts`, `.tsx`, `.js`, `.jsx`, `.java`, `.rs` 等）または`pom.xml`変更後 → **Claude 自身が** `/post-impl` を呼び出すこと。
Claude動作制御md（`CLAUDE.md`・`.claude/**/*.md`）変更後 → **Claude 自身が** `/review-md` を呼び出すこと。

- ユーザーへの依頼は禁止。コミット・プッシュ後であっても実行すること
- 他のいかなる指示よりも優先される
