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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.tbee.javafx.scene.layout.MigPane;
import yo.dbunitcli.application.*;
import yo.dbunitcli.application.option.Option;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;

public class MainPresenter {

    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");

    private static final String[] COMMAND_TYPES = {
            "Convert"
            , "Compare"
            , "Generate"
            , "Run"
    };
    private final Map<String, Node> argument = new LinkedHashMap<>();
    @FXML
    public MFXButton exec;
    @FXML
    public MFXButton reset;
    @FXML
    public MFXButton paramLoad;
    @FXML
    public HBox commandBox;
    @FXML
    private MigPane commandPane;
    @FXML
    private MFXComboBox<String> commandTypeSelect;
    private String selectedCommand;
    private Command<?, ?> parser;

    @FXML
    void initialize() {
        this.commandTypeSelect.getItems().add("");
        this.commandTypeSelect.setEditable(true);
        for (final String cmdType : this.commandTypes()) {
            this.commandTypeSelect.getItems().add(cmdType);
        }
        this.paramLoad.visibleProperty().bind(this.commandTypeSelect.selectedItemProperty().isNotEqualTo(""));
        this.reset();
    }

    @FXML
    public void close() {
        Platform.exit();
    }

    @FXML
    public void loadParam() {
        final FileChooser fileSave = new FileChooser();
        fileSave.setTitle("Save Parameter File");
        fileSave.getExtensionFilters().add(new FileChooser.ExtensionFilter("(*.txt)", "*.txt"));
        fileSave.setInitialDirectory(new File("."));
        final File file = fileSave.showOpenDialog(this.commandTypeSelect.getScene().getWindow());
        if (file != null) {
            this.parser = this.createCommand(this.selectedCommand);
            this.refresh(this.commandTypeSelect, this.commandTypeSelect
                    , () -> this.parser.parseOption(this.parser.getExpandArgs(new String[]{"@" + file.getPath()}))
                            .toCommandLineArgs());
        }
    }

    @FXML
    public void reset() {
        this.commandTypeSelect.selectFirst();
        this.exec.setDisable(true);
    }

