package yo.dbunitcli.javafx.view.main;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.ButtonType;
import io.github.palexdev.materialfx.enums.FloatMode;
import io.github.palexdev.materialfx.font.MFXFontIcon;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.MFXValidator;
import io.github.palexdev.materialfx.validation.Severity;
import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainPresenter {

    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");

    private static final String[] COMMAND_TYPES = {
            "Convert"
            , "Compare"
            , "Generate"
            , "Run"
    };

    @FXML
    public MFXButton exec;
    @FXML
    public MFXButton reset;
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
        this.commandTypeSelect.setEditable(true);
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
        this.exec.setDisable(true);
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
        if (file == null) {
            return;
        }
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
            this.clearInputFields(this.commandTypeSelect);
            this.exec.setDisable(true);
            return;
        }
        Class<?> clazz = Class.forName("yo.dbunitcli.application." + this.selectedCommand + "Option");
        this.parser = (ArgumentsParser) clazz.getDeclaredConstructor().newInstance();
        this.resetInput(this.commandTypeSelect);
    }

    private void resetInput(MFXComboBox<String> selected) {
        this.resetInput(selected, selected);
    }

    private void resetInput(MFXComboBox<String> selected, Node form) {
        ArgumentsParser.OptionParam option = this.parser.createOptionParam(this.inputToArg());
        this.clearInputFields(form);
        int row = 1;
        MFXValidator validator = new MFXValidator();
        validator.validProperty().addListener((observable, oldVal, newVal) -> {
            this.exec.setDisable(!newVal);
        });
        for (String key : option.keySet()) {
            Map.Entry<String, ArgumentsParser.Attribute> entry = option.getColumn(key);
            if (entry.getValue().getType() == ArgumentsParser.ParamType.ENUM) {
                if (selected.getFloatingText().equals(key)) {
                    this.argument.put(key, selected);
                } else {
                    MFXComboBox<String> select = new MFXComboBox<>(FXCollections.observableArrayList(
                            entry.getValue().getSelectOption()
                    ));
                    select.setFloatingText(key);
                    select.setFloatMode(FloatMode.INLINE);
                    select.setEditable(true);
                    select.getSelectionModel().selectItem(entry.getKey());
                    if (entry.getValue().isRequired()) {
                        VBox vbox = addRequiredValidation(validator, select);
                        this.commandPane.add(vbox, "cell 0 " + row);
                        select.getSelectionModel().selectedItemProperty()
                                .addListener((observable, newVal, oldVal) -> resetInput(select, vbox));
                    } else {
                        this.commandPane.add(select, "width 80,cell 0 " + row);
                        select.getSelectionModel().selectedItemProperty()
                                .addListener((observable, newVal, oldVal) -> resetInput(select));
                    }
                    this.argument.put(key, select);
                }
            } else {
                MFXTextField text = new MFXTextField();
                text.setPrefWidth(400);
                text.setText(option.get(key));
                text.setFloatingText(key);
                text.setFloatMode(FloatMode.INLINE);
                if (entry.getValue().isRequired()) {
                    VBox vbox = addRequiredValidation(validator, text);
                    this.commandPane.add(vbox, "cell 0 " + row);
                } else {
                    this.commandPane.add(text, "cell 0 " + row);
                }
                this.argument.put(key, text);
                if (entry.getValue().getType() == ArgumentsParser.ParamType.DIR) {
                    text.setTrailingIcon(this.createDirectoryChoiceButton(text));
                } else if (entry.getValue().getType() == ArgumentsParser.ParamType.FILE) {
                    text.setTrailingIcon(this.createFileChoiceButton(text));
                } else if (entry.getValue().getType() == ArgumentsParser.ParamType.FILE_OR_DIR) {
                    HBox hBox = new HBox();
                    hBox.getChildren().addAll(this.createDirectoryChoiceButton(text), this.createFileChoiceButton(text));
                    text.setTrailingIcon(hBox);
                }
            }
            row++;
        }
        MFXButton more = new MFXButton("more");
        more.setButtonType(ButtonType.RAISED);
        more.getStyleClass().add("text-button");
        this.commandPane.add(more, "align right,cell 1 " + row);
    }

    private VBox addRequiredValidation(MFXValidator validator, MFXTextField text) {
        Label validationLabel = new Label();
        validationLabel.setText("required input");
        validationLabel.getStyleClass().add("validationLabel");
        text.getValidator().validProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal) {
                text.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
            }
        });
        text.delegateFocusedProperty().addListener((observable, oldVal, newVal) -> {
            if (oldVal && !newVal) {
                List<Constraint> constraints = text.validate();
                if (!constraints.isEmpty()) {
                    text.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, true);
                }
            }
        });
        text.getValidator().constraint(Constraint.of(Severity.ERROR, validationLabel.getText()
                , BooleanExpression.booleanExpression(text.textProperty().isEmpty().not())));
        validator.dependsOn(text.getValidator());
        VBox vbox = new VBox();
        vbox.setPrefWidth(400);
        vbox.getChildren().addAll(text, validationLabel);
        return vbox;
    }

    private StackPane createDirectoryChoiceButton(MFXTextField text) {
        return this.createChoiceButton(new MFXFontIcon("mfx-folder", 32), event -> {
            File choice = openDirChooser();
            if (choice != null) {
                text.setText(relativize(choice));
            }
        });
    }

    private StackPane createFileChoiceButton(MFXTextField text) {
        return createChoiceButton(new MFXFontIcon("mfx-file", 32), event -> {
            File choice = openFileChooser();
            if (choice != null) {
                text.setText(relativize(choice));
            }
        });
    }

    private String relativize(File choice) {
        return Path.of(new File(".").getAbsolutePath())
                .relativize(Path.of(choice.getAbsolutePath()))
                .toString()
                .replace("\\", "/");
    }

    private StackPane createChoiceButton(MFXFontIcon folderIcon, EventHandler<ActionEvent> actionEventEventHandler) {
        MFXButton fileChoice = new MFXButton();
        fileChoice.setGraphic(folderIcon);
        fileChoice.setText("");
        fileChoice.setOnAction(actionEventEventHandler);
        fileChoice.setAlignment(Pos.TOP_CENTER);
        fileChoice.setPrefHeight(50);
        TextField textField = new TextField("open");
        textField.setMaxWidth(45);
        textField.setPrefHeight(10);
        textField.setAlignment(Pos.BOTTOM_CENTER);
        StackPane stack = new StackPane();
        stack.setAlignment(Pos.BOTTOM_CENTER);
        stack.getChildren().addAll(fileChoice, textField);
        return stack;
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

    private void clearInputFields(Node selected) {
        this.commandPane.getChildren().removeIf(node -> !this.commandTypeSelect.equals(node) && !selected.equals(node));
        this.argument = Maps.newHashMap();
    }

}
