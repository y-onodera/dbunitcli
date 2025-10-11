package yo.dbunitcli.fileprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public record CmdRunner(File baseDir, Parameter parameter) implements Runner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmdRunner.class);

    @Override
    public void runScript(final Stream<File> targetFiles) {
        targetFiles.forEach(target -> {
            try {
                final ProcessBuilder pb = new ProcessBuilder(target.getPath())
                        .directory(this.baseDir);
                this.parameter().forEach((key, value) -> pb.environment().put(key, value.toString()));
                // 標準エラー出力を標準出力にマージする
                pb.redirectErrorStream(true);
                final Process process = pb.start();
                final InputStream in = process.getInputStream();
                final BufferedReader br = new BufferedReader(new InputStreamReader(in, "MS932"));
                String stdout = "";
                while ((stdout = br.readLine()) != null) {
                    CmdRunner.LOGGER.info(stdout);
                }
                process.waitFor();
            } catch (final Throwable var30) {
                throw new AssertionError(var30);
            }
        });
    }
}
