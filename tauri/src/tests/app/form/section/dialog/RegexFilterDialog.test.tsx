import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { RegexFilterButton } from "../../../../../app/form/section/dialog/RegexFilterDialog";

const { mockUseDatasetTableSources } = vi.hoisted(() => ({
	mockUseDatasetTableSources: vi.fn(),
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableSources: mockUseDatasetTableSources,
}));
vi.mock("../../../../../context/DatasetSrcInfoProvider", () => ({
	useDatasetSrcInfo: () => ({
		values: { src: "/data", regInclude: "old", regTableInclude: "x" },
	}),
	useInDatasetSrcInfoScope: () => true,
}));

// 一覧で値（file・source table列）がkeyの行のチェックボックス
const checkboxOf = (key: string) =>
	within(
		screen.getByRole("cell", { name: key }).closest("tr") as HTMLElement,
	).getByRole("checkbox");

const SOURCES = [
	{
		tableName: "OLD",
		filePath: "old/users.csv",
		matchPath: "/data/old/users.csv",
		sourceTableName: "users",
	},
	{
		tableName: "NEW",
		filePath: "new/users.csv",
		matchPath: "/data/new/users.csv",
		sourceTableName: "users",
	},
	{
		tableName: "other",
		filePath: "other.csv",
		matchPath: "/data/other.csv",
		sourceTableName: "other",
	},
];

function loaded() {
	mockUseDatasetTableSources.mockReturnValue({
		sources: SOURCES,
		loading: false,
		logs: [],
		error: "",
	});
}

async function open(
	kind: "path" | "table",
	fieldName: string,
	value: string,
	onApply = vi.fn(),
) {
	const user = userEvent.setup();
	render(
		<RegexFilterButton
			kind={kind}
			fieldName={fieldName}
			value={value}
			onApply={onApply}
		/>,
	);
	await user.click(
		screen.getByRole("button", { name: `select ${fieldName} from table list` }),
	);
	return user;
}

describe("RegexFilterDialog", () => {
	it("編集する項目を空にしたsrcで読み、ファイルと最終的なテーブル名を並べ、入力中の値に一致する行を選んでおく", async () => {
		loaded();
		await open("path", "regInclude", "/data/old");
		expect(mockUseDatasetTableSources).toHaveBeenCalledWith({
			values: { src: "/data", regInclude: "", regTableInclude: "x" },
		});
		expect(
			screen.getByRole("columnheader", { name: "file" }),
		).toBeInTheDocument();
		expect(screen.getByRole("cell", { name: "OLD" })).toBeInTheDocument();
		expect(checkboxOf("old/users.csv")).toBeChecked();
		expect(checkboxOf("new/users.csv")).not.toBeChecked();
	});

	it("チェックした行で正規表現を組み立てて反映する", async () => {
		loaded();
		const onApply = vi.fn();
		const user = await open("path", "regInclude", "/data/old", onApply);
		await user.click(checkboxOf("new/users.csv"));
		await user.click(screen.getByRole("button", { name: "Apply" }));
		expect(onApply).toHaveBeenCalledWith(
			"(?:^|[\\\\/])(?:old[\\\\/]users\\.csv|new[\\\\/]users\\.csv)$",
		);
	});

	it("テーブル名の項目は元のテーブル名ごとにまとめ、元のテーブル名で組み立てる", async () => {
		loaded();
		const onApply = vi.fn();
		const user = await open("table", "regTableInclude", "", onApply);
		expect(
			screen.getByRole("columnheader", { name: "source table" }),
		).toBeInTheDocument();
		expect(screen.getByRole("cell", { name: "OLD, NEW" })).toBeInTheDocument();
		await user.click(checkboxOf("users"));
		await user.click(screen.getByRole("button", { name: "Apply" }));
		expect(onApply).toHaveBeenCalledWith("^(?:users)$");
	});

	it("読込に失敗したらエラーを表示する", async () => {
		mockUseDatasetTableSources.mockReturnValue({
			sources: [],
			loading: false,
			logs: [],
			error: "boom",
		});
		await open("table", "regTableExclude", "");
		expect(screen.getByText(/boom/)).toBeInTheDocument();
	});
});
