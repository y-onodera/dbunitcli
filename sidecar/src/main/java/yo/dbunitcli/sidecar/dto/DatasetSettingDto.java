package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;

@Serdeable
public class DatasetSettingDto {
    private List<String> name;
    private PatternDto pattern;
    private TableJoinDto innerJoin;
    private TableJoinDto outerJoin;
    private TableJoinDto fullJoin;
    private List<DatasetSettingDto> separate;
    private String prefix;
    private String tableName;
    private String suffix;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean distinct;
    private SplitDto split;
    private List<String> keys;
    @JsonProperty("string")
    private Map<String, String> stringColumns;
    @JsonProperty("number")
    private Map<String, String> numberColumns;
    @JsonProperty("boolean")
    private Map<String, String> booleanColumns;
    @JsonProperty("function")
    private Map<String, String> functionColumns;
    private List<String> exclude;
    private List<String> include;
    private List<String> filter;
    private List<String> order;

    public List<String> getName() {
        return this.name;
    }

    public void setName(final List<String> name) {
        this.name = name;
    }

    public PatternDto getPattern() {
        return this.pattern;
    }

    public void setPattern(final PatternDto pattern) {
        this.pattern = pattern;
    }

    public TableJoinDto getInnerJoin() {
        return this.innerJoin;
    }

    public void setInnerJoin(final TableJoinDto innerJoin) {
        this.innerJoin = innerJoin;
    }

    public TableJoinDto getOuterJoin() {
        return this.outerJoin;
    }

    public void setOuterJoin(final TableJoinDto outerJoin) {
        this.outerJoin = outerJoin;
    }

    public TableJoinDto getFullJoin() {
        return this.fullJoin;
    }

    public void setFullJoin(final TableJoinDto fullJoin) {
        this.fullJoin = fullJoin;
    }

    public List<DatasetSettingDto> getSeparate() {
        return this.separate;
    }

    public void setSeparate(final List<DatasetSettingDto> separate) {
        this.separate = separate;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    public boolean isDistinct() {
        return this.distinct;
    }

    public void setDistinct(final boolean distinct) {
        this.distinct = distinct;
    }

    public SplitDto getSplit() {
        return this.split;
    }

    public void setSplit(final SplitDto split) {
        this.split = split;
    }

    public List<String> getKeys() {
        return this.keys;
    }

    public void setKeys(final List<String> keys) {
        this.keys = keys;
    }

    public Map<String, String> getStringColumns() {
        return this.stringColumns;
    }

    public void setStringColumns(final Map<String, String> stringColumns) {
        this.stringColumns = stringColumns;
    }

    public Map<String, String> getNumberColumns() {
        return this.numberColumns;
    }

    public void setNumberColumns(final Map<String, String> numberColumns) {
        this.numberColumns = numberColumns;
    }

    public Map<String, String> getBooleanColumns() {
        return this.booleanColumns;
    }

    public void setBooleanColumns(final Map<String, String> booleanColumns) {
        this.booleanColumns = booleanColumns;
    }

    public Map<String, String> getFunctionColumns() {
        return this.functionColumns;
    }

    public void setFunctionColumns(final Map<String, String> functionColumns) {
        this.functionColumns = functionColumns;
    }

    public List<String> getExclude() {
        return this.exclude;
    }

    public void setExclude(final List<String> exclude) {
        this.exclude = exclude;
    }

    public List<String> getInclude() {
        return this.include;
    }

    public void setInclude(final List<String> include) {
        this.include = include;
    }

    public List<String> getFilter() {
        return this.filter;
    }

    public void setFilter(final List<String> filter) {
        this.filter = filter;
    }

    public List<String> getOrder() {
        return this.order;
    }

    public void setOrder(final List<String> order) {
        this.order = order;
    }
}
