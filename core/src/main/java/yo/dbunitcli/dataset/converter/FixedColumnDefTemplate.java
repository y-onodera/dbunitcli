package yo.dbunitcli.dataset.converter;

import org.stringtemplate.v4.ST;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FixedColumnDefTemplate {

    private static final String TEMPLATE = FileResources.readClasspathResource("fixedcolumndef/fixedColumnDefTemplate.txt");

    private static final TemplateRender RENDER = new TemplateRender.Builder()
            .setTemplateParameterAttribute(null)
            .build();

    public String render(final List<FixedColumnDef> columns) {
        final ST st = new ST(RENDER.createSTGroup(), TEMPLATE);
        st.add("columns", columns);
        return st.render();
    }

    public void write(final List<FixedColumnDef> columns, final File resultFile, final String outputEncoding) throws IOException {
        RENDER.write(TEMPLATE, Parameter.none().add("columns", columns), resultFile, outputEncoding);
    }
}
