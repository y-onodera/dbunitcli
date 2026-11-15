import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { GenerateForm } from "../../../app/form/GenerateForm";
import type { CommandOption } from "../../../model/CommandOption";
import type { GenerateOptions } from "../../../model/SelectParameter";
import { environmentFixture, workspaceResourcesFixture } from "../../setup";
import {
	generateLoadResponseFixture,
	generateRefreshGenerateTypeSqlResponseFixture,
	generateRefreshGenerateTypeXlsResponseFixture,
	generateRefreshGenerateTypeXlsxResponseFixture,
	generateRefreshSrcTypeTableResponseFixture,
	generateRefreshXlsxTemplateCompareResponseFixture,
	generateRefreshXlsxTemplateMetaDataResponseFixture,
} from "./fixtures";

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
// srcの入力値で呼ばれたときだけ候補を返す（読む前は値の無いsrcInfoで呼ばれる）
const { useDatasetTableNamesMock } = vi.hoisted(() => ({
	useDatasetTableNamesMock: vi.fn(
		(srcInfo: { values: Record<string, string> }) => ({
			tableNames:
				Object.keys(srcInfo.values).length > 0
					? ["OLD_USERS", "NEW_USERS"]
					: [],
			loading: false,
			logs: [],
			error: "",
		}),
	),
}));
// OLD_USERSテーブルの列だけを返す
const { useDatasetTableColumnsMock } = vi.hoisted(() => ({
	useDatasetTableColumnsMock: vi.fn((_srcInfo: unknown, tableName: string) => ({
		columns: tableName === "OLD_USERS" ? ["ID", "NAME", "AMT"] : [],
		loading: false,
		logs: [],
		error: "",
	})),
}));
vi.mock("../../../hooks/useDatasetSettings", async (importOriginal) => ({
	...(await importOriginal<
		typeof import("../../../hooks/useDatasetSettings")
	>()),
	useDatasetTableNames: useDatasetTableNamesMock,
	useDatasetTableColumns: useDatasetTableColumnsMock,
}));

function makeGenerateProps(
	fixture: GenerateOptions = generateLoadResponseFixture,
): {
	handleTypeSelect: () => Promise<void>;
	name: string;
	generate: GenerateOptions;
	scaffoldPrefill: () => { [k: string]: FormDataEntryValue };
	handleScaffoldReflect: (params: Record<string, string>) => Promise<void>;
} {
	return {
		handleTypeSelect: vi.fn().mockResolvedValue(undefined),
		name: "test",
		generate: fixture,
		scaffoldPrefill: vi.fn().mockReturnValue({ "-target": "ddl" }),
		handleScaffoldReflect: vi.fn().mockResolvedValue(undefined),
	};
}

// template欄のdropdownを開き、指定した項目の有無を返す
async function templateMenuItem(name: string): Promise<HTMLElement | null> {
	const templateInput = document.querySelector('input[name="-template"]');
	const menuButton = templateInput
		?.closest("div.flex")
		?.querySelector('button[aria-label="setting"]');
	if (!menuButton) {
		return null;
	}
	await userEvent.setup().click(menuButton);
	return screen.queryByRole("button", { name });
}

