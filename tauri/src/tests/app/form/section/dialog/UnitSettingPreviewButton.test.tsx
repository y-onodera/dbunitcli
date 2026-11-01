import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import type { ReactNode } from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import UnitSettingPreviewButton, {
	UnitSettingsDialog,
} from "../../../../../app/form/section/dialog/UnitSettingPreviewButton";
import type { SaveThen } from "../../../../../components/dialog";
import type { UnitTablePreview } from "../../../../../hooks/useDatasetSettings";
import type { DatasetSrcInfo } from "../../../../../model/CommandOption";
import {
	expectJobFailure,
	JOB_ERROR,
	JOB_LOG_LINES,
} from "../../../../helpers/jobFailure";

// （mockReset:trueで実装が毎回リセットされるため、値は各テスト内で設定する）
const {
	srcInfoFixture,
	encodingFixture,
	unitFixture,
	unitCalls,
	tableNamesCalls,
} = vi.hoisted(() => ({
	srcInfoFixture: { current: { values: {} } as DatasetSrcInfo },
	encodingFixture: { current: "" },
	unitFixture: {
		previews: [] as UnitTablePreview[],
		error: "",
		logs: [] as string[],
	},
	unitCalls: [] as {
		srcInfo: DatasetSrcInfo;
		tableName: string;
		unitSetting: string;
		encoding: string;
	}[],
	tableNamesCalls: [] as DatasetSrcInfo[],
}));
vi.mock("../../../../../context/DatasetSrcInfoProvider", () => ({
	useDatasetSrcInfo: () => srcInfoFixture.current,
}));
vi.mock("../../../../../context/UnitSettingEncodingProvider", () => ({
	useUnitSettingEncoding: () => encodingFixture.current,
}));
// 編集ダイアログ本体は使わず、footerのSave and Previewだけを出す（保存は"saved.json"へ成功したものとする）。
// 保存先の種類はdata-kindに出して確認する
vi.mock("../../../../../app/form/section/dialog/DatasetSettingsDialog", () => ({
	default: (props: {
		saveAndPreview?: (saveThen: SaveThen) => ReactNode;
		kind?: string;
	}) => (
		<div data-testid="dataset-settings-dialog" data-kind={props.kind}>
			{props.saveAndPreview?.((onSaved) => onSaved("saved.json"))}
		</div>
	),
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableNames: (srcInfo: DatasetSrcInfo) => {
		tableNamesCalls.push(srcInfo);
		return { tableNames: ["big"], loading: false, logs: [], error: "" };
	},
	useDatasetTablePreview: () => ({
		preview: null,
		loading: false,
		logs: [],
		error: "",
	}),
	useDatasetTableConvert: () => vi.fn(),
	useDatasetUnitTablePreview: (
		srcInfo: DatasetSrcInfo,
		tableName: string,
		unitSetting: string,
		encoding: string,
	) => {
		unitCalls.push({ srcInfo, tableName, unitSetting, encoding });
		return {
			previews: unitFixture.previews,
			loading: false,
			logs: unitFixture.logs,
			error: unitFixture.error,
		};
	},
}));

const SRC_INFO: DatasetSrcInfo = {
	values: { srcType: "csv", src: "data/current", setting: "src.json" },
};

function renderButton(unitSetting = "unit.json") {
	return render(<UnitSettingPreviewButton unitSetting={unitSetting} />);
}

const lastOf = <T,>(items: T[]): T | undefined => items[items.length - 1];

async function openUnitTables() {
	const user = userEvent.setup();
	renderButton();
	await user.click(
		screen.getByRole("button", { name: "Preview Unit Settings" }),
	);
	await user.click(screen.getByRole("button", { name: "Unit Preview" }));
	return user;
}

