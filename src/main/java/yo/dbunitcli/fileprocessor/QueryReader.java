package yo.dbunitcli.fileprocessor;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

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
        getParameter().forEach((k, v) -> {
            String token = k;
            if (!Strings.isNullOrEmpty(this.getTemplateParameterAttribute())) {
                token = this.getTemplateParameterAttribute() + k;
            }
            token = getTemplateVarStart() + token + getTemplateVarStop();
            query.replace(token, v.toString());
        });
        if (getParameter().size() > 0) {
            ST st = new ST(getSTGroup(), query);
            if (Strings.isNullOrEmpty(this.getTemplateParameterAttribute())) {
                this.getParameter().forEach(st::add);
                return st.render();
            }
            return st.add("param", getParameter()).render();
        }
        return query;
    }

    Map<String, Object> getParameter();

    String getEncoding();

    STGroup getSTGroup();

    String getTemplateParameterAttribute();

    default char getTemplateVarStart() {
        return this.getSTGroup().delimiterStartChar;
    }

    default char getTemplateVarStop() {
        return this.getSTGroup().delimiterStopChar;
    }

}
