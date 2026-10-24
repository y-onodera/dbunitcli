package yo.dbunitcli.dataset;

public enum DataSourceType {
    none,
    table,
    sql,
    file,
    dir,
    csv {
        @Override
        public String getExtension() {
            return this.name();
        }
    },
    csvq,
    reg,
    fixed,
    xls {
        @Override
        public String getExtension() {
            return this.name();
        }
    },
    xlsx {
        @Override
        public String getExtension() {
            return this.name();
        }
    };

    public String getExtension() {
        return null;
    }

    /**
     * ソースを並列に読み込めるか（{@link ComparableDataSetProducer#isParallelizableSource()}がtrueのproducerを使う形式）。
     * 画面でparallelを表示するかの判定に使う
     */
    public boolean isParallelizable() {
        return this == csv || this == csvq || this == reg || this == fixed || this == xls || this == xlsx;
    }
}
