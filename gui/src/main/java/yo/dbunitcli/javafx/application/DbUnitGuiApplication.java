package yo.dbunitcli.javafx.application;

import com.airhacks.afterburner.injection.Injector;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import yo.dbunitcli.javafx.view.main.MainView;

public class DbUnitGuiApplication extends Application {

    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(final Stage stage) {
        Injector.setModelOrService(DbUnitGuiApplication.class, this);

        stage.initStyle(StageStyle.TRANSPARENT);
        final MainView mainView = new MainView();
        UserAgentBuilder.builder()
                .themes(MaterialFXStylesheets.forAssemble(false))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();
        mainView.open(stage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
