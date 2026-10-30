import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { describe, expect, it } from "vitest";
import { FilterOrderFields } from "../../../../../app/form/section/dialog/DatasetSettingFields";
import { DatasetSetting } from "../../../../../model/DatasetSettings";

function FilterOrderHarness({ include }: { include: string[] }) {
	const [target, setTarget] = useState(new DatasetSetting({ include }));
	return (
		<FilterOrderFields
			target={target}
			setTarget={setTarget}
			columnDatalist={["COL_A", "COL_B", "COL_C"]}
			columnList={undefined}
			onLoadColumns={() => {}}
		/>
	);
}

function includeValues() {
	return Array.from(
		document.querySelectorAll<HTMLInputElement>('input[name="include"]'),
	).map((input) => input.value);
}

// 各項目の行ごとにselectボタンがあり、exclude(空の1行)の次にincludeの行が並ぶ
async function selectColumns(
	user: ReturnType<typeof userEvent.setup>,
	includeRow: number,
	columns: string[],
) {
	await user.click(
		screen.getAllByRole("button", { name: "select" })[1 + includeRow],
	);
	for (const column of columns) {
		await user.click(screen.getByRole("checkbox", { name: column }));
	}
	await user.click(screen.getByRole("button", { name: "Apply" }));
}

describe("FilterOrderFieldsのカラム選択のテスト", () => {
	it("他の行にあるカラムは除き、先頭をクリックした行に設定して残りを追加する", async () => {
		const user = userEvent.setup();
		render(<FilterOrderHarness include={["COL_A", "COL_B"]} />);

		await selectColumns(user, 1, ["COL_A", "COL_C"]);
		expect(includeValues()).toEqual(["COL_A", "COL_C"]);
	});

	it("クリックした行と同じカラムを選び直しても重複しない", async () => {
		const user = userEvent.setup();
		render(<FilterOrderHarness include={["COL_A", "COL_B"]} />);

		await selectColumns(user, 1, ["COL_C", "COL_B"]);
		expect(includeValues()).toEqual(["COL_A", "COL_C", "COL_B"]);
	});

	it("選んだカラムがすべて他の行にあれば何も変えない", async () => {
		const user = userEvent.setup();
		render(<FilterOrderHarness include={["COL_A", "COL_B"]} />);

		await selectColumns(user, 1, ["COL_A"]);
		expect(includeValues()).toEqual(["COL_A", "COL_B"]);
	});
});
