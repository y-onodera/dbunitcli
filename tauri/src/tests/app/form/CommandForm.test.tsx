import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import CommandForm from "../../../app/form/CommandForm";
import type { SelectParameter } from "../../../model/CommandParam";
import {
	compareLoadResponseFixture,
	convertLoadResponseFixture,
	generateLoadResponseFixture,
	parameterizeLoadResponseFixture,
	runLoadResponseFixture,
} from "./fixtures";

// モック関数をホイスト（mockReset: true により各テスト前にリセットされるため beforeEach で再設定する）
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

// 子フォームコンポーネントをモック
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

// SelectParameterProvider の useSelectParameter をモック
vi.mock("../../../context/SelectParameterProvider", () => ({
	useSelectParameter: mockUseSelectParameter,
}));

// useSelectParameter フックをモック
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

// 全コマンドの共通モック SelectParameter 値を生成
function makeSelectParameter(command: string): SelectParameter {
	return {
		command,
		name: "test-param",
		convert: convertLoadResponseFixture,
		compare: compareLoadResponseFixture,
		generate: generateLoadResponseFixture,
		run: runLoadResponseFixture,
		parameterize: parameterizeLoadResponseFixture,
		currentParameter: () => undefined,
	} as unknown as SelectParameter;
}

const mockFormValues = { key: "val" as FormDataEntryValue };

describe("CommandFormのテスト", () => {
	it.each([
		{ command: "convert", testId: "mock-convert-form" },
		{ command: "compare", testId: "mock-compare-form" },
		{ command: "generate", testId: "mock-generate-form" },
		{ command: "run", testId: "mock-run-form" },
		{ command: "parameterize", testId: "mock-parameterize-form" },
	])("command=$commandのとき、対応するフォームが表示される", ({
		command,
		testId,
	}) => {
		mockUseSelectParameter.mockReturnValue(makeSelectParameter(command));

		render(<CommandForm formData={mockFormData} />);

		expect(screen.getByTestId(testId)).toBeInTheDocument();
	});

	it("commandが未知のとき、何も表示されない", () => {
		mockUseSelectParameter.mockReturnValue(makeSelectParameter("unknown"));

		const { container } = render(<CommandForm formData={mockFormData} />);

		expect(container.firstChild).toBeNull();
	});

	it("handleTypeSelectが呼ばれたとき、formDataの値でrefreshSelectが実行される", async () => {
		mockUseSelectParameter.mockReturnValue(makeSelectParameter("convert"));

		render(<CommandForm formData={mockFormData} />);

		await userEvent.click(screen.getByTestId("type-select-btn"));

		expect(mockFormData).toHaveBeenCalledWith(false);
		expect(mockRefreshSelectFn).toHaveBeenCalledWith(mockFormValues);
	});
});
