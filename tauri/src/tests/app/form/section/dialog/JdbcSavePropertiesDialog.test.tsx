import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import JdbcSavePropertiesDialog from "../../../../../app/form/section/dialog/JdbcSavePropertiesDialog";

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { loadCalls } = vi.hoisted(() => ({
	loadCalls: [] as { kind: string; name: string }[],
}));
vi.mock("../../../../../hooks/useResourceFiles", () => ({
	CONTENT_PARSER: {},
	useResourceLoad: (kind: string, name: string) => {
		loadCalls.push({ kind, name });
		return {
			data: "",
			location:
				name === ""
					? null
					: {
							folder: "/ws",
							defaultFolder: "/ws/resources/jdbc",
							fileName: "base.properties",
							defaultFolderHasFile: false,
						},
			loading: false,
		};
	},
}));

function renderDialog(jdbcProperties: string) {
	const save = vi.fn().mockResolvedValue("success");
	const handleSave = vi.fn();
	render(
		<JdbcSavePropertiesDialog
			jdbcValues={{ jdbcProperties, jdbcUrl: "jdbc:h2:mem:test" }}
			handleDialogClose={vi.fn()}
			save={save}
			handleSave={handleSave}
		/>,
	);
	return { user: userEvent.setup(), save, handleSave };
}

describe("JdbcSavePropertiesDialogのテスト", () => {
	it("jdbcPropertiesのファイルを開いたファイルとして読み、ファイル名の初期値にすることを確認", () => {
		loadCalls.length = 0;
		renderDialog("base.properties");
		expect(loadCalls).toContainEqual({ kind: "jdbc", name: "base.properties" });
		expect(document.querySelector<HTMLInputElement>("#fileName")?.value).toBe(
			"base.properties",
		);
		expect(screen.getByText("Opened from: /ws")).toBeDefined();
	});

	it("規定フォルダ外の元のファイルへ上書きを選べることを確認", async () => {
		const { user, save, handleSave } = renderDialog("base.properties");
		await user.click(
			screen.getByRole("radio", { name: "Overwrite the opened file" }),
		);
		await user.click(screen.getByRole("button", { name: "Save" }));
		await waitFor(() => {
			expect(handleSave).toHaveBeenCalledWith("base.properties");
		});
		expect(save).toHaveBeenCalledWith("base.properties", true);
	});

	it("規定フォルダへの保存を選ぶと、同じファイル名で新規保存することを確認", async () => {
		const { user, save } = renderDialog("base.properties");
		await user.click(
			screen.getByRole("radio", { name: /Save to the default folder/ }),
		);
		await user.click(screen.getByRole("button", { name: "Save" }));
		await waitFor(() => {
			expect(save).toHaveBeenCalledWith("base.properties", false);
		});
	});

	it("jdbcPropertiesが無ければ、空のファイル名から新規保存することを確認", () => {
		renderDialog("");
		expect(document.querySelector<HTMLInputElement>("#fileName")?.value).toBe(
			"",
		);
		expect(screen.queryByRole("radio")).toBeNull();
		expect(screen.queryByText(/Opened from/)).toBeNull();
	});
});
