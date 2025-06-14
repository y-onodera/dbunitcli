# Coreプロジェクト Boomerangモード開発ルール

このドキュメントは、Boomerangモード固有の開発ルールを定義します。

## 1. オーケストレーション原則
オーケストレーターとして、`new_task`ツールを使用して`boomerang-`で始まるモードにサブタスクを委任します
1. `boomerang-architect`にtaskファイルを作らせます
2. `boomerang-code`にソースを修正させます
3. `boomerang-docs`に修正した実装をドキュメント化させます

### 1.1 タスク分析と分解
1. 各サブタスクについて、`new_task`ツールを使用して委任します。サブタスクの具体的な目標に最も適したモードを選択し、`message`パラメータで従うべきtaskファイルを指定します。
    * `boomerang-architect`に`new_task`ツールでサブタスクを委任する前に、tasks-doneディレクトリの完了済みタスクの番号を確認して、tasksに新しいTASK-{番号}のディレクトリを作成します。`boomerang-architect`には作ったディレクトリに、main.mdとサブタスクのtaskファイルを作るよう`message`パラメータで指示します
    * `boomerang-code`に`new_task`ツールでサブタスクを委任する際は、`boomerang-architect`が作成したcode用のサブタスクのtaskファイルを`message`パラメータで指示します
    * `boomerang-docs`に`new_task`ツールでサブタスクを委任する際は、`boomerang-architect`が作成したdocs用のサブタスクのtaskファイルを`message`パラメータで指示します
2. サブタスクへの指示には以下を含めます
    * taskファイルの作業のみを実行し、逸脱しないようにという明示的な指示
    * サブタスクが`attempt_completion`ツールを使用して完了を通知し、`result`パラメータに作業の実施に使ったtaskファイルと作業中に変更した内容を提供するという指示（この要約がプロジェクトで完了したことを追跡するための信頼できる情報源となることを念頭に置く）
3. すべてのサブタスクの進捗を追跡、管理します。サブタスクが完了したら、その結果を分析し、次のステップを決定します。
    * `attempt_completion`ツールの`result`パラメータに含まれるtaskファイルを`execute_command`ツールを使って`.roo/work/tasks`から`.roo/work/tasks-done`ディレクトリに移動します
    * 移動したファイルは`execute_command`ツールを使って`.roo/work/tasks`から削除します
4. 全体のワークフローにおいて、異なるサブタスクがどのように関連しているかをユーザーが理解できるようにします。特定のタスクを特定のモードに委任する理由について、明確な説明を提供します。
5. すべてのサブタスクが完了したら、結果を統合し、達成されたことの包括的な概要を提供します。

### 1.2 タスク構造

- タスクは以下の階層構造で管理します：
  ```
  work/
  ├── tasks/                     # 作業中のタスク用ディレクトリ
  │   └── TASK-{番号}/          
  │       ├── main.md           # boomerang-architectモード用メインタスク定義
  │       └── subtasks/         # サブタスク用ディレクトリ
  │           ├── code/         # boomerang-codeモード用サブタスク
  │           │   └── {番号}.md 
  │           └── docs/         # boomerang-docsモード用サブタスク
  │               └── {番号}.md
  └── tasks-done/               # 完了タスク用ディレクトリ
      └── TASK-{番号}/          # タスク完了時にtasksからここに移動
          └── [同上の構造]
  ```

### 1.3 タスクファイルの命名規則

1. タスクID
   - 形式: `TASK-{3桁の連番}`
   - 例: TASK-001, TASK-002, ...
   - 新規タスク作成時は`tasks-done`ディレクトリも確認し、未使用の最小の番号を使用

2. メインタスクファイル
   - ファイル名: `main.md`
   - タスク全体の目的、構成、完了条件を定義
   - サブタスクの分割方針と依存関係を記載

3. サブタスクファイル
   - 形式: `{番号}.md`（例：001.md）
   - モードごとのディレクトリに配置
   - サブタスク番号はタスク内で一意の連番
