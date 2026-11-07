import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import {
	DialogBody,
	DialogField,
	DialogTitle,
	ModalOverlay,
	SettingDialog,
} from "../../../components/dialog";
import { TextArea } from "../../../components/element/Input";

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
				<TextArea id="outer" value="" />
				<ModalOverlay>
					<DialogTitle>Nested</DialogTitle>
					<DialogField grow>
						<TextArea id="nested" value="" />
					</DialogField>
				</ModalOverlay>
			</DialogBody>
		</SettingDialog>,
	);
	return userEvent.setup();
}

// テキストエリアの高さは親（DialogBody）が flex 縦並びで伸びるかで決まる
const bodyOf = (id: string) =>
	document.getElementById(id)?.parentElement as HTMLElement;
// ModalOverlay の枠（スクロールする箱）
const frameOf = (id: string) =>
	document.getElementById(id)?.closest(".overflow-y-auto") as HTMLElement;

describe("ダイアログの最大化", () => {
	it("maximizableでなければ最大化ボタンを出さない", () => {
		renderSettingDialog(false);
		expect(
			screen.queryByRole("button", { name: "Maximize" }),
		).not.toBeInTheDocument();
	});

	it("最大化すると本文が枠の残りを埋め、元に戻すと指定の幅に戻る", async () => {
		const user = renderSettingDialog(true);
		expect(bodyOf("outer")).toHaveClass("w-dialog-md");

		await user.click(screen.getByRole("button", { name: "Maximize" }));
		expect(bodyOf("outer")).toHaveClass("grow", "flex", "w-full");
		expect(bodyOf("outer")).not.toHaveClass("w-dialog-md");

		await user.click(screen.getByRole("button", { name: "Restore" }));
		expect(bodyOf("outer")).toHaveClass("w-dialog-md");
	});

	it("入れ子の枠は親の最大化を引き継がない", async () => {
		const user = renderSettingDialog(true);
		await user.click(screen.getByRole("button", { name: "Maximize" }));
		expect(frameOf("nested")).toHaveClass("max-h-[80vh]");
		expect(frameOf("nested")).not.toHaveClass("h-full");
		// 入れ子の ModalOverlay は maximizable でないのでボタンは親の1つだけ
		expect(
			screen.getAllByRole("button", { name: /Maximize|Restore/ }),
		).toHaveLength(1);
	});
});
