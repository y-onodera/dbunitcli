package yo.dbunitcli.javafx.view.main;

import com.google.common.base.Strings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.SearchableComboBox;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.NamedOptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.tbee.javafx.scene.layout.MigPane;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

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
        this.clearInputFields();
        Class<?> clazz = Class.forName("yo.dbunitcli.application." + this.selectedCommand + "Option");
        Object bean = clazz.getDeclaredConstructor().newInstance();
        CmdLineParser parser = new CmdLineParser(bean);
        int row = 1;
        for (OptionHandler it : parser.getOptions()) {
            if (it.option instanceof NamedOptionDef && !it.option.isArgument()) {
                Label label = new Label();
                label.setText(((NamedOptionDef) it.option).name());
                TextField text = new TextField();
                text.setText(it.printDefaultValue());
                this.commandPane.add(label, "width 150,cell 0 " + row);
                this.commandPane.add(text, "width 200,cell 1 " + row++);
            }
        }
    }

    private String[] commandTypes() {
        return COMMAND_TYPES;
    }

    private void clearInputFields() {
        this.commandPane.getChildren().removeIf(node -> !this.commandTypeSelect.equals(node) && !this.labelSelectType.equals(node));
    }

}
