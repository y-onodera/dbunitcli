package yo.dbunitcli.application.argument;

import yo.dbunitcli.dataset.DataSourceType;

public class DataSourceTypeOptionFactory {

    public ComparableDataSetParamOption create(final String prefix, final DataSourceType type) {
        return switch (type) {
            case file -> ComparableDataSetParamOption.join(new ExtensionOption(prefix), new RecursiveOption(prefix));
            case dir -> new RecursiveOption(prefix);
            case table -> ComparableDataSetParamOption.join(new EncodingOption(prefix), new JdbcLoadOption(prefix));
            case sql ->
                    ComparableDataSetParamOption.join(new EncodingOption(prefix), new JdbcLoadOption(prefix), new TemplateRenderOption(prefix));
            case xls, xlsx ->
                    ComparableDataSetParamOption.join(new ExcelOption(prefix), new ExtensionOption(prefix), new RecursiveOption(prefix));
            case reg ->
                    ComparableDataSetParamOption.join(new EncodingOption(prefix), new RegexOption(prefix), new HeaderNameOption(prefix), new RecursiveOption(prefix));
            case fixed ->
                    ComparableDataSetParamOption.join(new EncodingOption(prefix), new FixedOption(prefix), new HeaderNameOption(prefix), new RecursiveOption(prefix));
            case csv ->
                    ComparableDataSetParamOption.join(new EncodingOption(prefix), new HeaderNameOption(prefix), new CsvOption(prefix), new ExtensionOption(prefix), new RecursiveOption(prefix));
            case csvq ->
                    ComparableDataSetParamOption.join(new EncodingOption(prefix), new TemplateRenderOption(prefix));
        };
    }

}
