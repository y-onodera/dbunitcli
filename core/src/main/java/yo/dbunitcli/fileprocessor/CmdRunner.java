package yo.dbunitcli.fileprocessor;

import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

public class CmdRunner implements Runner {

    private final Map<String, Object> parameter;
    private final TemplateRender templateRender;

    public CmdRunner(final Map<String, Object> parameter
            , final TemplateRender templateRender) {
        this.parameter = parameter;
        this.templateRender = templateRender;
    }

    @Override
    public void runScript(final Collection<File> targetFiles) {
        targetFiles.forEach(target -> {
            try {
                final ProcessBuilder pb = new ProcessBuilder(this.getTemplateRender()
                        .render(target, this.getParameter()));
                // 標準エラー出力を標準出力にマージする
                pb.redirectErrorStream(true);
                final Process process = pb.start();
                final InputStream in = process.getInputStream();
                final BufferedReader br = new BufferedReader(new InputStreamReader(in, "MS932"));
                while (br.readLine() != null) {
                    // skip input
                }
                process.waitFor();
            } catch (final Throwable var30) {
                throw new AssertionError(var30);
            }
        });
    }

    public Map<String, Object> getParameter() {
        return this.parameter;
    }

    public TemplateRender getTemplateRender() {
        return this.templateRender;
    }
}
