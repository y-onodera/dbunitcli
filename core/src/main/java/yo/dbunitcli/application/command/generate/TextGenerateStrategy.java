package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.resource.FileResources;

import java.io.File;

/**
 * txt: ユーザー指定の-templateで生成する汎用テキスト型
 */
public final class TextGenerateStrategy implements GenerateStrategy {

    @Override
    public String templateString(final GenerateContext option) {
        final File templatePath = option.getTemplatePath();
        if (templatePath == null || !templatePath.exists() || !templatePath.isFile()) {
            throw new AssertionError(option.template() + " is not exist file",
                                     new IllegalArgumentException(String.valueOf(option.template())));
        }
        return FileResources.read(templatePath, option.templateOption().encoding());
    }
}
