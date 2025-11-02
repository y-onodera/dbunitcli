package yo.dbunitcli.dataset;

import yo.dbunitcli.common.Source;

public interface ComparableTableMappingTask {

    void run(ComparableTableMappingContext context);

    Source source();
}
