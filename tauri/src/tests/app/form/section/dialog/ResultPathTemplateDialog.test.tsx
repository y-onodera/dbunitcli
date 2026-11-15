import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import ResultPathTemplateDialog from "../../../../../app/form/section/dialog/ResultPathTemplateDialog";
import { DEFAULT_TEMPLATE_EXPRESSION_SYNTAX } from "../../../../../model/TemplateExpression";

function renderDialog(unit?: string) {
	render(
		<ResultPathTemplateDialog
			currentValue=""
			syntax={DEFAULT_TEMPLATE_EXPRESSION_SYNTAX}
			unit={unit}
			handleDialogClose={vi.fn()}
			handleSave={vi.fn()}
		/>,
	);
}

function candidates(): string[] {
	return Array.from(
		document.querySelectorAll("#resultPathTemplate_variable_list option"),
	).map((it) => it.getAttribute("value") ?? "");
}

function variableInput(): HTMLInputElement {
	return document.getElementById(
		"resultPathTemplate_variable",
	) as HTMLInputElement;
}

function hint(): string | null {
	return (
		document.getElementById("resultPathTemplate_hint")?.textContent ?? null
	);
}

describe("ResultPathTemplateDialog", () => {
	it("unit=recordではtableName・rowNumberを候補にし、rowNumberが行番号である旨を出す", () => {
		renderDialog("record");
		expect(candidates()).toEqual(["tableName", "rowNumber"]);
		expect(variableInput().value).toBe("tableName");
		expect(hint()).toContain("row number within each table");
	});

	it("unit=tableではtableName・rowNumberを候補にし、rowNumberがテーブル番号である旨を出す", () => {
		renderDialog("table");
		expect(candidates()).toEqual(["tableName", "rowNumber"]);
		expect(variableInput().value).toBe("tableName");
		expect(hint()).toContain("table number");
	});

	it("unit=datasetでは候補を出さず、Variableも空にする", () => {
		renderDialog("dataset");
		expect(candidates()).toEqual([]);
		expect(variableInput().value).toBe("");
		expect(hint()).toContain("tableName is not available");
		expect(screen.getByText("(Enter a variable)")).toBeTruthy();
	});

	it("unitが分からないときはtableName・rowNumberを候補にし、説明は出さない", () => {
		renderDialog(undefined);
		expect(candidates()).toEqual(["tableName", "rowNumber"]);
		expect(variableInput().value).toBe("tableName");
		expect(hint()).toBeNull();
	});
});
