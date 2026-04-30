import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import CommandForm from "../../../app/form/CommandForm";
import type { Command, Options } from "../../../model/SelectParameter";
import { SelectParameter } from "../../../model/SelectParameter";
import {
	compareLoadResponseFixture,
	compareRefreshTargetTypeImageResponseFixture,
	convertLoadResponseFixture,
	convertRefreshSrcTypeXlsxResponseFixture,
	generateLoadResponseFixture,
	generateRefreshSrcTypeTableResponseFixture,
	parameterizeLoadResponseFixture,
	runLoadResponseFixture,
	runRefreshScriptTypeSqlResponseFixture,
} from "./fixtures";

const {
	mockRefreshSelectFn,
	mockUseSelectParameter,
	mockUseRefreshSelectParameter,
	mockFormData,
} = vi.hoisted(() => ({
	mockRefreshSelectFn: vi.fn(),
	mockUseSelectParameter: vi.fn(),
	mockUseRefreshSelectParameter: vi.fn(),
	mockFormData: vi.fn(),
}));

vi.mock("../../../app/form/ConvertForm", () => ({
	ConvertForm: ({ handleTypeSelect }: { handleTypeSelect: () => void }) => (
		<div data-testid="mock-convert-form">
			<button
				type="button"
				data-testid="type-select-btn"
				onClick={handleTypeSelect}
			>
				select
			</button>
		</div>
	),
}));
vi.mock("../../../app/form/CompareForm", () => ({
	CompareForm: () => <div data-testid="mock-compare-form" />,
}));
vi.mock("../../../app/form/GenerateForm", () => ({
	GenerateForm: () => <div data-testid="mock-generate-form" />,
}));
vi.mock("../../../app/form/RunForm", () => ({
	RunForm: () => <div data-testid="mock-run-form" />,
}));
vi.mock("../../../app/form/ParameterizeForm", () => ({
	ParameterizeForm: () => <div data-testid="mock-parameterize-form" />,
}));

vi.mock("../../../context/SelectParameterProvider", () => ({
	useSelectParameter: mockUseSelectParameter,
}));

vi.mock("../../../hooks/useSelectParameter", () => ({
	useRefreshSelectParameter: mockUseRefreshSelectParameter,
}));

beforeEach(() => {
	mockUseRefreshSelectParameter.mockReturnValue(mockRefreshSelectFn);
	mockFormData.mockReturnValue({
		values: mockFormValues,
		validationError: false,
	});
});

function makeSelectParameter(
	command: Command,
	refreshOverrides: Options,
): SelectParameter {
	return new SelectParameter(refreshOverrides, command, "test-param");
}

const mockFormValues = { key: "val" as FormDataEntryValue };

function makeSelectParameterWithRefresh(command: Command): SelectParameter {
	switch (command) {
		case "convert":
			return makeSelectParameter(
				command,
				convertRefreshSrcTypeXlsxResponseFixture,
			);
		case "compare":
			return makeSelectParameter(
				command,
				compareRefreshTargetTypeImageResponseFixture,
			);
		case "generate":
			return makeSelectParameter(
				command,
				generateRefreshSrcTypeTableResponseFixture,
			);
		case "run":
			return makeSelectParameter(
				command,
				runRefreshScriptTypeSqlResponseFixture,
			);
		default:
	}
	return makeSelectParameter(command, parameterizeLoadResponseFixture);
}

vi.mock("../../../app/form/section/dialog/ParameterInputDialog", () => ({
	ParameterInputDialog: ({
		handleDialogClose,
		handleCommit,
	}: {
		params: { [key: string]: string };
		handleDialogClose: () => void;
		handleCommit: (params: { [key: string]: string }) => void;
	}) => (
		<div data-testid="mock-param-dialog">
			<button
				type="button"
				data-testid="param-dialog-apply"
				onClick={() => handleCommit({ myParam: "myValue" })}
			>
				Apply
			</button>
			<button
				type="button"
				data-testid="param-dialog-close"
				onClick={handleDialogClose}
			>
				Close
			</button>
		</div>
	),
}));

