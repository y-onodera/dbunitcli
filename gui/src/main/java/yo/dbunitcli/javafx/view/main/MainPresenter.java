package yo.dbunitcli.javafx.view.main;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import io.github.palexdev.materialfx.font.MFXFontIcon;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.tbee.javafx.scene.layout.MigPane;
import yo.dbunitcli.application.Command;
import yo.dbunitcli.application.argument.ArgumentsParser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainPresenter {

    private static final String[] COMMAND_TYPES = {
            "Convert"
            , "Compare"
            , "Generate"
            , "Run"
    };

    @FXML
    private MigPane commandPane;

    @FXML
    private MFXComboBox<String> commandTypeSelect;

    private String selectedCommand;

    private ArgumentsParser parser;

    private Map<String, Node> argument = Maps.newHashMap();


    @FXML
    void initialize() {
        this.commandTypeSelect.getItems().add("");
        for (String cmdType : this.commandTypes()) {
            this.commandTypeSelect.getItems().add(cmdType);
        }
        this.reset(new ActionEvent());
    }

    @FXML
    public void close(ActionEvent actionEvent) {
        Platform.exit();
    }

    @FXML
    public void reset(ActionEvent actionEvent) {
        this.commandTypeSelect.selectFirst();
    }

    @FXML
    public void execCmd() {
        try {
            Class<?> clazz = Class.forName("yo.dbunitcli.application." + this.selectedCommand);
            String[] args = this.inputToArg().entrySet()
                    .stream()
                    .map(it -> it.getKey() + "=" + it.getValue())
                    .toArray(String[]::new);
            ((Command) clazz.getDeclaredConstructor().newInstance()).exec(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void saveFile(ActionEvent actionEvent) throws IOException {
        String content = this.inputToArg().entrySet()
                .stream()
                .map(it -> it.getKey() + "=" + it.getValue())
                .collect(Collectors.joining("\r\n"));
        FileChooser fileSave = new FileChooser();
        fileSave.setTitle("Save Parameter File");
        fileSave.getExtensionFilters().add(new FileChooser.ExtensionFilter("(*.txt)", "*.txt"));
        fileSave.setInitialDirectory(new File("."));
        File file = fileSave.showSaveDialog(commandTypeSelect.getScene().getWindow());
        if (!file.getParentFile().exists()) {
            Files.createDirectories(file.getParentFile().toPath());
        }
        if (!file.exists()) {
            Files.createFile(file.toPath());
        }
        Files.writeString(file.toPath(), content, Charsets.UTF_8);
    }

    @FXML
    public void selectCommandType() throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String currentSelect = this.selectedCommand;
        this.selectedCommand = this.commandTypeSelect.getSelectionModel().getSelectedItem();
        if (Objects.equals(this.commandTypeSelect, currentSelect)) {
            return;
        } else if (Strings.isNullOrEmpty(this.selectedCommand)) {
            this.clearInputFields();
            return;
        }
        Class<?> clazz = Class.forName("yo.dbunitcli.application." + this.selectedCommand + "Option");
        this.parser = (ArgumentsParser) clazz.getDeclaredConstructor().newInstance();
        this.resetInput();
    }

    private void resetInput() {
        ArgumentsParser.OptionParam option = parser.expandOption(this.inputToArg());
        this.clearInputFields();
        int row = 1;
        for (String key : option.keySet()) {
            Map.Entry<String, ArgumentsParser.Attribute> entry = option.getColumn(key);
            if (entry.getValue().getType() == ArgumentsParser.ParamType.ENUM) {
                MFXComboBox<String> select = new MFXComboBox<>(FXCollections.observableArrayList(
                        entry.getValue().getSelectOption()
                ));
                select.getSelectionModel().selectItem(entry.getKey());
                select.setFloatingText(key);
                select.setFloatMode(FloatMode.INLINE);
                this.commandPane.add(select, "cell 0 " + row++);
                this.argument.put(key, select);
                select.getSelectionModel().selectedItemProperty().addListener((observable, newVal, oldVal) -> resetInput());
            } else {
                MFXTextField text = new MFXTextField();
                text.setText(option.get(key));
                text.setFloatingText(key);
                text.setFloatMode(FloatMode.INLINE);
                this.commandPane.add(text, "width 250,cell 0 " + row);
                this.argument.put(key, text);
                if (entry.getValue().getType() == ArgumentsParser.ParamType.DIR) {
                    MFXButton fileChoice = this.createDirectoryChoiceButton(text);
                    text.setTrailingIcon(fileChoice);
                } else if (entry.getValue().getType() == ArgumentsParser.ParamType.FILE) {
                    MFXButton fileChoice = this.createFileChoiceButton(text);
                    text.setTrailingIcon(fileChoice);
                } else if (entry.getValue().getType() == ArgumentsParser.ParamType.FILE_OR_DIR) {
                    HBox hBox = new HBox();
                    hBox.getChildren().addAll(this.createDirectoryChoiceButton(text), this.createFileChoiceButton(text));
                    text.setTrailingIcon(hBox);
                }
                row++;
            }
        }
    }

    private MFXButton createDirectoryChoiceButton(MFXTextField text) {
        return this.createChoiceButton(new MFXFontIcon("mfx-folder", 32), event -> {
            File choice = openDirChooser();
            if (choice != null) {
                text.setText(choice.getAbsolutePath());
            }
        });
    }

    private MFXButton createFileChoiceButton(MFXTextField text) {
        return createChoiceButton(new MFXFontIcon("mfx-file", 32), event -> {
            File choice = openFileChooser();
            if (choice != null) {
                text.setText(choice.getAbsolutePath());
            }
        });
    }

    private MFXButton createChoiceButton(MFXFontIcon folderIcon, EventHandler<ActionEvent> actionEventEventHandler) {
        MFXButton fileChoice = new MFXButton();
        fileChoice.setGraphic(folderIcon);
        fileChoice.setText("");
        fileChoice.setOnAction(actionEventEventHandler);
        return fileChoice;
    }

    private File openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        Stage stage = new Stage();
        stage.initOwner(commandTypeSelect.getScene().getWindow());
        return fileChooser.showOpenDialog(stage);
    }

    private File openDirChooser() {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setInitialDirectory(new File("."));
        Stage stage = new Stage();
        stage.initOwner(commandTypeSelect.getScene().getWindow());
        return fileChooser.showDialog(stage);
    }

    private Map<String, String> inputToArg() {
        return this.argument.entrySet()
                .stream()
                .filter(it -> {
                    if (it.getValue() instanceof TextField) {
                        return !Strings.isNullOrEmpty(it.getKey()) && !Strings.isNullOrEmpty(((TextField) it.getValue()).getText());
                    }
                    return !Strings.isNullOrEmpty(it.getKey())
                            && !Strings.isNullOrEmpty(((ChoiceBox) it.getValue()).getSelectionModel().getSelectedItem().toString());
                })
                .collect(Collectors.toMap(it -> it.getKey(), it -> {
                    if (it.getValue() instanceof TextField) {
                        return ((TextField) it.getValue()).getText();
                    }
                    return ((ChoiceBox) it.getValue()).getSelectionModel().getSelectedItem().toString();
                }));
    }

    private String[] commandTypes() {
        return COMMAND_TYPES;
    }

    private void clearInputFields() {
        this.commandPane.getChildren().removeIf(node -> !this.commandTypeSelect.equals(node));
        this.argument = Maps.newHashMap();
    }

}
