package yo.dbunitcli.javafx.view.main;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.MFXValidator;
import io.github.palexdev.materialfx.validation.Severity;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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

    private final Map<String, Node> argument = new LinkedHashMap<>();


    @FXML
    void initialize() {
        this.commandTypeSelect.getItems().add("");
        this.commandTypeSelect.setEditable(true);
        for (final String cmdType : this.commandTypes()) {
            this.commandTypeSelect.getItems().add(cmdType);
        }
        this.reset();
    }

    @FXML
    public void close() {
        Platform.exit();
    }

    @FXML
    public void reset() {
        this.commandTypeSelect.selectFirst();
        this.exec.setDisable(true);
    }

    @FXML
    public void execCmd() {
        try {
            final Class<?> clazz = Class.forName("yo.dbunitcli.application." + this.selectedCommand);
            final String[] args = this.inputToArg().entrySet()
                    .stream()
                    .map(it -> it.getKey() + "=" + it.getValue())
                    .toArray(String[]::new);
            ((Command) clazz.getDeclaredConstructor().newInstance()).exec(args);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void saveFile() throws IOException {
        final String content = this.inputToArg().entrySet()
                .stream()
                .map(it -> it.getKey() + "=" + it.getValue())
                .collect(Collectors.joining("\r\n"));
        final FileChooser fileSave = new FileChooser();
        fileSave.setTitle("Save Parameter File");
        fileSave.getExtensionFilters().add(new FileChooser.ExtensionFilter("(*.txt)", "*.txt"));
        fileSave.setInitialDirectory(new File("."));
        final File file = fileSave.showSaveDialog(this.commandTypeSelect.getScene().getWindow());
        if (file == null) {
            return;
        }
        if (!file.getParentFile().exists()) {
            Files.createDirectories(file.getParentFile().toPath());
        }
        if (!file.exists()) {
            Files.createFile(file.toPath());
        }
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    }

    @FXML
    public void selectCommandType() throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final String currentSelect = this.selectedCommand;
        this.selectedCommand = this.commandTypeSelect.getSelectionModel().getSelectedItem();
        if (Objects.equals(this.selectedCommand, currentSelect)) {
            return;
        } else if (Optional.ofNullable(this.selectedCommand).orElse("").isEmpty()) {
            this.clearInputFields(this.commandTypeSelect);
            this.exec.setDisable(true);
            return;
        }
        final Class<?> clazz = Class.forName("yo.dbunitcli.application." + this.selectedCommand + "Option");
        this.parser = (ArgumentsParser) clazz.getDeclaredConstructor().newInstance();
        this.resetInput(this.commandTypeSelect);
    }

    private void resetInput(final MFXComboBox<String> selected) {
        this.resetInput(selected, selected);
    }

    private void resetInput(final MFXComboBox<String> selected, final Node form) {
        final ArgumentsParser.OptionParam option = this.parser.createOptionParam(this.inputToArg());
        this.clearInputFields(form);
        int row = 1;
        final MFXValidator validator = new MFXValidator();
        validator.validProperty().addListener((observable, oldVal, newVal) -> {
            this.exec.setDisable(!newVal);
        });
        for (final String key : option.keySet()) {
            final Map.Entry<String, ArgumentsParser.Attribute> entry = option.getColumn(key);
            if (entry.getValue().getType() == ArgumentsParser.ParamType.ENUM) {
                if (selected.getFloatingText().equals(key)) {
                    this.argument.put(key, selected);
                } else {
                    final MFXComboBox<String> select = new MFXComboBox<>(FXCollections.observableArrayList(
                            entry.getValue().getSelectOption()
                    ));
                    select.setFloatingText(key);
                    select.setFloatMode(FloatMode.INLINE);
                    select.setEditable(true);
                    select.getSelectionModel().selectItem(entry.getKey());
                    if (entry.getValue().isRequired()) {
                        final VBox vbox = this.addRequiredValidation(validator, select);
                        this.commandPane.add(vbox, "cell 0 " + row);
                        select.getSelectionModel().selectedItemProperty()
                                .addListener((observable, newVal, oldVal) -> this.resetInput(select, vbox));
                    } else {
                        this.commandPane.add(select, "width 80,cell 0 " + row);
                        select.getSelectionModel().selectedItemProperty()
                                .addListener((observable, newVal, oldVal) -> this.resetInput(select));
                    }
                    this.argument.put(key, select);
                }
            } else {
                final MFXTextField text = new MFXTextField();
                text.setPrefWidth(400);
                text.setText(option.get(key));
                text.setFloatingText(key);
                text.setFloatMode(FloatMode.INLINE);
                if (entry.getValue().isRequired()) {
                    final VBox vbox = this.addRequiredValidation(validator, text);
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
                    final HBox hBox = new HBox();
                    hBox.getChildren().addAll(this.createDirectoryChoiceButton(text), this.createFileChoiceButton(text));
                    text.setTrailingIcon(hBox);
                }
            }
            row++;
        }
    }

    private VBox addRequiredValidation(final MFXValidator validator, final MFXTextField text) {
        final Label validationLabel = new Label();
        validationLabel.setText("required input");
        validationLabel.getStyleClass().add("validationLabel");
        text.getValidator().validProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal) {
                text.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
            }
        });
        text.delegateFocusedProperty().addListener((observable, oldVal, newVal) -> {
            if (oldVal && !newVal) {
                final List<Constraint> constraints = text.validate();
                if (!constraints.isEmpty()) {
                    text.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, true);
                }
            }
        });
        text.getValidator().constraint(Constraint.of(Severity.ERROR, validationLabel.getText()
                , BooleanExpression.booleanExpression(text.textProperty().isEmpty().not())));
        validator.dependsOn(text.getValidator());
        final VBox vbox = new VBox();
        vbox.setPrefWidth(400);
        vbox.getChildren().addAll(text, validationLabel);
        return vbox;
    }

    private StackPane createDirectoryChoiceButton(final MFXTextField text) {
        return this.createChoiceButton(new MFXFontIcon("fas-folder", 32), event -> {
            final File choice = this.openDirChooser();
            if (choice != null) {
                text.setText(this.relative(choice));
            }
        });
    }

    private StackPane createFileChoiceButton(final MFXTextField text) {
        return this.createChoiceButton(new MFXFontIcon("fas-file", 32), event -> {
            final File choice = this.openFileChooser();
            if (choice != null) {
                text.setText(this.relative(choice));
            }
        });
    }

    private String relative(final File choice) {
        return Path.of(new File(".").getAbsolutePath())
                .relativize(Path.of(choice.getAbsolutePath()))
                .toString()
                .replace("\\", "/");
    }

    private StackPane createChoiceButton(final MFXFontIcon folderIcon, final EventHandler<ActionEvent> actionEventEventHandler) {
        final MFXButton fileChoice = new MFXButton();
        fileChoice.setGraphic(folderIcon);
        fileChoice.setText("");
        fileChoice.setOnAction(actionEventEventHandler);
        fileChoice.setAlignment(Pos.TOP_CENTER);
        fileChoice.setPrefHeight(50);
        final TextField textField = new TextField("open");
        textField.setMaxWidth(45);
        textField.setPrefHeight(10);
        textField.setAlignment(Pos.BOTTOM_CENTER);
        final StackPane stack = new StackPane();
        stack.setAlignment(Pos.BOTTOM_CENTER);
        stack.getChildren().addAll(fileChoice, textField);
        return stack;
    }

    private File openFileChooser() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        final Stage stage = new Stage();
        stage.initOwner(this.commandTypeSelect.getScene().getWindow());
        return fileChooser.showOpenDialog(stage);
    }

    private File openDirChooser() {
        final DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setInitialDirectory(new File("."));
        final Stage stage = new Stage();
        stage.initOwner(this.commandTypeSelect.getScene().getWindow());
        return fileChooser.showDialog(stage);
    }

    private Map<String, String> inputToArg() {
        return this.argument.entrySet()
                .stream()
                .filter(it -> {
                    if (it.getValue() instanceof TextField) {
                        return !Optional.ofNullable(it.getKey()).orElse("").isEmpty()
                                && !Optional.ofNullable(((TextField) it.getValue()).getText()).orElse("").isEmpty();
                    }
                    return !Optional.ofNullable(it.getKey()).orElse("").isEmpty()
                            && !Optional.ofNullable(((ChoiceBox) it.getValue()).getSelectionModel().getSelectedItem().toString()).orElse("").isEmpty();
                })
                .collect(Collectors.toMap(Map.Entry::getKey, it -> {
                    if (it.getValue() instanceof TextField) {
                        return ((TextField) it.getValue()).getText();
                    }
                    return ((ChoiceBox) it.getValue()).getSelectionModel().getSelectedItem().toString();
                }, (s, a) -> s, LinkedHashMap::new));
    }

    private String[] commandTypes() {
        return COMMAND_TYPES;
    }

    private void clearInputFields(final Node selected) {
        this.commandPane.getChildren().removeIf(node -> !this.commandTypeSelect.equals(node) && !selected.equals(node));
        this.argument.clear();
    }

}
