package yo.dbunitcli.fileprocessor;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import yo.dbunitcli.Strings;

import java.io.File;
import java.util.Map;
import java.util.stream.Stream;

public record AntRunner(String baseDir, String target, Map<String, Object> parameter) implements Runner {

    @Override
    public void runScript(final Stream<File> targetFiles) {
        targetFiles.forEach(target -> {
            final Project p = new Project();
            p.setBasedir(this.baseDir());
            p.setUserProperty("ant.file", target.getAbsolutePath());
            p.init();
            final ProjectHelper helper = ProjectHelper.getProjectHelper();
            p.addReference("ant.projectHelper", helper);
            helper.parse(p, target);
            this.parameter.forEach((k, v) -> p.setProperty(k, v.toString()));
            p.executeTarget(Strings.isNotEmpty(this.target()) ? this.target() : p.getDefaultTarget());
        });
    }
}
