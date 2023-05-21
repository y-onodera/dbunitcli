package yo.dbunitcli.javafx.view.main;

import com.airhacks.afterburner.views.FXMLView;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainView extends FXMLView {
    private Window window;

    public void open(final Stage stage) {
        final Scene scene = new Scene(this.getView());
        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
        this.window = scene.getWindow();
    }

    public Window getMainWindow() {
        return this.window;
    }
}
