package yo.dbunitcli.fileprocessor;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.dbunit.dataset.DataSetException;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public class AntRunner implements Runner {

    private final Map<String, Object> parameter;

    public AntRunner(Map<String, Object> parameter) {
        this.parameter = parameter;
    }

    @Override
    public void runScript(Collection<File> targetFiles) throws DataSetException {
        try {
            for (File target : targetFiles) {
                Project p = new Project();
                p.setUserProperty("ant.file", target.getAbsolutePath());
                p.init();
                ProjectHelper helper = ProjectHelper.getProjectHelper();
                p.addReference("ant.projectHelper", helper);
                helper.parse(p, target);
                this.parameter.forEach((k, v) -> p.setProperty(k, v.toString()));
                p.executeTarget(p.getDefaultTarget());
            }
        } catch (Throwable var30) {
            throw new DataSetException(var30);
        }
    }
}
