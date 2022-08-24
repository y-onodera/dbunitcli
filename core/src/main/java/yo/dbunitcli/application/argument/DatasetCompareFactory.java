package yo.dbunitcli.application.argument;

import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.compare.DataSetCompareBuilder;

public class DatasetCompareFactory {

    public DataSetCompareBuilder create(String prefix, DataSourceType type) {
        return new DataSetCompareBuilder();
    }
}
