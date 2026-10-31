package yo.dbunitcli.dataset;

import java.util.*;

/**
 * 1回の読込（コンテキスト）で書き込んだテーブルの行数と、分割の途中状態、テーブルごとの行を入れたタスク。
 * 同じテーブル名に複数のソースから書き込む場合に、前のソースの続きから書き込み・分割する
 */
public final class WrittenTables {

    private final Map<String, Integer> rows = new HashMap<>();

    private final Map<SplitKey, SplitState> splits = new HashMap<>();

    private final Map<String, String> splitTables = new HashMap<>();

    private final Map<SplitKey, PendingSplit> pendingSplits = new LinkedHashMap<>();

    /**
     * テーブル（分割後のテーブルは分割のまとまり）ごとの、行を入れたタスク（入れた順）
     */
    private final Map<String, Set<ComparableTableMappingTask>> sources = new HashMap<>();

    /**
     * これから書くテーブルに行を入れるタスク。記録しない読込では空
     */
    private Set<ComparableTableMappingTask> reading = Set.of();

    public boolean contains(final String tableName) {
        return this.rows.containsKey(tableName);
    }

    public int rows(final String tableName) {
        return this.rows.getOrDefault(tableName, 0);
    }

    public void put(final String tableName, final int rows) {
        this.rows.put(tableName, rows);
    }

    /**
     * 分割後の1つ目のテーブル名と分割の設定ごとの分割の途中状態。初めてなら1つ目のテーブルから数え始める
     */
    public SplitState split(final String firstTableName, final TableSplitter splitter) {
        return this.splits.computeIfAbsent(new SplitKey(firstTableName, splitter), _ -> SplitState.start(splitter));
    }

    /**
     * 行を保持して分割するテーブルの、分割を待っている行。分割後の1つ目のテーブル名と分割の設定ごとに、ソースをまたいでためる
     */
    PendingSplit pendingSplit(final String firstTableName, final TableSplitter splitter) {
        return this.pendingSplits.computeIfAbsent(new SplitKey(firstTableName, splitter), _ -> new PendingSplit(firstTableName, splitter));
    }

    /**
     * 分割を待っている行をすべて取り出す（取り出した分は残さない）
     */
    List<PendingSplit> takePendingSplits() {
        final List<PendingSplit> result = new ArrayList<>(this.pendingSplits.values());
        this.pendingSplits.clear();
        return result;
    }

    /**
     * 分割後のテーブルを、どの分割のまとまり（分割後の1つ目のテーブル名）に属するかとともに記録する
     */
    void putSplitTable(final String tableName, final String firstTableName) {
        this.splitTables.put(tableName, firstTableName);
    }

    /**
     * これから書くテーブルに行を入れるタスクを設定し、それまでのタスクを返す。
     * joinの出力では、joinの左右のテーブルに行を入れたタスクを設定する
     */
    Set<ComparableTableMappingTask> reading(final Collection<ComparableTableMappingTask> tasks) {
        final Set<ComparableTableMappingTask> previous = this.reading;
        this.reading = new LinkedHashSet<>(tasks);
        return previous;
    }

    /**
     * テーブル（分割するテーブルは分割のまとまり＝分割後の1つ目のテーブル名）に、今読んでいるタスクが行を入れた
     */
    void wrote(final String tableNameOrSplitGroup) {
        if (!this.reading.isEmpty()) {
            this.sources.computeIfAbsent(tableNameOrSplitGroup, _ -> new LinkedHashSet<>()).addAll(this.reading);
        }
    }

    /**
     * テーブルに行を入れたタスク（入れた順）。分割後のテーブルは、分割のまとまりに行を入れた全タスク
     * （分割はタスクをまたいで数えるため、一部のタスクだけを読むと分割後のテーブルの行が変わる）
     */
    List<ComparableTableMappingTask> sources(final String tableName) {
        return List.copyOf(this.sources.getOrDefault(this.splitTables.getOrDefault(tableName, tableName), Set.of()));
    }

    private record SplitKey(String firstTableName, TableSplitter splitter) {
    }

    public static final class SplitState {
        private final TableSplitter.Counter counter;
        private int no;

        private SplitState(final TableSplitter.Counter counter) {
            this.counter = counter;
        }

        /**
         * 他と共有しない、1つ目のテーブルから数え始める状態
         */
        public static SplitState start(final TableSplitter splitter) {
            return new SplitState(splitter.counter());
        }

        public TableSplitter.Counter counter() {
            return this.counter;
        }

        /**
         * 今書き込んでいる分割後のテーブルの番号
         */
        public int no() {
            return this.no;
        }

        public void next() {
            this.no++;
        }
    }
}
