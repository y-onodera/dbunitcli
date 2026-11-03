import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import CsvReadInsertDialog from "../../../../../app/form/section/dialog/CsvReadInsertDialog";
import type { FileProp } from "../../../../../app/form/section/element/FormElementProp";

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { mockCsvColumns } = vi.hoisted(() => ({
	mockCsvColumns: vi.fn(),
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useCsvColumnNamesFetcher: () => mockCsvColumns,
}));
// ファイル選択ダイアログの代わりに、決まったファイルを選ぶボタンにする
vi.mock("../../../../../app/form/section/element/Chooser", () => ({
	FileChooser: (prop: FileProp) => (
		<button
			type="button"
			onClick={() => prop.setPath("dataset\\user_account.csv")}
		>
			Choose
		</button>
	),
}));

// 入れ子のダイアログが重なるため、後ろ（手前のダイアログ）から数えて取る
function fromLast(elements: HTMLElement[], n = 1): HTMLElement {
	return elements[elements.length - n];
}

function renderDialog() {
	mockCsvColumns.mockResolvedValue(["ID", "NAME"]);
	const onInsert = vi.fn();
	render(
		<CsvReadInsertDialog
			encoding="MS932"
			onInsert={onInsert}
			onClose={vi.fn()}
		/>,
	);
	return { user: userEvent.setup(), onInsert };
}

describe("CsvReadInsertDialog", () => {
	it("選んだファイルの列を読み、選んだ列でSELECT文を挿入する", async () => {
		const { user, onInsert } = renderDialog();
		expect(screen.getByLabelText("Charset*")).toHaveValue("MS932");
		expect(screen.getByRole("button", { name: "Next" })).toBeDisabled();

		await user.click(screen.getByRole("button", { name: "Choose" }));
		expect(screen.getByLabelText("CSV file*")).toHaveValue(
			"dataset\\user_account.csv",
		);
		expect(screen.getByLabelText("Table alias")).toHaveValue("UA");
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");
		expect(mockCsvColumns).toHaveBeenCalledWith(
			"CSVREAD('dataset/user_account.csv',null,'charset=MS932')",
			expect.anything(),
		);

		await user.click(screen.getByText("NAME"));
		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsert).toHaveBeenCalledWith([
			"SELECT",
			"    UA.NAME",
			"FROM",
			"    CSVREAD('dataset/user_account.csv',null,'charset=MS932') UA",
		]);
	});

	it("joinでは別名が空なら進めず、選んだ列でONを組み立てる", async () => {
		const { user, onInsert } = renderDialog();
		await user.type(screen.getByLabelText("CSV file*"), "orders.csv");
		await user.clear(screen.getByLabelText("Charset*"));
		await user.type(screen.getByLabelText("Charset*"), "UTF-8");
		await user.selectOptions(screen.getByLabelText("Usage"), "leftJoin");
		expect(screen.getByRole("button", { name: "Next" })).toBeDisabled();

		await user.type(screen.getByLabelText("Table alias*"), "O");
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");
		await user.click(screen.getByText("ID"));
		await user.click(screen.getByRole("button", { name: "Insert Columns" }));
		expect(onInsert).toHaveBeenCalledWith([
			"    LEFT JOIN",
			"        CSVREAD('orders.csv',null,'charset=UTF-8') O",
			"          ON  O.ID = ",
		]);
	});

	it("ONのINのサブクエリは、テーブルの代わりにCSVファイルを1つ選んで組み立てる", async () => {
		const { user, onInsert } = renderDialog();
		await user.type(screen.getByLabelText("CSV file*"), "orders.csv");
		await user.selectOptions(screen.getByLabelText("Usage"), "innerJoin");
		await user.type(screen.getByLabelText("Table alias*"), "O");
		await user.click(screen.getByRole("button", { name: "Next" }));
		await screen.findByText("Select Columns");
		await user.selectOptions(screen.getByLabelText("Operator"), "in");
		await user.selectOptions(screen.getByLabelText("IN source"), "subquery");
		await user.click(screen.getByText("ID"));
		await user.click(fromLast(screen.getAllByRole("button", { name: "Next" })));

		await screen.findByText("Subquery Table");
		const chooseButtons = screen.getAllByRole("button", { name: "Choose" });
		await user.click(chooseButtons[chooseButtons.length - 1]);
		// 文字コードの初期値は外側で指定したもの、別名はファイル名から
		expect(fromLast(screen.getAllByLabelText("Charset*"))).toHaveValue("MS932");
		expect(screen.getByLabelText("Table alias")).toHaveValue("UA");
		const nexts = screen.getAllByRole("button", { name: "Next" });
		await user.click(nexts[nexts.length - 1]);

		await screen.findByText("Subquery Columns");
		expect(mockCsvColumns).toHaveBeenLastCalledWith(
			"CSVREAD('dataset/user_account.csv',null,'charset=MS932')",
			expect.anything(),
		);
		// 後ろ2つがSubquery ColumnsのSELECT columnとWHERE columns
		await user.click(fromLast(screen.getAllByText("NAME"), 2));
		await user.click(screen.getByRole("button", { name: "Insert" }));
		expect(onInsert).toHaveBeenCalledWith([
			"    INNER JOIN",
			"        CSVREAD('orders.csv',null,'charset=MS932') O",
			"          ON  O.ID IN (",
			"                  SELECT",
			"                      UA.NAME",
			"                  FROM",
			"                      CSVREAD('dataset/user_account.csv',null,'charset=MS932') UA",
			"              )",
		]);
	});

	it("cross joinは列を読まずにそのまま挿入する", async () => {
		const { user, onInsert } = renderDialog();
		await user.click(screen.getByRole("button", { name: "Choose" }));
		await user.selectOptions(screen.getByLabelText("Usage"), "crossJoin");
		expect(screen.getByTestId("insert-preview")).toHaveTextContent(
			"CROSS JOIN CSVREAD('dataset/user_account.csv',null,'charset=MS932') UA",
		);
		await user.click(screen.getByRole("button", { name: "Insert" }));
		expect(onInsert).toHaveBeenCalledWith([
			"    CROSS JOIN",
			"        CSVREAD('dataset/user_account.csv',null,'charset=MS932') UA",
		]);
		expect(mockCsvColumns).not.toHaveBeenCalled();
	});
});
