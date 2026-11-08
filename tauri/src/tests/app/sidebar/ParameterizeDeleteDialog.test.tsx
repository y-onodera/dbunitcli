import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import NameEditMenu from "../../../app/sidebar/NameEditMenu";
import ParameterizeDeleteDialog from "../../../app/sidebar/ParameterizeDeleteDialog";
import type { ParameterizeFile } from "../../../hooks/useSelectParameter";

// （mockReset:trueで実装が毎回リセットされるため、resolve値は各テスト内で設定する）
const {
	mockDeleteParameterize,
	mockLoadFiles,
	mockHandleDelete,
	mockOnDelete,
} = vi.hoisted(() => ({
	mockDeleteParameterize: vi.fn(),
	mockLoadFiles: vi.fn(),
	mockHandleDelete: vi.fn(),
	mockOnDelete: vi.fn(),
}));
// 取得したファイルでonLoadを呼ぶ
const loadFilesWith = (loaded: ParameterizeFile[]) =>
	mockLoadFiles.mockImplementation(
		(_name: string, onLoad: (files: ParameterizeFile[]) => void) => {
			onLoad(loaded);
			return Promise.resolve("success");
		},
	);
vi.mock("../../../hooks/useSelectParameter", () => ({
	useDeleteParameterize: () => mockDeleteParameterize,
	useLoadParameterizeFiles: () => mockLoadFiles,
	useParameterizeFrom: () => vi.fn(),
	useParameterizeCandidates: () => [],
	useParameterActions: () => ({
		handleDelete: mockHandleDelete,
		handleCopy: vi.fn(),
		handleRename: vi.fn(),
	}),
}));

const files: ParameterizeFile[] = [
	{ option: "-template", path: "/ws/template/a.txt", usedBy: [] },
	{ option: "-param.src", path: "/ws/a.csv", usedBy: ["other"] },
];

const editName = (command: string) => ({
	name: "a",
	command,
	x: 0,
	y: 0,
	afterEdge: false,
});

describe("ParameterizeDeleteDialogのテスト", () => {
	it("他から参照されないファイルだけ最初にチェックし、参照されるファイルはチェックできない", () => {
		render(
			<ParameterizeDeleteDialog
				name="a"
				files={files}
				onDelete={mockOnDelete}
				onClose={vi.fn()}
			/>,
		);

		expect(
			screen.getByRole("checkbox", { name: "/ws/template/a.txt" }),
		).toBeChecked();
		const shared = screen.getByRole("checkbox", { name: "/ws/a.csv" });
		expect(shared).not.toBeChecked();
		expect(shared).toBeDisabled();
		expect(screen.getByText("used by other")).toBeInTheDocument();
	});

	it("チェックしたファイルを指定して削除し、外せばオプションだけ削除する", async () => {
		const onClose = vi.fn();
		const { unmount } = render(
			<ParameterizeDeleteDialog
				name="a"
				files={files}
				onDelete={mockOnDelete}
				onClose={onClose}
			/>,
		);
		await userEvent.click(screen.getByRole("button", { name: "Delete" }));
		expect(mockOnDelete).toHaveBeenCalledWith(["/ws/template/a.txt"]);
		expect(onClose).toHaveBeenCalled();
		unmount();

		render(
			<ParameterizeDeleteDialog
				name="a"
				files={files}
				onDelete={mockOnDelete}
				onClose={vi.fn()}
			/>,
		);
		await userEvent.click(
			screen.getByRole("checkbox", { name: "/ws/template/a.txt" }),
		);
		await userEvent.click(screen.getByRole("button", { name: "Delete" }));
		expect(mockOnDelete).toHaveBeenLastCalledWith([]);
	});

	it("Closeでは何も削除しない", async () => {
		const onClose = vi.fn();
		render(
			<ParameterizeDeleteDialog
				name="a"
				files={files}
				onDelete={mockOnDelete}
				onClose={onClose}
			/>,
		);

		await userEvent.click(screen.getByRole("button", { name: "Close" }));

		expect(onClose).toHaveBeenCalled();
		expect(mockOnDelete).not.toHaveBeenCalled();
	});
});

describe("NameEditMenuの削除のテスト", () => {
	it("parameterizeは参照するファイルがあれば確認ダイアログを開く", async () => {
		loadFilesWith(files);
		render(
			<NameEditMenu
				editName={editName("parameterize")}
				setEditName={vi.fn()}
			/>,
		);

		await userEvent.click(screen.getAllByTitle("delete")[0]);

		expect(mockLoadFiles).toHaveBeenCalledWith("a", expect.any(Function));
		expect(await screen.findByText("Delete a")).toBeInTheDocument();
		expect(mockDeleteParameterize).not.toHaveBeenCalled();
	});

	it("parameterizeは参照するファイルが無ければ確認せずに削除する", async () => {
		loadFilesWith([]);
		render(
			<NameEditMenu
				editName={editName("parameterize")}
				setEditName={vi.fn()}
			/>,
		);

		await userEvent.click(screen.getAllByTitle("delete")[0]);

		await waitFor(() =>
			expect(mockDeleteParameterize).toHaveBeenCalledWith("a", []),
		);
		expect(screen.queryByText("Delete a")).not.toBeInTheDocument();
	});

	it("parameterize以外はファイルを調べずに削除する", async () => {
		render(
			<NameEditMenu editName={editName("compare")} setEditName={vi.fn()} />,
		);

		await userEvent.click(screen.getAllByTitle("delete")[0]);

		expect(mockHandleDelete).toHaveBeenCalled();
		expect(mockLoadFiles).not.toHaveBeenCalled();
	});
});
