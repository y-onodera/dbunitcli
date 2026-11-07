import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import Text from "../../../../../app/form/section/element/Text";
import type { CommandOption } from "../../../../../model/CommandOption";
import { makeMinimalParam, workspaceResourcesFixture } from "../../../../setup";

vi.mock("../../../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
}));

function textElement(value: string): CommandOption {
	return { ...makeMinimalParam("encoding"), value };
}

function clearButton() {
	return screen.queryByRole("button", { name: "clear -src.encoding" });
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
		expect(clearButton()).not.toBeInTheDocument();
	});

	it.each([
		{ title: "入力が空", value: "", hidden: false },
		{ title: "hidden", value: "UTF-8", hidden: true },
	])("$titleのときはクリアボタンを表示しない", ({ value, hidden }) => {
		render(<Text prefix="src" element={textElement(value)} hidden={hidden} />);

		expect(clearButton()).not.toBeInTheDocument();
	});
});

describe("Textのプレースホルダー", () => {
	it("未入力時に使われる値をプレースホルダーに表示する", () => {
		const element = textElement("");
		render(
			<Text
				prefix="src"
				element={{
					...element,
					attribute: { ...element.attribute, defaultValue: "MS932" },
				}}
			/>,
		);

		expect(screen.getByLabelText("-src.encoding")).toHaveAttribute(
			"placeholder",
			"MS932",
		);
	});
});

describe("Textのchildrenに渡すsetPath", () => {
	it("ボタン等から値を変えたときも入力を更新して変更を通知する", async () => {
		const handleValueChange = vi.fn();
		render(
			<Text
				prefix="src"
				element={textElement("UTF-8")}
				handleValueChange={handleValueChange}
			>
				{({ setPath }) => (
					<button type="button" onClick={() => setPath("MS932")}>
						choose
					</button>
				)}
			</Text>,
		);

		await userEvent.click(screen.getByRole("button", { name: "choose" }));

		expect(screen.getByLabelText("-src.encoding")).toHaveValue("MS932");
		expect(handleValueChange).toHaveBeenCalledWith("MS932");
	});
});