describe("CommandFormのテスト", () => {
	it.each([
		{
			command: "convert",
			testId: "mock-convert-form",
			parameter: convertLoadResponseFixture,
		},
		{
			command: "compare",
			testId: "mock-compare-form",
			parameter: compareLoadResponseFixture,
		},
		{
			command: "generate",
			testId: "mock-generate-form",
			parameter: generateLoadResponseFixture,
		},
		{
			command: "run",
			testId: "mock-run-form",
			parameter: runLoadResponseFixture,
		},
		{
			command: "parameterize",
			testId: "mock-parameterize-form",
			parameter: parameterizeLoadResponseFixture,
		},
	])("command=$commandのとき、対応するフォームが表示される", ({
		command,
		testId,
		parameter,
	}) => {
		mockUseSelectParameter.mockReturnValue(
			makeSelectParameter(command as Command, parameter),
		);

		render(<CommandForm formData={mockFormData} />);

		expect(screen.getByTestId(testId)).toBeInTheDocument();
	});

	it("handleTypeSelectが呼ばれたとき、formDataの値でrefreshSelectが実行される", async () => {
		mockUseSelectParameter.mockReturnValue(
			makeSelectParameter("convert", convertLoadResponseFixture),
		);

		render(<CommandForm formData={mockFormData} />);

		await userEvent.click(screen.getByTestId("type-select-btn"));

		expect(mockFormData).toHaveBeenCalledWith(false);
		expect(mockRefreshSelectFn).toHaveBeenCalledWith(mockFormValues);
	});

	it.each([
		{ command: "convert", parameter: convertLoadResponseFixture },
		{ command: "compare", parameter: compareLoadResponseFixture },
		{ command: "generate", parameter: generateLoadResponseFixture },
		{ command: "run", parameter: runLoadResponseFixture },
		{ command: "parameterize", parameter: parameterizeLoadResponseFixture },
	])("command=$commandのとき、useRefreshSelectParameterが$commandで呼ばれる", ({
		command,
		parameter,
	}) => {
		mockUseSelectParameter.mockReturnValue(
			makeSelectParameter(command as Command, parameter),
		);

		render(<CommandForm formData={mockFormData} />);

		expect(mockUseRefreshSelectParameter).toHaveBeenCalledWith(command);
	});

	it.each([
		{ command: "convert", testId: "mock-convert-form" },
		{ command: "compare", testId: "mock-compare-form" },
		{ command: "generate", testId: "mock-generate-form" },
		{ command: "run", testId: "mock-run-form" },
		{ command: "parameterize", testId: "mock-parameterize-form" },
	])("refreshレスポンスのパラメータでcommand=$commandのとき、対応するフォームが表示される", ({
		command,
		testId,
	}) => {
		mockUseSelectParameter.mockReturnValue(
			makeSelectParameterWithRefresh(command as Command),
		);

		render(<CommandForm formData={mockFormData} />);

		expect(screen.getByTestId(testId)).toBeInTheDocument();
	});

	it("コマンドが選択されているとき、Parameters (-P) セクションが表示される", () => {
		mockUseSelectParameter.mockReturnValue(
			makeSelectParameter("convert", convertLoadResponseFixture),
		);

		render(<CommandForm formData={mockFormData} />);

		expect(screen.getByText("Parameters (-P)")).toBeInTheDocument();
	});

	it("Edit Parameters ボタンをクリックするとダイアログが表示される", async () => {
		mockUseSelectParameter.mockReturnValue(
			makeSelectParameter("convert", convertLoadResponseFixture),
		);

		render(<CommandForm formData={mockFormData} />);

		await userEvent.click(screen.getByTitle("Edit Parameters"));

		expect(screen.getByTestId("mock-param-dialog")).toBeInTheDocument();
	});

	it("ダイアログでApplyするとhidden inputが追加される", async () => {
		mockUseSelectParameter.mockReturnValue(
			makeSelectParameter("convert", convertLoadResponseFixture),
		);

		const { container } = render(<CommandForm formData={mockFormData} />);

		await userEvent.click(screen.getByTitle("Edit Parameters"));
		await userEvent.click(screen.getByTestId("param-dialog-apply"));

		const hiddenInput = container.querySelector('input[name="-PmyParam"]');
		expect(hiddenInput).toBeInTheDocument();
		expect(hiddenInput).toHaveValue("myValue");
	});

	it("ダイアログでApplyするとパラメータ件数が表示される", async () => {
		mockUseSelectParameter.mockReturnValue(
			makeSelectParameter("convert", convertLoadResponseFixture),
		);

		render(<CommandForm formData={mockFormData} />);

		await userEvent.click(screen.getByTitle("Edit Parameters"));
		await userEvent.click(screen.getByTestId("param-dialog-apply"));

		expect(screen.getByText("1 parameter(s) set")).toBeInTheDocument();
	});

	it("パラメータ名が変わるとパラメータがリセットされる", async () => {
		const parameter1 = makeSelectParameter(
			"convert",
			convertLoadResponseFixture,
		);
		mockUseSelectParameter.mockReturnValue(parameter1);

		const { rerender } = render(<CommandForm formData={mockFormData} />);

		await userEvent.click(screen.getByTitle("Edit Parameters"));
		await userEvent.click(screen.getByTestId("param-dialog-apply"));
		expect(screen.getByText("1 parameter(s) set")).toBeInTheDocument();

		const parameter2 = makeSelectParameter(
			"convert",
			convertLoadResponseFixture,
		);
		(parameter2 as { name: string }).name = "other-param";
		mockUseSelectParameter.mockReturnValue(parameter2);
		rerender(<CommandForm formData={mockFormData} />);

		expect(screen.queryByText("1 parameter(s) set")).not.toBeInTheDocument();
	});
});
