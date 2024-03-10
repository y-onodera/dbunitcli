package yo.dbunitcli.javafx.view.main;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class LoadingView extends FXMLView {
    private Stage loading;

    public Stage open(final Window window) {
        final Scene scene = new Scene(this.getView());
        scene.setFill(null);
        this.loading = new Stage(StageStyle.TRANSPARENT);
        this.loading.setScene(scene);
        this.loading.initOwner(window);
        this.loading.setResizable(false);
        this.loading.show();
        return this.loading;
    }

    public void close() {
        this.loading.close();
    }
}
