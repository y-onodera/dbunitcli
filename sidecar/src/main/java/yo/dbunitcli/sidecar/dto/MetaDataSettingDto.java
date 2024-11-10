package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;

@Serdeable
public class MetaDataSettingDto {
    private List<String> name;
    private PatternDto pattern;
    private TableJoinDto innerJoin;
    private TableJoinDto outerJoin;
    private TableJoinDto fullJoin;
    private List<MetaDataSettingDto> separate;
    private String prefix;
    private String tableName;
    private String suffix;
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
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public PatternDto getPattern() {
        return pattern;
    }

    public void setPattern(PatternDto pattern) {
        this.pattern = pattern;
    }

    public TableJoinDto getInnerJoin() {
        return innerJoin;
    }

    public void setInnerJoin(TableJoinDto innerJoin) {
        this.innerJoin = innerJoin;
    }

    public TableJoinDto getOuterJoin() {
        return outerJoin;
    }

    public void setOuterJoin(TableJoinDto outerJoin) {
        this.outerJoin = outerJoin;
    }

    public TableJoinDto getFullJoin() {
        return fullJoin;
    }

    public void setFullJoin(TableJoinDto fullJoin) {
        this.fullJoin = fullJoin;
    }

    public List<MetaDataSettingDto> getSeparate() {
        return separate;
    }

    public void setSeparate(List<MetaDataSettingDto> separate) {
        this.separate = separate;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public SplitDto getSplit() {
        return split;
    }

    public void setSplit(SplitDto split) {
        this.split = split;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public Map<String, String> getStringColumns() {
        return stringColumns;
    }

    public void setStringColumns(Map<String, String> stringColumns) {
        this.stringColumns = stringColumns;
    }

    public Map<String, String> getNumberColumns() {
        return numberColumns;
    }

    public void setNumberColumns(Map<String, String> numberColumns) {
        this.numberColumns = numberColumns;
    }

    public Map<String, String> getBooleanColumns() {
        return booleanColumns;
    }

    public void setBooleanColumns(Map<String, String> booleanColumns) {
        this.booleanColumns = booleanColumns;
    }

    public Map<String, String> getFunctionColumns() {
        return functionColumns;
    }

    public void setFunctionColumns(Map<String, String> functionColumns) {
        this.functionColumns = functionColumns;
    }

    public List<String> getExclude() {
        return exclude;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
    }

    public List<String> getInclude() {
        return include;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }

    public List<String> getOrder() {
        return order;
    }

    public void setOrder(List<String> order) {
        this.order = order;
    }
}
