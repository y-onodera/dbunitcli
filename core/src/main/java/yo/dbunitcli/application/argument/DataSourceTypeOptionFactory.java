package yo.dbunitcli.application.argument;

import yo.dbunitcli.dataset.DataSourceType;

public class DataSourceTypeOptionFactory {

    public ComparableDataSetParamOption create(String prefix, DataSourceType type) {
        switch (type) {
            case file:
                return ComparableDataSetParamOption.join(new ExtensionOption(prefix));
            case table:
                return ComparableDataSetParamOption.join(new EncodingOption(prefix), new JdbcLoadOption(prefix));
            case sql:
                return ComparableDataSetParamOption.join(new EncodingOption(prefix), new JdbcLoadOption(prefix), new TemplateRenderOption(prefix));
            case xls:
            case xlsx:
                return new ExcelOption(prefix);
            case reg:
                return ComparableDataSetParamOption.join(new EncodingOption(prefix), new RegexOption(prefix), new HeaderNameOption(prefix));
            case fixed:
                return ComparableDataSetParamOption.join(new EncodingOption(prefix), new FixedOption(prefix), new HeaderNameOption(prefix));
            case csv:
                return ComparableDataSetParamOption.join(new EncodingOption(prefix), new HeaderNameOption(prefix),new CsvOption(prefix));
            case csvq:
                return ComparableDataSetParamOption.join(new EncodingOption(prefix), new TemplateRenderOption(prefix));
        }
        return ComparableDataSetParamOption.NONE;
    }

}
