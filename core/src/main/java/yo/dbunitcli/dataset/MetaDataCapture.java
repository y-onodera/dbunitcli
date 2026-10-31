package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

/**
 * 読み込んだテーブルのメタデータを受け取るconverter。分割（split）後も同じインスタンスで受け、既定では行を使わない
 */
public abstract class MetaDataCapture implements IDataSetConverter {

    @Override
    public boolean isExportEmptyTable() {
        return true;
    }

    @Override
    public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
        try {
            this.startTable(tableMetaData);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public IDataSetConverter split() {
        return this;
    }

    @Override
    public void endTable() {
        // メタデータだけを使う
    }

    @Override
    public void row(final Object[] values) {
        // メタデータだけを使う
    }
}
