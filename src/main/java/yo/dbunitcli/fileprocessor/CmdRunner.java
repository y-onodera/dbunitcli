package yo.dbunitcli.fileprocessor;

import com.google.common.base.Strings;
import org.dbunit.dataset.DataSetException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

public class CmdRunner implements Runner, QueryReader {

    private final Map<String, Object> parameter;
    private final String encoding;
    private final char templateVarStart;
    private final char templateVarStop;
    private final String templateParameterAttribute;

    public CmdRunner(Map<String, Object> parameter
            , String encoding
            , char templateVarStart, char templateVarStop
            , String templateParameterAttribute) {
        this.parameter = parameter;
        this.encoding = encoding;
        this.templateVarStart = templateVarStart;
        this.templateVarStop = templateVarStop;
        this.templateParameterAttribute = templateParameterAttribute;
    }

    @Override
    public void runScript(Collection<File> targetFiles) throws DataSetException {
        try {
            for (File target : targetFiles) {
                ProcessBuilder pb = new ProcessBuilder(this.readQuery(target));
                // 標準エラー出力を標準出力にマージする
                pb.redirectErrorStream(true);
                Process process = pb.start();
                InputStream in = process.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "MS932"));
                String stdout = "";
                while ((stdout = br.readLine()) != null) {
                    // 不要なメッセージを表示しない
                    if (Strings.isNullOrEmpty(stdout))
                        continue;
                    if (stdout.contains("echo off "))
                        continue;
                    if (stdout.contains("続行するには何かキーを押してください ")) {
                        continue;
                    }
                }
                process.waitFor();
            }
        } catch (Throwable var30) {
            throw new DataSetException(var30);
        }
    }

    @Override
    public Map<String, Object> getParameter() {
        return this.parameter;
    }

    @Override
    public String getEncoding() {
        return this.encoding;
    }

    @Override
    public String getTemplateParameterAttribute() {
        return this.templateParameterAttribute;
    }

    @Override
    public char getTemplateVarStart() {
        return this.templateVarStart;
    }

    @Override
    public char getTemplateVarStop() {
        return this.templateVarStop;
    }
}
