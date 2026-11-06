import { render } from "@testing-library/react";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";
import { CompareForm } from "../../../app/form/CompareForm";
import { GenerateForm } from "../../../app/form/GenerateForm";
import { ParameterizeForm } from "../../../app/form/ParameterizeForm";
import { RunForm } from "../../../app/form/RunForm";
import type { ResourceEncodings } from "../../../context/ResourceEncodingProvider";
import type { CommandOption } from "../../../model/CommandOption";
import { environmentFixture, workspaceResourcesFixture } from "../../setup";
import {
	compareLoadResponseFixture,
	generateLoadResponseFixture,
	parameterizeLoadResponseFixture,
	runLoadResponseFixture,
} from "./fixtures";

const { providedEncodings } = vi.hoisted(() => ({
	// 描画されたResourceEncodingProviderのencodings（外側から順）
	providedEncodings: [] as ResourceEncodings[],
}));
vi.mock("../../../context/ResourceEncodingProvider", () => ({
	ResourceEncodingProvider: ({
		children,
		encodings,
	}: {
		children: ReactNode;
		encodings: ResourceEncodings;
	}) => {
		providedEncodings.push(encodings);
		return children;
	},
	useResourceEncoding: () => "",
}));

vi.mock("@tauri-apps/plugin-dialog", () => ({ open: vi.fn() }));
vi.mock("@tauri-apps/api", () => ({ core: { invoke: vi.fn() } }));
vi.mock("@tauri-apps/api/path", () => ({
	isAbsolute: vi.fn().mockResolvedValue(false),
	sep: vi.fn().mockReturnValue("/"),
}));
vi.mock("../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
	useResourcesSettings: () => workspaceResourcesFixture.resources,
	useSetResourcesSettings: () => vi.fn(),
}));
vi.mock("../../../context/EnvironmentProvider", () => ({
	useEnvironment: () => environmentFixture,
}));
vi.mock("../../../hooks/useJdbc", () => ({
	useJdbcConnectionTest: () => vi.fn(),
	useSaveJdbcProperties: () => vi.fn(),
}));

function withValue<T extends CommandOption | undefined>(
	option: T,
	value: string,
): T {
	return { ...(option as CommandOption), value } as T;
}

// template optionのencodingの値を差し替える
function withTemplateEncoding<T extends { templateOption?: object }>(
	options: T,
	value: string,
): T {
	const templateOption = options.templateOption as { encoding?: CommandOption };
	return {
		...options,
		templateOption: {
			...templateOption,
			encoding: withValue(templateOption.encoding, value),
		},
	};
}

// 描画したフォームが置いたProviderのencodings（重複を除き、外側から順）
function renderedEncodings(form: ReactNode): ResourceEncodings[] {
	providedEncodings.length = 0;
	render(form);
	const unique = new Map(
		providedEncodings.map((it) => [JSON.stringify(it), it]),
	);
	return [...unique.values()];
}

// DatasetLoadFormは-<prefix>.settingEncodingで設定ファイルを、-<prefix>.encodingでSQL・templateGroupを読み書きする
function datasetLoadEncodings(
	encoding: string,
	settingEncoding: string,
): ResourceEncodings {
	return {
		datasetSetting: settingEncoding,
		xlsxSchema: settingEncoding,
		sql: encoding,
		table: encoding,
		csvq: encoding,
		template: encoding,
	};
}

const handleTypeSelect = () => Promise.resolve();

describe("各フォームがリソースの編集に渡す文字コード", () => {
	it("compareは-settingEncodingで比較キーの設定を、各データセットはその文字コードオプションで読み書きする", () => {
		const fixture = compareLoadResponseFixture;
		const encodings = renderedEncodings(
			<CompareForm
				handleTypeSelect={handleTypeSelect}
				name="test"
				compare={{
					...fixture,
					settingEncoding: withValue(fixture.settingEncoding, "C"),
					newData: {
						...fixture.newData,
						encoding: withValue(fixture.newData.encoding, "N"),
						settingEncoding: withValue(fixture.newData.settingEncoding, "NS"),
					},
					oldData: {
						...fixture.oldData,
						encoding: withValue(fixture.oldData.encoding, "O"),
						settingEncoding: withValue(fixture.oldData.settingEncoding, "OS"),
					},
				}}
			/>,
		);
		expect(encodings.slice(0, 3)).toStrictEqual([
			{ compareSetting: "C" },
			datasetLoadEncodings("N", "NS"),
			datasetLoadEncodings("O", "OS"),
		]);
	});

	it("generateは-unitSettingEncodingでunitSettingを、template optionのencodingで-templateを、srcはその文字コードオプションで読み書きする", () => {
		const fixture = generateLoadResponseFixture;
		const encodings = renderedEncodings(
			<GenerateForm
				handleTypeSelect={handleTypeSelect}
				name="test"
				generate={withTemplateEncoding(
					{
						...fixture,
						unitSettingEncoding: withValue(
							fixture.srcData.encoding as CommandOption,
							"U",
						),
						srcData: {
							...fixture.srcData,
							encoding: withValue(fixture.srcData.encoding, "S"),
							settingEncoding: withValue(fixture.srcData.settingEncoding, "SS"),
						},
					},
					"T",
				)}
				scaffoldPrefill={vi.fn()}
				handleScaffoldReflect={vi.fn()}
			/>,
		);
		expect(encodings).toStrictEqual([
			{ generateSetting: "U", template: "T" },
			datasetLoadEncodings("S", "SS"),
		]);
	});

	it("parameterizeはtemplate optionのencodingで-templateとtemplateGroupを、paramはその文字コードオプションで読み書きする", () => {
		const fixture = parameterizeLoadResponseFixture;
		const encodings = renderedEncodings(
			<ParameterizeForm
				handleTypeSelect={handleTypeSelect}
				name="test"
				parameterize={withTemplateEncoding(
					{
						...fixture,
						paramData: {
							...fixture.paramData,
							encoding: withValue(fixture.paramData.encoding, "P"),
							settingEncoding: withValue(
								fixture.paramData.settingEncoding,
								"PS",
							),
						},
					},
					"T",
				)}
			/>,
		);
		expect(encodings).toStrictEqual([
			{ parameterizeTemplate: "T", template: "T" },
			datasetLoadEncodings("P", "PS"),
		]);
	});

	it("runはtemplate optionのencodingでSQLとtemplateGroupを読み書きする", () => {
		const fixture = runLoadResponseFixture;
		const encodings = renderedEncodings(
			<RunForm
				handleTypeSelect={handleTypeSelect}
				name="test"
				run={withTemplateEncoding(
					{
						...fixture,
					},
					"T",
				)}
			/>,
		);
		expect(encodings).toStrictEqual([{ sql: "T", template: "T" }]);
	});
});
