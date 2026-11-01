package yo.dbunitcli.application.command.generate;

/**
 * javaBean: テーブル毎にJavaBeanクラスを生成する固定成果物型
 */
public final class JavaBeanGenerateStrategy extends DdlMetaDataGenerateStrategy {

    @Override
    public String stgPath() {
        return "javabean/javaBeanTemplate.stg";
    }

    @Override
    public String templatePath() {
        return "javabean/javaBeanTemplate.txt";
    }

    @Override
    public String defaultSettingsPath() {
        return "javabean/javaBeanSettings.json";
    }

    @Override
    public String extension() {
        return ".java";
    }

    @Override
    public String tableNameFormat() {
        return "snakeToUpperCamel";
    }
}
