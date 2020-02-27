package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.io.File;

public class ComparableCSVDataSet extends AbstractComparableDataSet {

    private final String srcDir;

    public ComparableCSVDataSet(File aSrcDir, String aEncoding) throws DataSetException {
        this(ComparableDataSetLoaderParam.builder().setSrc(aSrcDir).setEncoding(aEncoding).build());
    }

    public ComparableCSVDataSet(ComparableDataSetLoaderParam param) throws DataSetException {
        super(new ComparableCsvDataSetProducer(param), param);
        this.srcDir = param.getSrc().getPath();
    }

    @Override
    public String getSrc() {
        return srcDir;
    }

}
