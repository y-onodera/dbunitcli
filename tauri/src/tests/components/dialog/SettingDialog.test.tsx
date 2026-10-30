import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { SettingDialog } from "../../../components/dialog";
import type { OperationResult } from "../../../utils/fetchUtils";

function renderFileMode(result: OperationResult) {
	const save = vi.fn().mockResolvedValue(result);
	const handleSave = vi.fn();
	render(
		<SettingDialog
			fileName="before.json"
			handleDialogClose={vi.fn()}
			save={save}
			handleSave={handleSave}
		>
			<div />
		</SettingDialog>,
	);
	return { user: userEvent.setup(), save, handleSave };
}

describe("SettingDialogのファイルモードのテスト", () => {
	it("Saveで入力中のファイル名へ保存し、成功したらhandleSaveを呼ぶ", async () => {
		const { user, save, handleSave } = renderFileMode("success");
		const fileName = document.querySelector(
			'input[name="fileName"]',
		) as HTMLInputElement;
		await user.clear(fileName);
		await user.type(fileName, "after.json");
		await user.click(screen.getByRole("button", { name: "Save" }));

		await waitFor(() => {
			expect(handleSave).toHaveBeenCalledWith("after.json");
		});
		expect(save).toHaveBeenCalledWith("after.json");
	});

	it("保存に失敗したときはhandleSaveを呼ばない", async () => {
		const { user, save, handleSave } = renderFileMode("failed");
		await user.click(screen.getByRole("button", { name: "Save" }));

		await waitFor(() => {
			expect(save).toHaveBeenCalledWith("before.json");
		});
		expect(handleSave).not.toHaveBeenCalled();
	});
});
