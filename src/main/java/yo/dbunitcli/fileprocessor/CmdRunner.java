package yo.dbunitcli.fileprocessor;

import com.google.common.base.Strings;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.TemplateRender;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

public class CmdRunner implements Runner {

    private final Map<String, Object> parameter;
    private final TemplateRender templateRender;

    public CmdRunner(Map<String, Object> parameter
            , TemplateRender templateRender) {
        this.parameter = parameter;
        this.templateRender = templateRender;
    }

    @Override
    public void runScript(Collection<File> targetFiles) throws DataSetException {
        try {
            for (File target : targetFiles) {
                ProcessBuilder pb = new ProcessBuilder(this.getTemplateLoader()
                        .render(target, this.getParameter()));
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

    public Map<String, Object> getParameter() {
        return this.parameter;
    }

    public TemplateRender getTemplateLoader() {
        return this.templateRender;
    }
}
