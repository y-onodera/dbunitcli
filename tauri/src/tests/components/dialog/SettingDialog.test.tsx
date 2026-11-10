import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { SettingDialog } from "../../../components/dialog";
import type { ResourceLocation } from "../../../model/ResourceLocation";
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
		expect(save).toHaveBeenCalledWith("after.json", false);
	});

	it("保存に失敗したときはhandleSaveを呼ばない", async () => {
		const { user, save, handleSave } = renderFileMode("failed");
		await user.click(screen.getByRole("button", { name: "Save" }));

		await waitFor(() => {
			expect(save).toHaveBeenCalledWith("before.json", false);
		});
		expect(handleSave).not.toHaveBeenCalled();
	});

	it("ファイル名が空ならSaveできない", async () => {
		const { user, save } = renderFileMode("success");
		const fileName = document.querySelector(
			'input[name="fileName"]',
		) as HTMLInputElement;
		await user.clear(fileName);

		const saveButton = screen.getByRole("button", { name: "Save" });
		expect(saveButton).toBeDisabled();
		await user.click(saveButton);
		expect(save).not.toHaveBeenCalled();
	});

	it("保存処理が例外を投げたらalertで知らせる", async () => {
		const alertSpy = vi.spyOn(window, "alert").mockImplementation(() => {});
		vi.spyOn(console, "error").mockImplementation(() => {});
		const save = vi.fn().mockRejectedValue(new Error("boom"));
		render(
			<SettingDialog
				fileName="before.json"
				handleDialogClose={vi.fn()}
				save={save}
				handleSave={vi.fn()}
			>
				<div />
			</SettingDialog>,
		);
		await userEvent.setup().click(screen.getByRole("button", { name: "Save" }));

		await waitFor(() => {
			expect(alertSpy).toHaveBeenCalledWith("Save failed: boom");
		});
	});
});

const OUTSIDE_LOCATION: ResourceLocation = {
	folder: "/ws/other",
	defaultFolder: "/ws/resources/sql",
	fileName: "q.sql",
	defaultFolderHasFile: false,
};

function renderWithLocation(
	location: ResourceLocation | null,
	{ fileName = "other/q.sql" } = {},
) {
	const save = vi.fn().mockResolvedValue("success");
	const handleSave = vi.fn();
	render(
		<SettingDialog
			fileName={fileName}
			handleDialogClose={vi.fn()}
			save={save}
			handleSave={handleSave}
			location={location}
		>
			<div />
		</SettingDialog>,
	);
	return { user: userEvent.setup(), save, handleSave };
}

describe("SettingDialogの保存先の選択のテスト", () => {
	it("開いたファイルのフォルダを出し、既定の上書き保存は開いたファイルへ書く", async () => {
		const { user, save, handleSave } = renderWithLocation(OUTSIDE_LOCATION);
		expect(screen.getByText("Opened from: /ws/other")).toBeInTheDocument();
		expect(
			screen.getByRole("radio", { name: "Overwrite the opened file" }),
		).toBeChecked();

		await user.click(screen.getByRole("button", { name: "Save" }));

		await waitFor(() => {
			expect(handleSave).toHaveBeenCalledWith("other/q.sql");
		});
		expect(save).toHaveBeenCalledWith("other/q.sql", true);
	});

	it("規定フォルダを選ぶと同じファイル名で規定フォルダへ新規保存し、入力欄はそのファイル名にする", async () => {
		const { user, save, handleSave } = renderWithLocation(OUTSIDE_LOCATION);
		await user.click(
			screen.getByRole("radio", { name: /Save to the default folder/ }),
		);
		await user.click(screen.getByRole("button", { name: "Save" }));

		await waitFor(() => {
			expect(handleSave).toHaveBeenCalledWith("q.sql");
		});
		expect(save).toHaveBeenCalledWith("q.sql", false);
	});

	it("規定フォルダに同名のファイルがあれば置き換えることを示す", () => {
		renderWithLocation({ ...OUTSIDE_LOCATION, defaultFolderHasFile: true });
		expect(
			screen.getByRole("radio", { name: /replaces the existing q\.sql/ }),
		).toBeInTheDocument();
	});

	it("規定フォルダ直下のファイルは選択肢を出さずに上書きする", async () => {
		const { user, save } = renderWithLocation(
			{
				...OUTSIDE_LOCATION,
				folder: "/ws/resources/sql",
			},
			{ fileName: "q.sql" },
		);
		expect(screen.queryByRole("radio")).toBeNull();
		await user.click(screen.getByRole("button", { name: "Save" }));

		await waitFor(() => {
			expect(save).toHaveBeenCalledWith("q.sql", true);
		});
	});

	it("名前を変えたら選択肢を出さずに入力した名前で新規保存する", async () => {
		const { user, save, handleSave } = renderWithLocation(OUTSIDE_LOCATION);
		const fileName = document.querySelector(
			'input[name="fileName"]',
		) as HTMLInputElement;
		await user.clear(fileName);
		await user.type(fileName, "renamed.sql");

		expect(screen.queryByRole("radio")).toBeNull();
		expect(
			screen.getByText(/Name changed: saved as a new file/),
		).toBeInTheDocument();
		await user.click(screen.getByRole("button", { name: "Save" }));

		await waitFor(() => {
			expect(handleSave).toHaveBeenCalledWith("renamed.sql");
		});
		expect(save).toHaveBeenCalledWith("renamed.sql", false);
	});
});
