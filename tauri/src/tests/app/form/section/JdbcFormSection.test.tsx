import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import JdbcFormSection from "../../../../app/form/section/JdbcFormSection";
import type { JdbcOption } from "../../../../model/CommandOption";
import { makeMinimalParam, workspaceResourcesFixture } from "../../../setup";

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const { mockClearFieldErrors } = vi.hoisted(() => ({
	mockClearFieldErrors: vi.fn(),
}));
vi.mock("../../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
	useResourcesSettings: () => workspaceResourcesFixture.resources,
}));
vi.mock("../../../../context/JdbcConnectionProvider", () => ({
	useSetJdbcConnectionState: () => vi.fn(),
}));
vi.mock("../../../../context/FieldErrorProvider", () => ({
	useFieldErrors: () => [],
	useClearFieldErrors: () => mockClearFieldErrors,
}));
vi.mock("../../../../hooks/useJdbc", () => ({
	useJdbcConnectionTest: () => vi.fn(),
	useSaveJdbcProperties: () => vi.fn(),
}));
// 保存の成否はダイアログのテストで見るため、ここでは保存したファイル名を返すだけにする
vi.mock("../../../../app/form/section/dialog/JdbcSavePropertiesDialog", () => ({
	default: ({ handleSave }: { handleSave: (path: string) => void }) => (
		<button type="button" onClick={() => handleSave("saved.properties")}>
			saved
		</button>
	),
}));

function option(name: string, value: string) {
	return { ...makeMinimalParam(name), value };
}

const jdbcOption: JdbcOption = {
	prefix: "src",
	jdbcProperties: option("jdbcProperties", "base.properties"),
	jdbcUrl: option("jdbcUrl", "jdbc:h2:mem:test"),
	jdbcUser: option("jdbcUser", "user"),
	jdbcPass: option("jdbcPass", "pass"),
};

const inputValue = (name: string) =>
	document.querySelector<HTMLInputElement>(`input[name="-src.${name}"]`)?.value;

describe("JdbcFormSectionのテスト", () => {
	it("Save as Propertiesで保存したファイルをjdbcPropertiesに入れ、url・user・passは入力のまま残すことを確認", async () => {
		const user = userEvent.setup();
		render(<JdbcFormSection jdbcOption={jdbcOption} />);

		await user.click(
			screen.getByRole("button", { name: "Save as Properties" }),
		);
		await user.click(screen.getByRole("button", { name: "saved" }));

		expect(inputValue("jdbcProperties")).toBe("saved.properties");
		expect(inputValue("jdbcUrl")).toBe("jdbc:h2:mem:test");
		expect(inputValue("jdbcUser")).toBe("user");
		expect(inputValue("jdbcPass")).toBe("pass");
		expect(mockClearFieldErrors).toHaveBeenCalledWith("-src.jdbcProperties");
		expect(screen.queryByRole("button", { name: "saved" })).toBeNull();
	});
});
