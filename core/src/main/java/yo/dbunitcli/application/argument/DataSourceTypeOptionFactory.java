package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;

import java.util.Map;

public class DataSourceTypeOptionFactory {

    public ComparableDataSetParamOption create(String prefix, DataSourceType type) {
        switch (type) {
            case table:
                return new JdbcLoadOption(prefix);
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
                return ComparableDataSetParamOption.join(new EncodingOption(prefix), new HeaderNameOption(prefix));
            case csvq:
                return ComparableDataSetParamOption.join(new EncodingOption(prefix), new TemplateRenderOption(prefix));
        }
        return ComparableDataSetParamOption.NONE;
    }

    public class FixedOption extends PrefixArgumentsParser implements ComparableDataSetParamOption {
        public FixedOption(String prefix) {
            super(prefix);
        }

        @Option(name = "-fixedLength", usage = "comma separate column Lengths")
        private String fixedLength;

        @Override
        public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
            return builder.setFixedLength(this.fixedLength);
        }

        @Override
        public OptionParam expandOption(Map<String, String> args) {
            OptionParam result = super.expandOption(args);
            result.put("-fixedLength", this.fixedLength);
            return result;
        }
    }
}
