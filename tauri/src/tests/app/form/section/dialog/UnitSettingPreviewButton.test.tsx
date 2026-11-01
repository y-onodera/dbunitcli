import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import UnitSettingPreviewButton from "../../../../../app/form/section/dialog/UnitSettingPreviewButton";
import type {
	UnitSettingRef,
	UnitTablePreview,
} from "../../../../../hooks/useDatasetSettings";
import type { DatasetSrcInfo } from "../../../../../model/CommandOption";
import {
	expectJobFailure,
	JOB_ERROR,
	JOB_LOG_LINES,
} from "../../../../helpers/jobFailure";

// （mockReset:trueで実装が毎回リセットされるため、値は各テスト内で設定する）
const { srcInfoFixture, unitFixture, unitCalls, tableNamesCalls } = vi.hoisted(
	() => ({
		srcInfoFixture: { current: { values: {} } as DatasetSrcInfo },
		unitFixture: {
			previews: [] as UnitTablePreview[],
			error: "",
			logs: [] as string[],
		},
		unitCalls: [] as {
			srcInfo: DatasetSrcInfo;
			tableName: string;
			unitSetting: UnitSettingRef;
		}[],
		tableNamesCalls: [] as DatasetSrcInfo[],
	}),
);
vi.mock("../../../../../context/DatasetSrcInfoProvider", () => ({
	useDatasetSrcInfo: () => srcInfoFixture.current,
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
		unitSetting: UnitSettingRef,
	) => {
		unitCalls.push({ srcInfo, tableName, unitSetting });
		return {
			previews: unitFixture.previews,
			loading: false,
			logs: unitFixture.logs,
			error: unitFixture.error,
		};
	},
}));

const FORM_VALUES = {
	"-generateType": "txt",
	"-unitSettingEncoding": "MS932",
	"-src.srcType": "csv",
	"-src.src": "data/current",
	"-src.setting": "src.json",
};

function renderButton(unitSetting = "unit.json") {
	return render(
		<UnitSettingPreviewButton
			unitSetting={unitSetting}
			formValues={() => FORM_VALUES}
		/>,
	);
}

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
		srcInfoFixture.current = { values: { srcType: "csv", src: "data/old" } };
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

	it("srcTypeが未指定ならボタンを出さない", () => {
		srcInfoFixture.current = { values: {} };
		renderButton();
		expect(
			screen.queryByRole("button", { name: "Preview Unit Settings" }),
		).not.toBeInTheDocument();
	});

	it("押した時点のフォームのsrcで、srcのsettingを適用したテーブル一覧を読む", async () => {
		const user = userEvent.setup();
		renderButton();
		await user.click(
			screen.getByRole("button", { name: "Preview Unit Settings" }),
		);

		expect(screen.getByText("big")).toBeInTheDocument();
		expect(tableNamesCalls.at(-1)).toEqual({
			values: { srcType: "csv", src: "data/current", setting: "src.json" },
		});
	});

	it("テーブルの横のボタンで、unitSettingを適用した後のテーブル一覧を出す", async () => {
		await openUnitTables();

		expect(
			screen.getByText("big — Unit Table List Preview"),
		).toBeInTheDocument();
		expect(screen.getByText("ONE")).toBeInTheDocument();
		expect(unitCalls.at(-1)).toEqual({
			srcInfo: {
				values: { srcType: "csv", src: "data/current", setting: "src.json" },
			},
			tableName: "big",
			unitSetting: { path: "unit.json", encoding: "MS932" },
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
});
