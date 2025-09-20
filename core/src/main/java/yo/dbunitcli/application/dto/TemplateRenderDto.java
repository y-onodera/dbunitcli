package yo.dbunitcli.application.dto;

import picocli.CommandLine;

public class TemplateRenderDto {
    @CommandLine.Option(names = "-encoding", description = "template file encoding")
    private String encoding;

    @CommandLine.Option(names = "-templateGroup", description = "StringTemplate4 templateGroup file.")
    private String templateGroup;

    @CommandLine.Option(names = "-templateParameterAttribute", description = "attributeName that is used to for access parameter in StringTemplate expression default 'param'.")
    private String templateParameterAttribute;

    @CommandLine.Option(names = "-templateVarStart", description = "StringTemplate expression start char.default '$'")
    private String templateVarStart;

    @CommandLine.Option(names = "-templateVarStop", description = "StringTemplate expression stop char.default '$'\"")
    private String templateVarStop;

    @CommandLine.Option(names = "-formulaProcess", description = "default true. if false xlsx output use LowerMemory but cellRef in formula isn't along with row increase")
    private String formulaProcess;

    @CommandLine.Option(names = "-evaluateFormulas", description = "default true. evaluate Excel formulas when formula process is enabled")
    private String evaluateFormulas;

    @CommandLine.Option(names = "-forceFormulaRecalc", description = "default false. recalculation formulas when opening the file in Excel")
    private String forceFormulaRecalc;

    @CommandLine.Option(names = "-fastFormulaProcess", description = "default false. if true use fast formula processor")
    private String fastFormulaProcess;

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public String getTemplateGroup() {
        return this.templateGroup;
    }

    public void setTemplateGroup(final String templateGroup) {
        this.templateGroup = templateGroup;
    }

    public String getTemplateParameterAttribute() {
        return this.templateParameterAttribute;
    }

    public void setTemplateParameterAttribute(final String templateParameterAttribute) {
        this.templateParameterAttribute = templateParameterAttribute;
    }

    public String getTemplateVarStart() {
        return this.templateVarStart;
    }

    public void setTemplateVarStart(final String templateVarStart) {
        this.templateVarStart = templateVarStart;
    }

    public String getTemplateVarStop() {
        return this.templateVarStop;
    }

    public void setTemplateVarStop(final String templateVarStop) {
        this.templateVarStop = templateVarStop;
    }

    public String getFormulaProcess() {
        return this.formulaProcess;
    }

    public void setFormulaProcess(final String formulaProcess) {
        this.formulaProcess = formulaProcess;
    }

    public String getEvaluateFormulas() {
        return this.evaluateFormulas;
    }

    public void setEvaluateFormulas(final String evaluateFormulas) {
        this.evaluateFormulas = evaluateFormulas;
    }

    public String getForceFormulaRecalc() {
        return this.forceFormulaRecalc;
    }

    public void setForceFormulaRecalc(final String forceFormulaRecalc) {
        this.forceFormulaRecalc = forceFormulaRecalc;
    }

    public String getFastFormulaProcess() {
        return this.fastFormulaProcess;
    }

    public void setFastFormulaProcess(final String fastFormulaProcess) {
        this.fastFormulaProcess = fastFormulaProcess;
    }
}
