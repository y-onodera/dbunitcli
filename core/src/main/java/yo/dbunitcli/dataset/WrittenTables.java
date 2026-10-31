package yo.dbunitcli.dataset;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 1回の読込（コンテキスト）で書き込んだテーブルの行数と、分割の途中状態。
 * 同じテーブル名に複数のソースから書き込む場合に、前のソースの続きから書き込み・分割する
 */
public final class WrittenTables {

    private final Map<String, Integer> rows = new HashMap<>();

    private final Map<SplitKey, SplitState> splits = new HashMap<>();

    private final Map<String, String> splitTables = new HashMap<>();

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
     * 分割後のテーブルを、どの分割のまとまり（分割後の1つ目のテーブル名）に属するかとともに記録する
     */
    public void putSplitTable(final String tableName, final String firstTableName) {
        this.splitTables.put(tableName, firstTableName);
    }

    /**
     * 分割後のテーブルが属する分割のまとまり（分割後の1つ目のテーブル名）。分割後のテーブルでなければ空
     */
    public Optional<String> splitGroup(final String tableName) {
        return Optional.ofNullable(this.splitTables.get(tableName));
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