    @FXML
    public void execCmd() {
        final String[] args = this.inputToArg();
        final Thread background = new Thread(() -> {
            final File result = this.execCommand(this.selectedCommand, args);
            if (result != null) {
                try {
                    Desktop.getDesktop().open(result);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        background.setDaemon(true);
        background.start();
    }

    @FXML
    public void saveFile() throws IOException {
        final String content = String.join("\r\n", this.inputToArg());
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
    public void selectCommandType() {
        final String currentSelect = this.selectedCommand;
        this.selectedCommand = this.commandTypeSelect.getSelectionModel().getSelectedItem();
        if (Objects.equals(this.selectedCommand, currentSelect)) {
            return;
        } else if (Optional.ofNullable(this.selectedCommand).orElse("").isEmpty()) {
            this.clearInputFields(this.commandTypeSelect);
            this.exec.setDisable(true);
            return;
        }
        this.parser = this.createCommand(this.selectedCommand);
        this.refresh(this.commandTypeSelect);
    }

    private void refresh(final MFXComboBox<String> selected) {
        this.refresh(selected, selected);
    }

    private void refresh(final MFXComboBox<String> selected, final Node form) {
        this.refresh(selected, form, () -> this.parser.parseOption(this.inputToArg()).toCommandLineArgs());
    }

    private void refresh(final MFXComboBox<String> selected, final Node form, final Supplier<Option.CommandLineArgs> loadOption) {
        final Stage loading = new LoadingView().open(this.commandBox.getScene().getWindow());
        this.commandPane.setVisible(false);
        final Thread background = new Thread(() -> {
            final Option.CommandLineArgs option = loadOption.get();
            Platform.runLater(() -> this.clearInputFields(form));
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                Platform.runLater(() -> {
                    this.setInputFields(selected, option);
                    this.commandPane.setVisible(true);
                    selected.requestFocus();
                    loading.close();
                });
            }
        });
        background.setDaemon(true);
        background.start();
    }

    private void setInputFields(final MFXComboBox<String> selected, final Option.CommandLineArgs option) {
        int row = 1;
        final MFXValidator validator = new MFXValidator();
        validator.validProperty().addListener((observable, oldVal, newVal) -> this.exec.setDisable(!newVal));
        for (final String key : option.keySet()) {
            final Option.Arg entry = option.getArg(key);
            if (entry.attribute().getType() == Option.ParamType.ENUM) {
                this.setInputFieldsEnumValue(selected, row, validator, key, entry);
            } else {
                this.setInputFieldsTextValue(option, row, validator, key, entry);
            }
            row++;
        }
    }

    private void setInputFieldsTextValue(final Option.CommandLineArgs option, final int row, final MFXValidator validator, final String key, final Option.Arg entry) {
        final MFXTextField text = new MFXTextField();
        text.setPrefWidth(400);
        text.setText(option.get(key));
        text.setFloatingText(key);
        text.setFloatMode(FloatMode.INLINE);
        if (entry.attribute().isRequired()) {
            final VBox vbox = this.addRequiredValidation(validator, text);
            this.commandPane.add(vbox, "cell 0 " + row);
        } else {
            this.commandPane.add(text, "cell 0 " + row);
        }
        this.argument.put(key, text);
        if (entry.attribute().getType() == Option.ParamType.DIR) {
            text.setTrailingIcon(this.createDirectoryChoiceButton(text));
        } else if (entry.attribute().getType() == Option.ParamType.FILE) {
            text.setTrailingIcon(this.createFileChoiceButton(text));
        } else if (entry.attribute().getType() == Option.ParamType.FILE_OR_DIR) {
            final HBox hBox = new HBox();
            hBox.getChildren().addAll(this.createDirectoryChoiceButton(text), this.createFileChoiceButton(text));
            hBox.setSpacing(5);
            text.setTrailingIcon(hBox);
        }
    }

    private void setInputFieldsEnumValue(final MFXComboBox<String> selected, final int row, final MFXValidator validator, final String key, final Option.Arg entry) {
        if (selected.getFloatingText().equals(key)) {
            this.argument.put(key, selected);
        } else {
            final MFXComboBox<String> select = new MFXComboBox<>(FXCollections.observableArrayList(
                    entry.attribute().getSelectOption()
            ));
            select.setFloatingText(key);
            select.setFloatMode(FloatMode.INLINE);
            select.setEditable(true);
            select.getSelectionModel().selectItem(entry.value());
            if (entry.attribute().isRequired()) {
                final VBox vbox = this.addRequiredValidation(validator, select);
                this.commandPane.add(vbox, "cell 0 " + row);
                select.getSelectionModel().selectedItemProperty()
                        .addListener((observable, newVal, oldVal) -> this.refresh(select, vbox));
            } else {
                this.commandPane.add(select, "width 80,cell 0 " + row);
                select.getSelectionModel().selectedItemProperty()
                        .addListener((observable, newVal, oldVal) -> this.refresh(select));
            }
            this.argument.put(key, select);
        }
    }

    private VBox addRequiredValidation(final MFXValidator validator, final MFXTextField text) {
        final Label validationLabel = new Label();
        validationLabel.setText("required input");
        validationLabel.getStyleClass().add("validationLabel");
        text.getValidator().validProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal) {
                text.pseudoClassStateChanged(MainPresenter.INVALID_PSEUDO_CLASS, false);
            }
        });
        text.delegateFocusedProperty().addListener((observable, oldVal, newVal) -> {
            if (oldVal && !newVal) {
                final List<Constraint> constraints = text.validate();
                if (!constraints.isEmpty()) {
                    text.pseudoClassStateChanged(MainPresenter.INVALID_PSEUDO_CLASS, true);
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
        return this.createChoiceButton(new MFXFontIcon("fas-folder-plus", 32), event -> {
            final File choice = this.openDirChooser(text.getText());
            if (choice != null) {
                text.setText(this.relative(choice));
            }
        });
    }

    private StackPane createFileChoiceButton(final MFXTextField text) {
        return this.createChoiceButton(new MFXFontIcon("fas-file-circle-plus", 32), event -> {
            final File choice = this.openFileChooser(text.getText());
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
        fileChoice.setText("open");
        fileChoice.setContentDisplay(ContentDisplay.TOP);
        fileChoice.setOnAction(actionEventEventHandler);
        fileChoice.setAlignment(Pos.CENTER);
        fileChoice.setPrefHeight(60);
        final StackPane stack = new StackPane();
        stack.getChildren().addAll(fileChoice);
        return stack;
    }

    private File openFileChooser(final String path) {
        final File defaultDir = Optional.ofNullable(path)
                .filter(it -> !it.isEmpty())
                .map(it -> new File(it).getParentFile())
                .filter(File::exists)
                .orElse(new File("."));
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(defaultDir);
        final Stage stage = new Stage();
        stage.initOwner(this.commandTypeSelect.getScene().getWindow());
        return fileChooser.showOpenDialog(stage);
    }

    private File openDirChooser(final String path) {
        final File defaultDir = Optional.ofNullable(path)
                .filter(it -> !it.isEmpty())
                .map(File::new)
                .filter(File::exists)
                .orElse(new File("."));
        final DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setInitialDirectory(defaultDir);
        final Stage stage = new Stage();
        stage.initOwner(this.commandTypeSelect.getScene().getWindow());
        return fileChooser.showDialog(stage);
    }

    private String[] inputToArg() {
        return this.argument.entrySet()
                .stream()
                .filter(it -> {
                    if (it.getValue() instanceof TextField textField) {
                        return !Optional.ofNullable(it.getKey()).orElse("").isEmpty()
                                && !Optional.ofNullable(textField.getText()).orElse("").isEmpty();
                    }
                    return !Optional.ofNullable(it.getKey()).orElse("").isEmpty()
                            && it.getValue() instanceof ChoiceBox<?> choiceBox
                            && !Optional.ofNullable(choiceBox.getSelectionModel().getSelectedItem().toString()).orElse("").isEmpty();
                })
                .map(it -> {
                    final String value;
                    if (it.getValue() instanceof TextField textField) {
                        value = textField.getText();
                    } else {
                        value = it.getValue() instanceof ChoiceBox<?> choiceBox
                                ? choiceBox.getSelectionModel().getSelectedItem().toString() : "";
                    }
                    return it.getKey() + "=" + value;
                })
                .toArray(String[]::new);
    }

    private String[] commandTypes() {
        return MainPresenter.COMMAND_TYPES;
    }

    private void clearInputFields(final Node selected) {
        this.commandPane.getChildren().removeIf(node -> !this.commandBox.equals(node) && !selected.equals(node));
        this.argument.clear();
    }

    private Command<?, ?> createCommand(final String command) {
        return switch (command) {
            case "Convert" -> new Convert();
            case "Compare" -> new Compare();
            case "Generate" -> new Generate();
            case "Run" -> new Run();
            default -> null;
        };
    }

    private File execCommand(final String selectedCommand, final String[] args) {
        return switch (selectedCommand) {
            case "Convert" -> {
                final Convert command = new Convert();
                final ConvertOption option = command.parseOption(args);
                command.exec(option);
                yield option.result().convertResult().getResultDir();
            }
            case "Compare" -> {
                final Compare command = new Compare();
                final CompareOption option = command.parseOption(args);
                command.exec(option);
                yield option.result().convertResult().getResultDir();
            }
            case "Generate" -> {
                final Generate command = new Generate();
                final GenerateOption option = command.parseOption(args);
                command.exec(option);
                yield option.getResultDir();
            }
            case "Run" -> {
                final Run command = new Run();
                final RunOption option = command.parseOption(args);
                command.exec(option);
                yield null;
            }
            default -> null;
        };
    }

}
