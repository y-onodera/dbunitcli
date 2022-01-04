package yo.dbunitcli.javafx.view.menu;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MenuPresenter {

    @FXML
    private Pane menuPane;

    @FXML
    void initialize() {
        assert menuPane != null : "fx:id=\"menuPane\" was not injected: check your FXML file 'menu.fxml'.";
    }

    @FXML
    void handleOpenFile() {
        File file = openFileChooser("Open Config File", "text format (*.txt)", "*.txt");
    }

    @FXML
    void handleSaveAs() {
        File saveAs = openFileChooser("Save Configto", "text format (*.txt)", "*.txt");
    }

    @FXML
    void handleCreateNewConfig() {

    }

    protected File openFileChooser(String aTitle, String aFilterMessage, String aFilterExtensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(aTitle);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(aFilterMessage, aFilterExtensions));
        fileChooser.setInitialDirectory(this.getBaseDirectory());
        Stage stage = new Stage();
        stage.initOwner(menuPane.getScene().getWindow());
        return fileChooser.showOpenDialog(stage);
    }

    protected File getBaseDirectory() {
        return new File("");
    }

}
