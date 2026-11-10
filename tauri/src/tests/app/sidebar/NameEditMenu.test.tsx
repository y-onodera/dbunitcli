import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import NameEditMenu from "../../../app/sidebar/NameEditMenu";

const { mockParameterActions } = vi.hoisted(() => ({
	mockParameterActions: vi.fn(),
}));
vi.mock("../../../hooks/useSelectParameter", () => ({
	useParameterizeCandidates: () => ({ candidates: [], loading: false }),
	useParameterizeFrom: () => vi.fn(),
	useLoadParameterizeFiles: () => vi.fn(),
	useDeleteParameterize: () => vi.fn(),
	useParameterActions: (command: string, name: string) => {
		mockParameterActions(command, name);
		return {
			handleDelete: vi.fn(),
			handleCopy: vi.fn(),
			handleRename: vi.fn(),
		};
	},
}));

const editName = {
	name: "csvCsv",
	command: "compare",
	x: 0,
	y: 0,
	afterEdge: false,
};

describe("NameEditMenuのテスト", () => {
	it("閉じている（null）ときはメニューを出さず、パラメータの操作も用意しない", () => {
		render(<NameEditMenu editName={null} setEditName={vi.fn()} />);

		expect(screen.queryByTitle("delete")).not.toBeInTheDocument();
		expect(mockParameterActions).not.toHaveBeenCalled();
	});

	it("開いたら選んだcommandと名前で操作を用意する", () => {
		render(<NameEditMenu editName={editName} setEditName={vi.fn()} />);

		expect(screen.getByTitle("delete")).toBeInTheDocument();
		expect(mockParameterActions).toHaveBeenCalledWith("compare", "csvCsv");
	});

	it("メニューの外をクリックしたら閉じる（nullにする）", async () => {
		const setEditName = vi.fn();
		render(
			<>
				<p>outside</p>
				<NameEditMenu editName={editName} setEditName={setEditName} />
			</>,
		);

		await userEvent.click(screen.getByText("outside"));

		expect(setEditName).toHaveBeenCalledWith(null);
	});
});
