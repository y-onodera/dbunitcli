import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import NameEditMenu from "../../../app/sidebar/NameEditMenu";
import ParameterizeOptionDialog from "../../../app/sidebar/ParameterizeOptionDialog";

const { mockParameterizeFrom, candidatesFixture } = vi.hoisted(() => ({
	mockParameterizeFrom: vi.fn(),
	candidatesFixture: {
		current: [] as { key: string; value: string }[],
	},
}));
vi.mock("../../../hooks/useSelectParameter", () => ({
	useParameterizeCandidates: () => ({
		candidates: candidatesFixture.current,
		loading: false,
	}),
	useParameterizeFrom: () => mockParameterizeFrom,
	useParameterActions: () => ({
		handleDelete: vi.fn(),
		handleCopy: vi.fn(),
		handleRename: vi.fn(),
	}),
}));

const candidates = [
	{ key: "-new.src", value: "multi1.csv" },
	{ key: "-new.srcType", value: "csv" },
	{ key: "-old.src", value: "multi2.csv" },
];

describe("ParameterizeOptionDialogのテスト", () => {
	it("保存値を表示し、初期状態は全オプションをパラメータ化する", async () => {
		candidatesFixture.current = candidates;
		const onApply = vi.fn();
		const onClose = vi.fn();
		render(
			<ParameterizeOptionDialog
				command="compare"
				name="csvCsv"
				onApply={onApply}
				onClose={onClose}
			/>,
		);

		expect(screen.getByText("multi1.csv")).toBeInTheDocument();
		await userEvent.click(screen.getByRole("button", { name: "Parameterize" }));

		expect(onApply).toHaveBeenCalledWith([
			"-new.src",
			"-new.srcType",
			"-old.src",
		]);
		expect(onClose).toHaveBeenCalled();
	});

	it("チェックを外したオプションは送らず、送る順は保存順に揃える", async () => {
		candidatesFixture.current = candidates;
		const onApply = vi.fn();
		render(
			<ParameterizeOptionDialog
				command="compare"
				name="csvCsv"
				onApply={onApply}
				onClose={vi.fn()}
			/>,
		);

		await userEvent.click(screen.getByRole("checkbox", { name: "select all" }));
		await userEvent.click(screen.getByRole("checkbox", { name: "-old.src" }));
		await userEvent.click(screen.getByRole("checkbox", { name: "-new.src" }));
		await userEvent.click(screen.getByRole("button", { name: "Parameterize" }));

		expect(onApply).toHaveBeenCalledWith(["-new.src", "-old.src"]);
	});

	it("1つも選んでいなければParameterizeを押せない", async () => {
		candidatesFixture.current = candidates;
		render(
			<ParameterizeOptionDialog
				command="compare"
				name="csvCsv"
				onApply={vi.fn()}
				onClose={vi.fn()}
			/>,
		);

		await userEvent.click(screen.getByRole("checkbox", { name: "select all" }));

		expect(screen.getByRole("button", { name: "Parameterize" })).toBeDisabled();
	});
});

describe("NameEditMenuのParameterizeのテスト", () => {
	it("メニューを閉じてダイアログを開き、選んだオプションでparameterizeする", async () => {
		candidatesFixture.current = candidates;
		const setEditName = vi.fn();
		const { rerender } = render(
			<NameEditMenu
				editName={{
					name: "csvCsv",
					command: "compare",
					x: 0,
					y: 0,
					afterEdge: false,
				}}
				setEditName={setEditName}
			/>,
		);

		await userEvent.click(screen.getByTitle("Parameterize"));
		expect(setEditName).toHaveBeenCalledWith({});
		expect(mockParameterizeFrom).not.toHaveBeenCalled();

		rerender(<NameEditMenu editName={{} as never} setEditName={setEditName} />);
		await userEvent.click(
			screen.getByRole("checkbox", { name: "-new.srcType" }),
		);
		await userEvent.click(screen.getByRole("button", { name: "Parameterize" }));

		expect(mockParameterizeFrom).toHaveBeenCalledWith("compare", "csvCsv", [
			"-new.src",
			"-old.src",
		]);
		expect(
			screen.queryByRole("button", { name: "Parameterize" }),
		).not.toBeInTheDocument();
	});
});
