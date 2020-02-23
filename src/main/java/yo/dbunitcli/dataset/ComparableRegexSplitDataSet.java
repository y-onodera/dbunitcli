package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.io.File;

public class ComparableRegexSplitDataSet extends AbstractComparableDataSet {
    private final File src;

    public ComparableRegexSplitDataSet(ComparableDataSetLoaderParam param) throws DataSetException {
        super(new ComparableRegexSplitDataSetProducer(param.getHeaderSplitPattern()
                        , param.getDataSplitPattern()
                        , param.getSrc()
                        , param.getEncoding())
                , param);
        this.src = param.getSrc();
    }

    @Override
    public String getSrc() {
        return src.toString();
    }
}