package yo.dbunitcli.dataset;

public interface ComparableTableMapper {

    /**
     * 出力するテーブルが無い（outputFilterで全て除外された）場合のmapper。何もしない
     */
    ComparableTableMapper NONE = new ComparableTableMapper() {
        @Override
        public void startTable() {
        }

        @Override
        public void addRow(final Object[] values) {
        }

        @Override
        public void endTable() {
        }

        @Override
        public boolean hasOutput() {
            return false;
        }
    };

    void startTable();

    void addRow(Object[] values);

    void endTable();

    /**
     * falseなら行を渡しても使われないため、producerは行の読込を省略できる
     */
    default boolean hasOutput() {
        return true;
    }

}
