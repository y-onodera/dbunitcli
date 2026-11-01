import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import Text from "../../../../../app/form/section/element/Text";
import type { CommandOption } from "../../../../../model/CommandOption";
import { workspaceResourcesFixture } from "../../../../setup";

vi.mock("../../../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
}));

function textElement(value: string): CommandOption {
	return {
		name: "encoding",
		value,
		attribute: {
			type: "TEXT",
			required: false,
			selectOption: [],
			defaultPath: "WORKSPACE",
		},
		optional: false,
	};
}

describe("Textのクリアボタン", () => {
	it("入力があるときはクリアボタンを表示し、クリックで入力を消して変更を通知する", async () => {
		const handleValueChange = vi.fn();
		render(
			<Text
				prefix="src"
				element={textElement("UTF-8")}
				handleValueChange={handleValueChange}
			/>,
		);

		await userEvent.click(
			screen.getByRole("button", { name: "clear -src.encoding" }),
		);

		const input = screen.getByLabelText("-src.encoding");
		expect(input).toHaveValue("");
		expect(input).toHaveFocus();
		expect(handleValueChange).toHaveBeenCalledWith("");
		expect(
			screen.queryByRole("button", { name: "clear -src.encoding" }),
		).not.toBeInTheDocument();
	});

	it("入力が空のときはクリアボタンを表示しない", () => {
		render(<Text prefix="src" element={textElement("")} />);

		expect(
			screen.queryByRole("button", { name: "clear -src.encoding" }),
		).not.toBeInTheDocument();
	});

	it("hiddenのときはクリアボタンを表示しない", () => {
		render(<Text prefix="src" element={textElement("UTF-8")} hidden />);

		expect(
			screen.queryByRole("button", { name: "clear -src.encoding" }),
		).not.toBeInTheDocument();
	});
});
