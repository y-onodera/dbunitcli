package yo.dbunitcli.resource.st4;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Parameter;

public class TemplateRenderTest {

    @Test
    public void replaceParameter_replaces_attribute_with_value() {
        final Parameter parameter = Parameter.none().add("tableName", "T1");
        Assertions.assertEquals("out/T1.txt", TemplateRender.builder().setTemplateParameterAttribute("").build().replaceParameter("out/$tableName$.txt", parameter));
    }

    @Test
    public void replaceParameter_does_not_stringify_unused_value() {
        // 行の一覧など大きな値は、変数がtargetに無ければ文字列化しない
        final Object unused = new Object() {
            @Override
            public String toString() {
                throw new AssertionError("toString called");
            }
        };
        final Parameter parameter = Parameter.none().add("tableName", "T1").add("rows", unused);
        Assertions.assertEquals("out/T1.txt", TemplateRender.builder().setTemplateParameterAttribute("").build().replaceParameter("out/$tableName$.txt", parameter));
    }
}
