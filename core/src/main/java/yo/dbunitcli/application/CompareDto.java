package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.cli.CommandDto;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.dto.ImageCompareDto;

public class CompareDto extends CommandDto {

    @CommandLine.Option(names = "-setting", description = "file comparison settings")
    private String setting;

    @CommandLine.Option(names = "-settingEncoding", description = "settings encoding")
    private String settingEncoding = System.getProperty("file.encoding");

    @CommandLine.Option(names = "-targetType")
    private CompareOption.Type targetType = CompareOption.Type.data;

    private ImageCompareDto imageCompare = new ImageCompareDto();

    private DataSetLoadDto expectDataSetLoad = new DataSetLoadDto();

    private DataSetLoadDto oldDataSetLoad = new DataSetLoadDto();

    private DataSetLoadDto newDataSetLoad = new DataSetLoadDto();

    public String getSetting() {
        return this.setting;
    }

    public void setSetting(final String setting) {
        this.setting = setting;
    }

    public String getSettingEncoding() {
        return this.settingEncoding;
    }

    public void setSettingEncoding(final String settingEncoding) {
        this.settingEncoding = settingEncoding;
    }

    public CompareOption.Type getTargetType() {
        return this.targetType;
    }

    public void setTargetType(final CompareOption.Type targetType) {
        this.targetType = targetType;
    }

    public ImageCompareDto getImageCompare() {
        return this.imageCompare;
    }

    public void setImageCompare(final ImageCompareDto imageCompare) {
        this.imageCompare = imageCompare;
    }

    public DataSetLoadDto getExpectDataSetLoad() {
        return this.expectDataSetLoad;
    }

    public void setExpectDataSetLoad(final DataSetLoadDto expectDataSetLoad) {
        this.expectDataSetLoad = expectDataSetLoad;
    }

    public DataSetLoadDto getOldDataSetLoad() {
        return this.oldDataSetLoad;
    }

    public void setOldDataSetLoad(final DataSetLoadDto oldDataSetLoad) {
        this.oldDataSetLoad = oldDataSetLoad;
    }

    public DataSetLoadDto getNewDataSetLoad() {
        return this.newDataSetLoad;
    }

    public void setNewDataSetLoad(final DataSetLoadDto newDataSetLoad) {
        this.newDataSetLoad = newDataSetLoad;
    }

}