describe("GenerateFormの描画テスト", () => {
	describe("csvソース（loadレスポンス）", () => {
		it("generate・template・src・traversalのセクションがこの順で表示される", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			const legends = document.querySelectorAll("fieldset legend");
			expect(legends[0]).toHaveTextContent("generate");
			expect(legends[1]).toHaveTextContent("template");
			expect(legends[2]).toHaveTextContent("src");
			expect(legends[3]).toHaveTextContent("traversal");
		});

		it("generateセクションにgenerateType・unit・template・result・resultPath・outputEncodingが含まれる", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			expect(
				document.querySelector('select[name="-generateType"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('select[name="-unit"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-template"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-result"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-resultPath"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-outputEncoding"]'),
			).toBeInTheDocument();
		});

		it("parallelがgenerateセクションに表示される（並列化できる組み合わせのときだけレスポンスに含まれる）", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-parallel"]'),
			).toBeInTheDocument();
		});

		it("parallelがレスポンスに無いときは表示しない", () => {
			const { parallel: _, ...withoutParallel } = generateLoadResponseFixture;
			render(
				<GenerateForm
					{...makeGenerateProps(withoutParallel as GenerateOptions)}
				/>,
			);

			expect(document.querySelector('[name="-parallel"]')).toBeNull();
		});

		it("templateセクションにencodingが含まれる", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-template.encoding"]'),
			).toBeInTheDocument();
		});

		it("srcセクションにsrcType・src・encodingが含まれる", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			expect(
				document.querySelector('select[name="-src.srcType"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-src.src"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-src.encoding"]'),
			).toBeInTheDocument();
		});

		it("traversal option要素（recursive等）は初期状態でhiddenになっている", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			expect(
				document.querySelector('input[type="checkbox"][name="-src.recursive"]'),
			).not.toBeVisible();
			expect(
				document.querySelector('input[type="text"][name="-src.regInclude"]'),
			).not.toBeVisible();
		});

		it("csv option要素（delimiter等）は初期状態で表示される", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-src.delimiter"]'),
			).toBeVisible();
		});

		it("generateType=txtのときtemplateのdropdownにedit項目が表示され、generate項目は表示されない", async () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			expect(await templateMenuItem("edit")).toBeInTheDocument();
			expect(screen.queryByRole("button", { name: "generate" })).toBeNull();
		});

		it("generateType=txtのときunitの上にScaffoldボタンが表示される", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			const button = screen.getByRole("button", { name: "Scaffold" });
			const unitSelect = document.querySelector('select[name="-unit"]');
			expect(button).toBeInTheDocument();
			expect(
				button.compareDocumentPosition(unitSelect as Node) &
					Node.DOCUMENT_POSITION_FOLLOWING,
			).toBeTruthy();
		});
	});

	describe("tableソース（srcType=tableのrefreshレスポンス）", () => {
		it("srcセクションにJDBC要素（jdbcUrl・jdbcUser・jdbcPass）が含まれる", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshSrcTypeTableResponseFixture)}
				/>,
			);

			expect(
				document.querySelector('input[type="text"][name="-src.jdbcUrl"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-src.jdbcUser"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-src.jdbcPass"]'),
			).toBeInTheDocument();
		});

		it("useJdbcMetaData要素が直接表示される", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshSrcTypeTableResponseFixture)}
				/>,
			);

			expect(
				document.querySelector(
					'input[type="checkbox"][name="-src.useJdbcMetaData"]',
				),
			).toBeVisible();
		});
	});

	describe("xlsxタイプ（generateType=xlsx）", () => {
		it("generate・jxls・src・traversalのセクションがこの順で表示される", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeXlsxResponseFixture)}
				/>,
			);

			const legends = document.querySelectorAll("fieldset legend");
			expect(legends[0]).toHaveTextContent("generate");
			expect(legends[1]).toHaveTextContent("jxls");
			expect(legends[2]).toHaveTextContent("src");
			expect(legends[3]).toHaveTextContent("traversal");
		});

		it("jxlsオプション要素は初期状態でhiddenになっている", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeXlsxResponseFixture)}
				/>,
			);

			expect(
				document.querySelector(
					'input[type="checkbox"][name="-template.formulaProcess"]',
				),
			).not.toBeVisible();
			expect(
				document.querySelector(
					'input[type="checkbox"][name="-template.evaluateFormulas"]',
				),
			).not.toBeVisible();
		});

		it("formulaProcessがjxlsセクションに含まれる", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeXlsxResponseFixture)}
				/>,
			);

			expect(
				document.querySelector(
					'input[type="checkbox"][name="-template.formulaProcess"]',
				),
			).toBeInTheDocument();
		});

		it("templateのdropdownにgenerate項目が表示される", async () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeXlsxResponseFixture)}
				/>,
			);

			expect(await templateMenuItem("generate")).toBeInTheDocument();
			expect(screen.queryByRole("button", { name: "edit" })).toBeNull();
		});
	});

	describe("xlsタイプ（generateType=xls）", () => {
		it("generate・jxls・src・traversalのセクションがこの順で表示される", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeXlsResponseFixture)}
				/>,
			);

			const legends = document.querySelectorAll("fieldset legend");
			expect(legends[0]).toHaveTextContent("generate");
			expect(legends[1]).toHaveTextContent("jxls");
			expect(legends[2]).toHaveTextContent("src");
			expect(legends[3]).toHaveTextContent("traversal");
		});

		it("formulaProcessはjxlsセクションに含まれない", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeXlsResponseFixture)}
				/>,
			);

			expect(
				document.querySelector(
					'input[type="checkbox"][name="-template.formulaProcess"]',
				),
			).not.toBeInTheDocument();
		});

		it("evaluateFormulasがjxlsセクションに含まれる", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeXlsResponseFixture)}
				/>,
			);

			expect(
				document.querySelector(
					'input[type="checkbox"][name="-template.evaluateFormulas"]',
				),
			).toBeInTheDocument();
		});
	});

	describe("sqlタイプ（generateType=sql）", () => {
		it("unitSetting・unitSettingEncodingは表示されない（固定テンプレートのため設定不可）", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeSqlResponseFixture)}
				/>,
			);

			expect(
				document.querySelector('input[type="text"][name="-unitSetting"]'),
			).not.toBeInTheDocument();
			expect(
				document.querySelector(
					'input[type="text"][name="-unitSettingEncoding"]',
				),
			).not.toBeInTheDocument();
		});

		it("opがselectとして表示される", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeSqlResponseFixture)}
				/>,
			);

			const opSelect = document.querySelector('select[name="-op"]');
			expect(opSelect).toBeInTheDocument();
			expect(opSelect?.querySelectorAll("option")).toHaveLength(5);
		});

		it("unitは表示されない（table固定のためレスポンスに含まれない）", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeSqlResponseFixture)}
				/>,
			);

			expect(
				document.querySelector('select[name="-unit"]'),
			).not.toBeInTheDocument();
		});

		it("templateのdropdownにgenerate項目は表示されない", async () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshGenerateTypeSqlResponseFixture)}
				/>,
			);

			expect(await templateMenuItem("generate")).not.toBeInTheDocument();
		});
	});

	describe("xlsxTemplateの比較用（templateType=compare）", () => {
		it("generateセクションにtemplateTypeがselectとして表示される", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(
						generateRefreshXlsxTemplateCompareResponseFixture,
					)}
				/>,
			);

			const select = document.querySelector('select[name="-templateType"]');
			expect(
				select?.closest("fieldset")?.querySelector("legend"),
			).toHaveTextContent("generate");
			expect(select).toHaveValue("compare");
		});

		it("compareセクションがsrcの後に表示され、比較用の項目がこの順で並ぶ", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(
						generateRefreshXlsxTemplateCompareResponseFixture,
					)}
				/>,
			);

			const legends = [...document.querySelectorAll("fieldset legend")];
			expect(legends[0]).toHaveTextContent("generate");
			expect(legends[1]).toHaveTextContent("src");
			const compareSection = legends
				.find((legend) => legend.textContent?.startsWith("compare"))
				?.closest("fieldset") as HTMLFieldSetElement;
			expect(
				legends[1].compareDocumentPosition(compareSection) &
					Node.DOCUMENT_POSITION_FOLLOWING,
			).toBeTruthy();
			expect(
				[...compareSection.querySelectorAll("input[type='text']")].map(
					(input) => input.getAttribute("name"),
				),
			).toEqual([
				"-oldTable",
				"-newTable",
				"-keys",
				"-compares",
				"-compareSheetName",
			]);
			expect(compareSection.querySelector("legend button")).toBeInTheDocument();
		});

		it("reloadボタンを押すと、oldTable・newTableにsrcのsetting適用後のテーブル名を候補として出す", async () => {
			render(
				<GenerateForm
					{...makeGenerateProps(
						generateRefreshXlsxTemplateCompareResponseFixture,
					)}
				/>,
			);
			const datalistValues = (name: string) =>
				[
					...(document.querySelector<HTMLInputElement>(`input[name="${name}"]`)
						?.list?.options ?? []),
				].map((option) => option.value);
			await userEvent.click(
				document.querySelector('input[name="-oldTable"]') as HTMLElement,
			);
			expect(datalistValues("-oldTable")).toEqual([]);

			await userEvent.click(
				screen.getAllByRole("button", { name: "reload table names" })[1],
			);

			for (const name of ["-oldTable", "-newTable"]) {
				expect(datalistValues(name)).toEqual(["OLD_USERS", "NEW_USERS"]);
			}
		});

		it("keys・comparesのボタンから、oldTableの列を選んだ順にカンマ区切りで入れる", async () => {
			const user = userEvent.setup();
			render(
				<GenerateForm
					{...makeGenerateProps(
						generateRefreshXlsxTemplateCompareResponseFixture,
					)}
				/>,
			);
			await user.type(
				document.querySelector('input[name="-oldTable"]') as HTMLElement,
				"OLD_USERS",
			);
			const keys = document.querySelector(
				'input[name="-keys"]',
			) as HTMLInputElement;
			await user.type(keys, "ID:CODE,X");

			await user.click(screen.getByRole("button", { name: "select keys" }));
			const dialog = screen.getByRole("dialog");
			expect(
				within(dialog)
					.getAllByRole("checkbox")
					.slice(1)
					.map((box) => (box as HTMLInputElement).checked),
			).toEqual([true, false, false]);
			await user.click(within(dialog).getByText("AMT"));
			await user.click(within(dialog).getByRole("button", { name: "Apply" }));

			// oldのIDを選んだままなので"ID:CODE"の対応は残し、列に無いXも消さない
			expect(keys).toHaveValue("ID:CODE,X,AMT");
			expect(
				screen.getByRole("button", { name: "select compares" }),
			).toBeInTheDocument();
		});

		it("templateTypeを変えるとフォームを再構成する", async () => {
			const props = makeGenerateProps(
				generateRefreshXlsxTemplateCompareResponseFixture,
			);
			render(<GenerateForm {...props} />);

			await userEvent.selectOptions(
				document.querySelector('select[name="-templateType"]') as HTMLElement,
				"table",
			);

			expect(props.handleTypeSelect).toHaveBeenCalled();
		});

		it("templateTypeの無いgenerateTypeではcompareセクションを表示しない", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			expect(
				document.querySelector('select[name="-templateType"]'),
			).not.toBeInTheDocument();
			expect(
				[...document.querySelectorAll("fieldset legend")].some((legend) =>
					legend.textContent?.startsWith("compare"),
				),
			).toBe(false);
		});
	});

	describe("xlsxTemplateのテーブル定義書（templateType=metadata）", () => {
		it("srcセクションを表示しない", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(
						generateRefreshXlsxTemplateMetaDataResponseFixture,
					)}
				/>,
			);

			expect(
				document.querySelector('select[name="-templateType"]'),
			).toHaveValue("metadata");
			expect(document.querySelector('[name^="-src."]')).toBeNull();
			expect(
				[...document.querySelectorAll("fieldset legend")].some((legend) =>
					legend.textContent?.startsWith("src"),
				),
			).toBe(false);
		});
	});
});

