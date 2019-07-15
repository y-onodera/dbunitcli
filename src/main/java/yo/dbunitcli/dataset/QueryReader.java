package yo.dbunitcli.dataset;

import com.google.common.io.Files;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public interface QueryReader {

    default String readQuery(File aFile) throws IOException {
        String query = Files.asCharSource(aFile, Charset.forName(getEncoding())).read();
        if (getParameter().size() > 0) {
            query = new ST(query, '$', '$')
                    .add("param", getParameter())
                    .render();
        }
        return query;
    }

    Map<String, Object> getParameter();

    String getEncoding();
}
