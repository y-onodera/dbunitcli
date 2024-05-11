package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.DataSourceType;

public class DataSourceTypeOptionFactory {

    public ComparableDataSetParamOption create(final String prefix, final DataSourceType type, final DataSetLoadDto dto) {
        return switch (type) {
            case table ->
                    ComparableDataSetParamOption.join(new EncodingOption(prefix, dto), new JdbcLoadOption(prefix, dto));
            case sql -> ComparableDataSetParamOption.join(new EncodingOption(prefix, dto)
                    , new JdbcLoadOption(prefix, dto)
                    , new TemplateRenderOption(prefix, dto.getTemplateRender()));
            case csv -> ComparableDataSetParamOption.join(new EncodingOption(prefix, dto)
                    , new HeaderNameOption(prefix, dto)
                    , new CsvOption(prefix, dto)
                    , new ExtensionOption(prefix, dto)
                    , new RecursiveOption(prefix, dto));
            case xls, xlsx -> ComparableDataSetParamOption.join(new ExcelOption(prefix, dto)
                    , new ExtensionOption(prefix, dto)
                    , new RecursiveOption(prefix, dto));
            case file -> ComparableDataSetParamOption.join(new ExtensionOption(prefix, dto)
                    , new RecursiveOption(prefix, dto));
            case dir -> new RecursiveOption(prefix, dto);
            case reg -> ComparableDataSetParamOption.join(new EncodingOption(prefix, dto)
                    , new RegexOption(prefix, dto)
                    , new HeaderNameOption(prefix, dto)
                    , new RecursiveOption(prefix, dto));
            case fixed -> ComparableDataSetParamOption.join(new EncodingOption(prefix, dto)
                    , new FixedOption(prefix, dto)
                    , new HeaderNameOption(prefix, dto)
                    , new RecursiveOption(prefix, dto));
            case csvq -> ComparableDataSetParamOption.join(new EncodingOption(prefix, dto)
                    , new TemplateRenderOption(prefix, dto.getTemplateRender()));
            case none -> new NoneOption();
        };
    }
}
