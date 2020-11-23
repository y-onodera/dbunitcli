package yo.dbunitcli.fileprocessor;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.stringtemplate.v4.ST;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public interface QueryReader {

    default String readQuery(File aFile) throws IOException {
        String query = toString(aFile);
        if (getParameter().size() > 0) {
            query = new ST(query, getTemplateVarStart(), getTemplateVarStop())
                    .add("param", getParameter())
                    .render();
        }
        return query;
    }

    default String toString(File aFile) throws IOException {
        StringBuilder script = new StringBuilder();
        BufferedReader lineReader = Files.asCharSource(aFile, Charset.forName(getEncoding()))
                .openBufferedStream();
        String line;
        while ((line = lineReader.readLine()) != null) {
            String trimmedLine = line.trim();
            if (!Strings.isNullOrEmpty(trimmedLine)) {
                if (!isComment(trimmedLine)) {
                    script.append(trimmedLine);
                    script.append(System.getProperty("line.separator", "\n"));
                }
            }
        }
        return script.toString();
    }

    default boolean isComment(String trimmedLine) {
        return trimmedLine.startsWith("//") || trimmedLine.startsWith("--");
    }

    Map<String, Object> getParameter();

    String getEncoding();

    char getTemplateVarStart();

    char getTemplateVarStop();

}
