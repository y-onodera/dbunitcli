package yo.dbunitcli.fileprocessor;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public interface QueryReader {

    default String readQuery(File aFile) throws IOException {
        String query = Files.asCharSource(aFile, Charset.forName(getEncoding())).read();
        return this.applyParameter(query);
    }

    default String applyParameter(String query) {
        if (getParameter().size() > 0) {
            ST st = new ST(query, getTemplateVarStart(), getTemplateVarStop());
            if(Strings.isNullOrEmpty(this.getTemplateParameterAttribute())){
                this.getParameter().forEach(st::add);
                return st.render();
            }
            return st
                    .add("param", getParameter())
                    .render();
        }
        return query;
    }

    Map<String, Object> getParameter();

    String getEncoding();

    String getTemplateParameterAttribute();

    char getTemplateVarStart();

    char getTemplateVarStop();

}
