package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.dto.ImageCompareDto;

public class CompareDto extends CommandDto {

    @CommandLine.Option(names = "-setting", description = "file comparison settings")
    private String setting;

    @CommandLine.Option(names = "-settingEncoding", description = "settings encoding")
    private String settingEncoding = System.getProperty("file.encoding");

    @CommandLine.Option(names = "-targetType")
    private CompareOption.Type targetType = CompareOption.Type.data;

    private ImageCompareDto imageOption = new ImageCompareDto();

    private DataSetLoadDto expectData = new DataSetLoadDto();

    private DataSetLoadDto oldData = new DataSetLoadDto();

    private DataSetLoadDto newData = new DataSetLoadDto();

    private DataSetConverterDto convertResult = new DataSetConverterDto();

    public DataSetConverterDto getConvertResult() {
        return this.convertResult;
    }

    public void setConvertResult(final DataSetConverterDto convertResult) {
        this.convertResult = convertResult;
    }

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

    public ImageCompareDto getImageOption() {
        return this.imageOption;
    }

    public void setImageOption(final ImageCompareDto imageOption) {
        this.imageOption = imageOption;
    }

    public DataSetLoadDto getExpectData() {
        return this.expectData;
    }

    public void setExpectData(final DataSetLoadDto expectData) {
        this.expectData = expectData;
    }

    public DataSetLoadDto getOldData() {
        return this.oldData;
    }

    public void setOldData(final DataSetLoadDto oldData) {
        this.oldData = oldData;
    }

    public DataSetLoadDto getNewData() {
        return this.newData;
    }

    public void setNewData(final DataSetLoadDto newData) {
        this.newData = newData;
    }

}
