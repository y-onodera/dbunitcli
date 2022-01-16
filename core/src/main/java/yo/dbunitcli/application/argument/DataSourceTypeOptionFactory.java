package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;

public class DataSourceTypeOptionFactory {

    public ComparableDataSetParamOption create(String prefix, DataSourceType type) {
        switch (type) {
            case XLS:
            case XLSX:
                return new ExcelOption(prefix);
            case REGSP:
                return new RegexOption(prefix);
            case FIXED:
                return new FixedOption(prefix);
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
    }
}
