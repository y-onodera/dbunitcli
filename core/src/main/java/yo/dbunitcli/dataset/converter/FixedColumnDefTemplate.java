package yo.dbunitcli.dataset.converter;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FixedColumnDefTemplate {

    private static final String TEMPLATE_PATH = "fixedcolumndef/fixedColumnDefTemplate.txt";

    private final TemplateRender render = new TemplateRender.Builder()
            .setTemplateParameterAttribute(null)
            .build();

    public String render(final List<FixedColumnDef> columns) {
        final STGroup stGroup = this.render.createSTGroup();
        final ST st = new ST(stGroup, FileResources.readClasspathResource(TEMPLATE_PATH));
        st.add("columns", columns);
        return st.render();
    }

    public void write(final List<FixedColumnDef> columns, final File resultFile, final String outputEncoding) throws IOException {
        this.render.write(
                FileResources.readClasspathResource(TEMPLATE_PATH),
                Parameter.none().add("columns", columns),
                resultFile,
                outputEncoding);
    }
}
