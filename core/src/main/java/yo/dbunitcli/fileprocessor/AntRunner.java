package yo.dbunitcli.fileprocessor;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import yo.dbunitcli.Strings;
import yo.dbunitcli.common.Parameter;

import java.io.File;

public record AntRunner(File baseDir, String target, Parameter parameter) implements Runner {
    @Override
    public void runScript(final File target) {
        final Project p = AntLogListener.newProject();
        p.setUserProperty("ant.file", target.getAbsolutePath());
        p.init();
        final ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, target);
        p.setBasedir(this.baseDir().getPath());
        this.parameter.forEach((k, v) -> p.setProperty(k, v.toString()));
        p.executeTarget(Strings.isNotEmpty(this.target()) ? this.target() : p.getDefaultTarget());
    }
}
