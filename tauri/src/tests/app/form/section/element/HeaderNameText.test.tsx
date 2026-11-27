import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import HeaderNameText from "../../../../../app/form/section/element/HeaderNameText";
import type { CommandOption } from "../../../../../model/CommandOption";
import { makeMinimalParam, workspaceResourcesFixture } from "../../../../setup";

vi.mock("../../../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
}));

function headerName(value: string): CommandOption {
	return { ...makeMinimalParam("headerName"), value };
}

const fillButton = () =>
	screen.queryByRole("button", {
		name: "fill blank columns of -src.headerName",
	});

describe("HeaderNameText", () => {
	it("ブランク列があれば位置をエラー表示し、編集ボタンを出す", () => {
		render(<HeaderNameText prefix="src" element={headerName("a,,b,")} />);

		expect(screen.getByText("blank column name at: 2, 4")).toBeInTheDocument();
		expect(fillButton()).toBeInTheDocument();
	});

	it("ブランク列が無ければエラーも編集ボタンも出さない", () => {
		render(<HeaderNameText prefix="src" element={headerName("a,b")} />);

		expect(screen.queryByText(/blank column name/)).not.toBeInTheDocument();
		expect(fillButton()).not.toBeInTheDocument();
	});

	it("hiddenのときはエラーを出さない", () => {
		render(<HeaderNameText prefix="src" element={headerName("a,,b")} hidden />);

		expect(screen.queryByText(/blank column name/)).not.toBeInTheDocument();
	});

	it("入力するとその場でエラーを更新する", async () => {
		render(<HeaderNameText prefix="src" element={headerName("a,b")} />);

		await userEvent.type(screen.getByLabelText("-src.headerName"), ",,c");

		expect(screen.getByText("blank column name at: 3")).toBeInTheDocument();
	});

	it("ダイアログで指定したデフォルト名+連番でブランク列を埋めて変更を通知する", async () => {
		const handleValueChange = vi.fn();
		render(
			<HeaderNameText
				prefix="src"
				element={headerName("a,,b,")}
				handleValueChange={handleValueChange}
			/>,
		);

		await userEvent.click(fillButton() as HTMLElement);
		const defaultName = screen.getByLabelText("default name*");
		expect(defaultName).toHaveValue("col");
		await userEvent.clear(defaultName);
		await userEvent.type(defaultName, "x");
		expect(screen.getByText("a,x1,b,x2")).toBeInTheDocument();
		await userEvent.click(screen.getByRole("button", { name: "Apply" }));

		expect(screen.getByLabelText("-src.headerName")).toHaveValue("a,x1,b,x2");
		expect(handleValueChange).toHaveBeenLastCalledWith("a,x1,b,x2");
		expect(screen.queryByText(/blank column name/)).not.toBeInTheDocument();
		expect(fillButton()).not.toBeInTheDocument();
	});

	it("デフォルト名が空ならApplyできない", async () => {
		render(<HeaderNameText prefix="src" element={headerName("a,,b")} />);

		await userEvent.click(fillButton() as HTMLElement);
		await userEvent.clear(screen.getByLabelText("default name*"));

		expect(screen.getByRole("button", { name: "Apply" })).toBeDisabled();
	});
});
