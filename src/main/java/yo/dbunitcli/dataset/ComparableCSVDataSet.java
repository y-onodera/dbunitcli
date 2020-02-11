package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.io.File;

public class ComparableCSVDataSet extends AbstractComparableDataSet {

    private final String srcDir;

    public ComparableCSVDataSet(File aSrcDir, String aEncoding) throws DataSetException {
        super(new ComparableCsvDataSetProducer(aSrcDir, aEncoding));
        this.srcDir = aSrcDir.getPath();
    }

    public ComparableCSVDataSet(ComparableDataSetLoaderParam param) throws DataSetException {
        super(new ComparableCsvDataSetProducer(param.getSrc(), param.getEncoding())
                , param.getExcludeColumns()
                , param.getOrderColumns());
        this.srcDir = param.getSrc().getPath();
    }

    @Override
    public String getSrc() {
        return srcDir;
    }

}