describe("UnitSettingPreviewButtonのテスト", () => {
	beforeEach(() => {
		srcInfoFixture.current = SRC_INFO;
		encodingFixture.current = "MS932";
		unitFixture.previews = [
			{ tableName: "big", headers: ["key"], rows: [["1"]], totalRows: 1 },
			{
				tableName: "ONE",
				headers: ["key", "label"],
				rows: [["1", "L1"]],
				totalRows: 1,
			},
		];
		unitFixture.error = "";
		unitFixture.logs = [];
		unitCalls.length = 0;
		tableNamesCalls.length = 0;
	});

	it("unitSettingが空ならボタンを出さない", () => {
		renderButton("");
		expect(
			screen.queryByRole("button", { name: "Preview Unit Settings" }),
		).not.toBeInTheDocument();
	});

	it.each<[string, DatasetSrcInfo]>([
		["srcTypeが未指定", { values: { src: "data/current" } }],
		["srcが空", { values: { srcType: "csv", src: "" } }],
	])("%sならボタンを出さない", (_, info) => {
		srcInfoFixture.current = info;
		renderButton();
		expect(
			screen.queryByRole("button", { name: "Preview Unit Settings" }),
		).not.toBeInTheDocument();
	});

	it("srcの入力値で、srcのsettingを適用したテーブル一覧を読む", async () => {
		const user = userEvent.setup();
		renderButton();
		await user.click(
			screen.getByRole("button", { name: "Preview Unit Settings" }),
		);

		expect(screen.getByText("big")).toBeInTheDocument();
		expect(lastOf(tableNamesCalls)).toEqual(SRC_INFO);
	});

	it("テーブルの横のボタンで、unitSettingを適用した後のテーブル一覧を出す", async () => {
		await openUnitTables();

		expect(
			screen.getByText("big — Unit Table List Preview"),
		).toBeInTheDocument();
		expect(screen.getByText("ONE")).toBeInTheDocument();
		expect(lastOf(unitCalls)).toEqual({
			srcInfo: SRC_INFO,
			tableName: "big",
			unitSetting: "unit.json",
			encoding: "MS932",
		});
	});

	it("unitSetting適用後のテーブルの横のボタンで、そのテーブルのデータを出す", async () => {
		const user = await openUnitTables();
		const previewButtons = screen.getAllByRole("button", { name: "preview" });
		// 先頭はsrcのテーブル一覧（big）のボタン。unitSetting適用後の一覧はbig・ONEの順
		await user.click(previewButtons[previewButtons.length - 1]);

		expect(screen.getByText("ONE — Data Preview")).toBeInTheDocument();
		expect(screen.getByText("L1")).toBeInTheDocument();
		expect(screen.getByText("1 rows")).toBeInTheDocument();
	});

	it("unitSettingの適用に失敗したら、エラーと読込中のログを出す", async () => {
		unitFixture.previews = [];
		unitFixture.error = JOB_ERROR;
		unitFixture.logs = JOB_LOG_LINES;
		await openUnitTables();

		expectJobFailure();
	});

	describe("unitSettingの編集ダイアログのSave and Preview", () => {
		const renderDialog = () =>
			render(
				<UnitSettingsDialog
					fileName="unit.json"
					kind="generateSetting"
					handleDialogClose={vi.fn()}
					handleSave={vi.fn()}
				/>,
			);

		it("保存したファイルをunitSettingとして適用してプレビューする", async () => {
			const user = userEvent.setup();
			renderDialog();
			await user.click(
				screen.getByRole("button", { name: "Save and Preview" }),
			);
			await user.click(screen.getByRole("button", { name: "Unit Preview" }));

			expect(lastOf(tableNamesCalls)).toEqual(SRC_INFO);
			expect(lastOf(unitCalls)).toEqual({
				srcInfo: SRC_INFO,
				tableName: "big",
				unitSetting: "saved.json",
				encoding: "MS932",
			});
		});

		it("受け取ったkind（読込・保存先）をDataset Settingsへ渡す", () => {
			renderDialog();
			expect(screen.getByTestId("dataset-settings-dialog")).toHaveAttribute(
				"data-kind",
				"generateSetting",
			);
		});

		it("srcが空ならSave and Previewを出さない", () => {
			srcInfoFixture.current = { values: { srcType: "csv", src: "" } };
			renderDialog();
			expect(
				screen.queryByRole("button", { name: "Save and Preview" }),
			).not.toBeInTheDocument();
		});
	});
});
