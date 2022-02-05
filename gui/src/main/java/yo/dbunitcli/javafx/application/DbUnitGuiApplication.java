package yo.dbunitcli.javafx.application;

import com.airhacks.afterburner.injection.Injector;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import yo.dbunitcli.javafx.view.main.MainView;

public class DbUnitGuiApplication extends Application {

    private MainView mainView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Injector.setModelOrService(DbUnitGuiApplication.class, this);
        stage.initStyle(StageStyle.TRANSPARENT);
        this.mainView = new MainView();
        this.mainView.open(stage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
