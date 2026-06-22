package yo.dbunitcli.application.command;

import yo.dbunitcli.Strings;

import java.util.Map;

public record JavaBeanField(String fieldName, String javaType, boolean nullable) {

    private static final Map<String, String> TYPE_MAP = Map.ofEntries(
            Map.entry("VARCHAR", "String"),
            Map.entry("CHAR", "String"),
            Map.entry("NVARCHAR", "String"),
            Map.entry("NCHAR", "String"),
            Map.entry("TEXT", "String"),
            Map.entry("CLOB", "String"),
            Map.entry("NCLOB", "String"),
            Map.entry("LONGVARCHAR", "String"),
            Map.entry("LONGNVARCHAR", "String"),
            Map.entry("INTEGER", "Integer"),
            Map.entry("INT", "Integer"),
            Map.entry("INT4", "Integer"),
            Map.entry("SMALLINT", "Integer"),
            Map.entry("TINYINT", "Integer"),
            Map.entry("BIGINT", "Long"),
            Map.entry("INT8", "Long"),
            Map.entry("DECIMAL", "java.math.BigDecimal"),
            Map.entry("NUMERIC", "java.math.BigDecimal"),
            Map.entry("NUMBER", "java.math.BigDecimal"),
            Map.entry("FLOAT", "Double"),
            Map.entry("FLOAT4", "Float"),
            Map.entry("FLOAT8", "Double"),
            Map.entry("REAL", "Float"),
            Map.entry("DOUBLE", "Double"),
            Map.entry("BOOLEAN", "Boolean"),
            Map.entry("BOOL", "Boolean"),
            Map.entry("BIT", "Boolean"),
            Map.entry("DATE", "java.time.LocalDate"),
            Map.entry("TIME", "java.time.LocalTime"),
            Map.entry("TIMESTAMP", "java.time.LocalDateTime"),
            Map.entry("BLOB", "byte[]"),
            Map.entry("BINARY", "byte[]"),
            Map.entry("VARBINARY", "byte[]"),
            Map.entry("LONGVARBINARY", "byte[]")
    );

    public static JavaBeanField of(final Map<String, Object> row) {
        final String columnName = row.get("COLUMN_NAME").toString();
        final String typeName = normalizeTypeName(row.get("TYPE_NAME").toString());
        final boolean nullable = Boolean.TRUE.equals(row.get("NULLABLE"));
        final String fieldName = Strings.snakeToCamel(columnName.toLowerCase(), Character::toLowerCase);
        final String javaType = resolveJavaType(typeName);
        return new JavaBeanField(fieldName, javaType, nullable);
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String getJavaType() {
        return this.javaType;
    }

    public boolean isNullable() {
        return this.nullable;
    }

    public String getGetterName() {
        if ("Boolean".equals(this.javaType)) {
            return "is" + Strings.capitalize(this.fieldName);
        }
        return "get" + Strings.capitalize(this.fieldName);
    }

    public String getSetterName() {
        return "set" + Strings.capitalize(this.fieldName);
    }

    private static String normalizeTypeName(final String typeName) {
        final int parenIdx = typeName.indexOf('(');
        return (parenIdx > 0 ? typeName.substring(0, parenIdx) : typeName).trim().toUpperCase();
    }

    private static String resolveJavaType(final String typeName) {
        final String mapped = TYPE_MAP.get(typeName);
        if (mapped != null) {
            return mapped;
        }
        if (typeName.contains("CHAR") || typeName.contains("TEXT") || typeName.contains("CLOB")) {
            return "String";
        }
        if (typeName.contains("INT")) {
            return "Long";
        }
        if (typeName.contains("FLOAT") || typeName.contains("DOUBLE") || typeName.contains("REAL")) {
            return "Double";
        }
        if (typeName.contains("DECIMAL") || typeName.contains("NUMERIC") || typeName.contains("NUMBER")) {
            return "java.math.BigDecimal";
        }
        if (typeName.contains("TIMESTAMP") || typeName.contains("DATETIME")) {
            return "java.time.LocalDateTime";
        }
        if (typeName.contains("DATE")) {
            return "java.time.LocalDate";
        }
        if (typeName.contains("TIME")) {
            return "java.time.LocalTime";
        }
        if (typeName.contains("BOOL") || typeName.contains("BIT")) {
            return "Boolean";
        }
        if (typeName.contains("BINARY") || typeName.contains("BLOB")) {
            return "byte[]";
        }
        return "Object";
    }
}
