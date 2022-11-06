package yo.dbunitcli.fileprocessor;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public class AntRunner implements Runner {

    private final Map<String, Object> parameter;

    public AntRunner(final Map<String, Object> parameter) {
        this.parameter = parameter;
    }

    @Override
    public void runScript(final Collection<File> targetFiles) {
        targetFiles.forEach(target -> {
            final Project p = new Project();
            p.setUserProperty("ant.file", target.getAbsolutePath());
            p.init();
            final ProjectHelper helper = ProjectHelper.getProjectHelper();
            p.addReference("ant.projectHelper", helper);
            helper.parse(p, target);
            this.parameter.forEach((k, v) -> p.setProperty(k, v.toString()));
            p.executeTarget(p.getDefaultTarget());
        });
    }
}
