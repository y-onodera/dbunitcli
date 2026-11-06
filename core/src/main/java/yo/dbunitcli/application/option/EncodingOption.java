package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.nio.charset.Charset;
import java.util.List;

public record EncodingOption(String prefix, String encoding) implements ComparableDataSetParamOption {

    public EncodingOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, EncodingOption.orDefault(dto.getEncoding()));
    }

    /**
     * 文字コードのオプションが空ならシステムの既定の文字コードにする
     */
    public static String orDefault(final String encoding) {
        return EncodingOption.orDefault(encoding, Charset.defaultCharset());
    }

    public static String orDefault(final String encoding, final Charset defaultCharset) {
        return Strings.isNotEmpty(encoding) ? encoding : defaultCharset.displayName();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix())
                .put("-encoding", this.encoding);
    }

    @Override
    public List<Violation> validate() {
        return this.validateEncoding("-encoding", this.encoding);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setEncoding(this.encoding);
    }

}
