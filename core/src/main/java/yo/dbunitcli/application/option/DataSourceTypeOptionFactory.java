package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.DataSourceType;

public class DataSourceTypeOptionFactory {

    public ComparableDataSetParamOption create(final String prefix, final DataSourceType type, final DataSetLoadDto dto) {
        return switch (type) {
            case xls, xlsx -> ComparableDataSetParamOption.join(
                    new FileTraverseOption(prefix, dto)
                    , new StartRowOption(prefix, dto)
                    , new HeaderNameOption(prefix, dto)
                    , new AddFileInfoOption(prefix, dto)
                    , new ExcelOption(prefix, dto)
            );
            case csv -> ComparableDataSetParamOption.join(
                    new FileTraverseOption(prefix, dto)
                    , new EncodingOption(prefix, dto)
                    , new StartRowOption(prefix, dto)
                    , new HeaderNameOption(prefix, dto)
                    , new AddFileInfoOption(prefix, dto)
                    , new CsvOption(prefix, dto)
            );
            case reg -> ComparableDataSetParamOption.join(
                    new FileTraverseOption(prefix, dto)
                    , new EncodingOption(prefix, dto)
                    , new StartRowOption(prefix, dto)
                    , new HeaderNameOption(prefix, dto)
                    , new AddFileInfoOption(prefix, dto)
                    , new RegexOption(prefix, dto)
            );
            case fixed -> ComparableDataSetParamOption.join(
                    new FileTraverseOption(prefix, dto)
                    , new EncodingOption(prefix, dto)
                    , new StartRowOption(prefix, dto)
                    , new HeaderNameOption(prefix, dto)
                    , new AddFileInfoOption(prefix, dto)
                    , new FixedOption(prefix, dto)
            );
            case csvq -> ComparableDataSetParamOption.join(
                    new FileTraverseOption(prefix, dto)
                    , new EncodingOption(prefix, dto)
                    , new HeaderNameOption(prefix, dto)
                    , new TemplateRenderOption(prefix, dto.getTemplateRender())
            );
            case table, sql -> ComparableDataSetParamOption.join(
                    new FileTraverseOption(prefix, dto)
                    , new EncodingOption(prefix, dto)
                    , new JdbcLoadOption(prefix, dto)
                    , new HeaderNameOption(prefix, dto)
                    , new TemplateRenderOption(prefix, dto.getTemplateRender())
            );
            case dir -> new TraverseOption(prefix, dto);
            case file -> new FileTraverseOption(prefix, dto);
            case none -> new NoneOption();
        };
    }
}
