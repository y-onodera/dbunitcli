import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { describe, expect, it, vi } from "vitest";
import {
	type SettingDialogComponentProps,
	SettingTable,
} from "../../../components/dialog";

class Item {
	constructor(readonly name: string) {}
	displayName(): string {
		return this.name;
	}
}

// 開いた設定をそのまま確定するダイアログ
function CommitDialog({
	setting,
	handleCommit,
}: SettingDialogComponentProps<Item>) {
	return (
		<button type="button" onClick={() => handleCommit(new Item(setting.name))}>
			commit
		</button>
	);
}

function Harness({ initial }: { initial: Item[] }) {
	const [settings, setSettings] = useState(initial);
	return (
		<>
			<SettingTable<Item>
				caption="items"
				settings={settings}
				setSettings={setSettings}
				SettingDialogComponent={CommitDialog}
				newSetting={() => new Item("")}
			/>
			<button
				type="button"
				onClick={() => setSettings((cur) => cur.map((s) => new Item(s.name)))}
			>
				reload
			</button>
		</>
	);
}

// 末尾のAdd行を除いた、設定の行の表示名
const rowNames = () =>
	screen
		.getAllByRole("rowheader")
		.slice(0, -1)
		.map((th) => th.textContent);

describe("SettingTableのテスト", () => {
	it("同じ表示名の行をコピーで追加しても行が重複扱いにならない", async () => {
		const consoleError = vi
			.spyOn(console, "error")
			.mockImplementation(() => {});
		const user = userEvent.setup();
		render(<Harness initial={[new Item("A")]} />);

		await user.click(screen.getByRole("button", { name: "copy" }));
		await user.click(screen.getByRole("button", { name: "commit" }));

		expect(rowNames()).toEqual(["A", "A"]);
		const duplicateKeyWarned = consoleError.mock.calls
			.flat()
			.some((arg) => String(arg).includes("same key"));
		expect(duplicateKeyWarned).toBe(false);
	});

	it("編集中に一覧が差し替わり編集元が無くなっていたら末尾に追加する", async () => {
		const user = userEvent.setup();
		render(<Harness initial={[new Item("A")]} />);

		await user.click(screen.getByRole("button", { name: "edit" }));
		await user.click(screen.getByRole("button", { name: "reload" }));
		await user.click(screen.getByRole("button", { name: "commit" }));

		expect(rowNames()).toEqual(["A", "A"]);
	});
});
