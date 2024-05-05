package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.cli.CommandDto;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.dto.TemplateRenderDto;

import java.util.HashMap;
import java.util.Map;

public class ParameterizeDto extends CommandDto {
    @CommandLine.Option(names = "-ignoreFail", description = "case when cmd is compare and unexpected diff found, then continue other cmd")
    private String ignoreFail;
    @CommandLine.Option(names = "-cmd", description = "data driven target cmd")
    private String cmd;
    @CommandLine.Option(names = "-cmdParam", description = "columnName parameterFile for cmd. dynamically set fileName from param dataset")
    private String cmdParam;
    @CommandLine.Option(names = {"-arg", "-A"})
    private Map<String, String> arg = new HashMap<>();
    @CommandLine.Option(names = "-parameterize", defaultValue = "true", description = "whether cmdParam is template or not. if true, then cmdParam populate by param")
    private String parameterize;
    @CommandLine.Option(names = "-template", description = "default template file. case when cmdParam exists,this option is ignore.")
    private String template;

    private TemplateRenderDto templateRender = new TemplateRenderDto();

    private DataSetLoadDto dateSetLoad = new DataSetLoadDto();

    public String getIgnoreFail() {
        return this.ignoreFail;
    }

    public void setIgnoreFail(final String ignoreFail) {
        this.ignoreFail = ignoreFail;
    }

    public String getCmd() {
        return this.cmd;
    }

    public void setCmd(final String cmd) {
        this.cmd = cmd;
    }

    public String getCmdParam() {
        return this.cmdParam;
    }

    public void setCmdParam(final String cmdParam) {
        this.cmdParam = cmdParam;
    }

    public String getParameterize() {
        return this.parameterize;
    }

    public void setParameterize(final String parameterize) {
        this.parameterize = parameterize;
    }

    public String getTemplate() {
        return this.template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    public Map<String, String> getArg() {
        return this.arg;
    }

    public void setArg(final Map<String, String> arg) {
        this.arg = arg;
    }

    public TemplateRenderDto getTemplateRender() {
        return this.templateRender;
    }

    public void setTemplateRender(final TemplateRenderDto templateRender) {
        this.templateRender = templateRender;
    }

    public DataSetLoadDto getDateSetLoad() {
        return this.dateSetLoad;
    }

    public void setDateSetLoad(final DataSetLoadDto dateSetLoad) {
        this.dateSetLoad = dateSetLoad;
    }

}