describe("unitSettingのPreview Unit Settings", () => {
	const option = (
		name: string,
		value: string,
		defaultPath: CommandOption["attribute"]["defaultPath"] = "WORKSPACE",
	): CommandOption => ({
		name,
		value,
		attribute: { type: "TEXT", required: false, selectOption: [], defaultPath },
		optional: false,
	});
	// unit=tableのtxtでunitSettingを指定し、srcは空
	const fixture: GenerateOptions = {
		...generateLoadResponseFixture,
		unit: {
			...(generateLoadResponseFixture.unit as CommandOption),
			value: "table",
		},
		unitSetting: option("unitSetting", "unit.json", "GENERATE_SETTING"),
		unitSettingEncoding: option("unitSettingEncoding", "UTF-8"),
		srcData: {
			...generateLoadResponseFixture.srcData,
			src: { ...generateLoadResponseFixture.srcData.src, value: "" },
		},
	};
	const previewButton = () =>
		screen.queryByRole("button", { name: "Preview Unit Settings" });

	it("srcが空なら出さず、srcフォームに入力すると出す", async () => {
		const user = userEvent.setup();
		render(<GenerateForm {...makeGenerateProps(fixture)} />);
		expect(previewButton()).not.toBeInTheDocument();

		const srcInput = document.querySelector(
			'input[type="text"][name="-src.src"]',
		) as HTMLInputElement;
		await user.type(srcInput, "resources/src/csv");

		expect(previewButton()).toBeInTheDocument();
	});
});
