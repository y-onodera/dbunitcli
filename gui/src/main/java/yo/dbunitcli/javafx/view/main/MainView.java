package yo.dbunitcli.javafx.view.main;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainView extends FXMLView {
    private double xOffset = 0;
    private double yOffset = 0;

    public void open(final Stage stage) {
        final Scene scene = new Scene(this.getView());
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
        //マウス・ボタンが押されたとき
        scene.setOnMousePressed(event -> {
            this.xOffset = event.getSceneX();
            this.yOffset = event.getSceneY();
        });
        //マウス・ボタンがドラッグされるとき
        scene.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - this.xOffset);
            stage.setY(event.getScreenY() - this.yOffset);
        });
    }

}
