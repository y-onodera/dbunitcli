import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import {
	DialogBody,
	DialogField,
	DialogTextArea,
	DialogTitle,
	ModalOverlay,
	SettingDialog,
} from "../../../components/dialog";

function renderSettingDialog(maximizable: boolean) {
	render(
		<SettingDialog
			title="Edit"
			maximizable={maximizable}
			setting={{}}
			handleCommit={vi.fn()}
			handleDialogClose={vi.fn()}
		>
			<DialogBody size="md">
				<DialogTextArea id="outer" value="" />
				<ModalOverlay>
					<DialogTitle>Nested</DialogTitle>
					<DialogField grow>
						<DialogTextArea id="nested" value="" />
					</DialogField>
				</ModalOverlay>
			</DialogBody>
		</SettingDialog>,
	);
	return userEvent.setup();
}

const textarea = (id: string) => document.getElementById(id) as HTMLElement;

describe("ダイアログの最大化", () => {
	it("maximizableでなければ最大化ボタンを出さない", () => {
		renderSettingDialog(false);
		expect(
			screen.queryByRole("button", { name: "Maximize" }),
		).not.toBeInTheDocument();
	});

	it("最大化するとテキストエリアが残りの高さを埋め、元に戻すと固定の高さに戻る", async () => {
		const user = renderSettingDialog(true);
		expect(textarea("outer")).toHaveClass("h-96");

		await user.click(screen.getByRole("button", { name: "Maximize" }));
		expect(textarea("outer")).toHaveClass("flex-1");
		expect(textarea("outer")).not.toHaveClass("h-96");

		await user.click(screen.getByRole("button", { name: "Restore" }));
		expect(textarea("outer")).toHaveClass("h-96");
	});

	it("入れ子の枠は親の最大化を引き継がない", async () => {
		const user = renderSettingDialog(true);
		await user.click(screen.getByRole("button", { name: "Maximize" }));
		expect(textarea("nested")).toHaveClass("h-96");
		// 入れ子の ModalOverlay は maximizable でないのでボタンは親の1つだけ
		expect(
			screen.getAllByRole("button", { name: /Maximize|Restore/ }),
		).toHaveLength(1);
	});
});
