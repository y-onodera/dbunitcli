package yo.dbunitcli.fileprocessor;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Antのメッセージ（ビルドファイルのタスク・SQLExec）をSLF4Jへ出す。
 * ERR→error、WARN→warn、INFO→info、VERBOSE/DEBUG→debug。タスクのメッセージはAntと同じく「[タスク名] 」を付ける
 */
final class AntLogListener implements BuildListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntLogListener.class);

    /**
     * @return このリスナーを付けたProject（タスク定義の読込はしない）
     */
    static Project newProject() {
        final Project result = new Project();
        result.addBuildListener(new AntLogListener());
        return result;
    }

    @Override
    public void messageLogged(final BuildEvent event) {
        final String message = event.getTask() == null
                ? event.getMessage()
                : "[" + event.getTask().getTaskName() + "] " + event.getMessage();
        switch (event.getPriority()) {
            case Project.MSG_ERR -> AntLogListener.LOGGER.error(message, event.getException());
            case Project.MSG_WARN -> AntLogListener.LOGGER.warn(message, event.getException());
            case Project.MSG_INFO -> AntLogListener.LOGGER.info(message, event.getException());
            default -> AntLogListener.LOGGER.debug(message, event.getException());
        }
    }

    @Override
    public void buildStarted(final BuildEvent event) {
    }

    @Override
    public void buildFinished(final BuildEvent event) {
    }

    @Override
    public void targetStarted(final BuildEvent event) {
    }

    @Override
    public void targetFinished(final BuildEvent event) {
    }

    @Override
    public void taskStarted(final BuildEvent event) {
    }

    @Override
    public void taskFinished(final BuildEvent event) {
    }
}
