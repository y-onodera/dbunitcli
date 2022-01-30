package yo.dbunitcli.javafx.view.main;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.SearchableComboBox;
import org.tbee.javafx.scene.layout.MigPane;
import yo.dbunitcli.application.argument.ArgumentsParser;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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
    private Label labelSelectType;

    @FXML
    private SearchableComboBox<String> commandTypeSelect;

    private String selectedCommand;

    private ArgumentsParser parser;

    private Map<Label, Node> argument = Maps.newHashMap();

    @FXML
    void initialize() {
        this.commandTypeSelect.getItems().add("");
        for (String cmdType : this.commandTypes()) {
            this.commandTypeSelect.getItems().add(cmdType);
        }
        this.commandTypeSelect.getSelectionModel().select(0);
        this.selectedCommand = this.commandTypeSelect.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void execCmd() {
    }

    @FXML
    public void selectCommandType() throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String cmdType = commandTypeSelect.getSelectionModel().getSelectedItem();
        if (Objects.equals(this.commandTypeSelect, cmdType) || Strings.isNullOrEmpty(cmdType)) {
            return;
        }
        this.selectedCommand = this.commandTypeSelect.getSelectionModel().getSelectedItem();
        Class<?> clazz = Class.forName("yo.dbunitcli.application." + this.selectedCommand + "Option");
        this.parser = (ArgumentsParser) clazz.getDeclaredConstructor().newInstance();
        this.resetInput();
    }

    private void resetInput() {
        ArgumentsParser.OptionParam option = parser.expandOption(this.inputToArg());
        this.clearInputFields();
        int row = 1;
        for (String key : option.keySet()) {
            Label label = new Label();
            label.setText(key);
            this.commandPane.add(label, "width 150,cell 0 " + row);
            Map.Entry<String, Class> entry = option.getColumn(key);
            if (entry.getValue().isEnum()) {
                ChoiceBox<String> select = new ChoiceBox<>(FXCollections.observableArrayList(
                        Arrays.stream(entry.getValue().getEnumConstants())
                                .map(Object::toString)
                                .collect(Collectors.toSet()))
                );
                select.getSelectionModel().select(entry.getKey());
                this.commandPane.add(select, "width 200,cell 1 " + row++);
                this.argument.put(label, select);
                select.getSelectionModel().selectedItemProperty().addListener((observable, newVal, oldVal) -> resetInput());
            } else {
                TextField text = new TextField();
                text.setText(option.get(key));
                this.commandPane.add(text, "width 200,cell 1 " + row++);
                this.argument.put(label, text);
            }
        }
    }

    private Map<String, String> inputToArg() {
        return this.argument.entrySet()
                .stream()
                .filter(it -> {
                    if (it.getValue() instanceof TextField) {
                        return !Strings.isNullOrEmpty(it.getKey().getText()) && !Strings.isNullOrEmpty(((TextField) it.getValue()).getText());
                    }
                    return !Strings.isNullOrEmpty(it.getKey().getText())
                            && !Strings.isNullOrEmpty(((ChoiceBox) it.getValue()).getSelectionModel().getSelectedItem().toString());
                })
                .collect(Collectors.toMap(it -> it.getKey().getText(), it -> {
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
        this.commandPane.getChildren().removeIf(node -> !this.commandTypeSelect.equals(node) && !this.labelSelectType.equals(node));
        this.argument = Maps.newHashMap();
    }

}
