import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { describe, expect, it } from "vitest";
import { AdditionalColumnsFields } from "../../../../../app/form/section/dialog/DatasetSettingFields";
import type { DatasetSettingBuilder } from "../../../../../model/DatasetSettings";
import { DatasetSetting } from "../../../../../model/DatasetSettings";

let current: DatasetSetting;

function record(next: DatasetSetting) {
	current = next;
	return next;
}

function AdditionalColumnsHarness({
	initial,
}: {
	initial: Partial<DatasetSettingBuilder>;
}) {
	const [target, setTarget] = useState(
		new DatasetSetting(initial as DatasetSettingBuilder),
	);
	return (
		<AdditionalColumnsFields
			target={target}
			setTarget={(action) =>
				setTarget((cur) =>
					record(typeof action === "function" ? action(cur) : action),
				)
			}
			columnDatalist={["顧客名", "金額(円)"]}
			onLoadColumns={() => {}}
		/>
	);
}

async function expand(user: ReturnType<typeof userEvent.setup>) {
	await user.click(screen.getByRole("button", { name: /additional columns/ }));
}

describe("AdditionalColumnsFieldsのカラム反映のテスト", () => {
	it("selectで選んだカラムを名前とget式の組で追加し、同名は上書きしない", async () => {
		const user = userEvent.setup();
		render(<AdditionalColumnsHarness initial={{ number: { 顧客名: "1" } }} />);
		await expand(user);

		// string, number, boolean, sqlFunction の順にselectが並ぶ
		await user.click(screen.getAllByRole("button", { name: "select" })[1]);
		await user.selectOptions(screen.getByLabelText("function"), "tryToNum");
		await user.click(screen.getByRole("checkbox", { name: "顧客名" }));
		await user.click(screen.getByRole("checkbox", { name: "金額(円)" }));
		await user.click(screen.getByRole("button", { name: "Apply" }));

		expect(current.number).toEqual({
			顧客名: "1",
			"金額(円)": "tryToNum(get('金額(円)'), 0)",
		});
	});

	it("editで式をテキストエリアで編集し、カラムを追記して反映する", async () => {
		const user = userEvent.setup();
		render(
			<AdditionalColumnsHarness initial={{ string: { 表示日: "'x'" } }} />,
		);
		await expand(user);

		await user.click(screen.getByRole("button", { name: "edit" }));
		const textarea = screen.getByRole("textbox", { name: "" });
		await user.clear(textarea);
		await user.type(textarea, "'日付:' + ");
		await user.selectOptions(screen.getByLabelText("function"), "formatDate");
		await user.click(screen.getByRole("button", { name: "Append Column" }));
		await user.click(screen.getByRole("button", { name: "Apply" }));

		expect(current.string).toEqual({
			表示日:
				"'日付:' + formatDate(get('顧客名'), 'yyyyMMdd', 'yyyy/MM/dd', '')",
		});
		expect(
			document.querySelectorAll<HTMLInputElement>('input[name="string"]')[1]
				.value,
		).toBe("'日付:' + formatDate(get('顧客名'), 'yyyyMMdd', 'yyyy/MM/dd', '')");
	});

	it("editで演算子と文字列メソッドを追記する", async () => {
		const user = userEvent.setup();
		render(<AdditionalColumnsHarness initial={{ boolean: { 判定: "" } }} />);
		await expand(user);

		await user.click(screen.getByRole("button", { name: "edit" }));
		await user.click(screen.getByRole("button", { name: "Append Column" }));
		await user.selectOptions(
			screen.getByLabelText("operator"),
			screen.getByRole("option", { name: "&& (AND)" }),
		);
		await user.click(screen.getByRole("button", { name: "Append Operator" }));
		await user.selectOptions(screen.getByLabelText("column"), "金額(円)");
		await user.selectOptions(screen.getByLabelText("function"), "matches");
		await user.type(screen.getByLabelText("argument"), "[[0-9]+");
		await user.click(screen.getByRole("button", { name: "Append Column" }));
		await user.click(screen.getByRole("button", { name: "Apply" }));

		expect(current.boolean).toEqual({
			判定: "get('顧客名') && get('金額(円)').matches('[0-9]+')",
		});
	});
});
