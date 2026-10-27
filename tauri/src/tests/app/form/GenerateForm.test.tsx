import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { GenerateForm } from "../../../app/form/GenerateForm";
import type { GenerateOptions } from "../../../model/SelectParameter";
import { environmentFixture, workspaceResourcesFixture } from "../../setup";
import {
	generateLoadResponseFixture,
	generateRefreshGenerateTypeSqlResponseFixture,
	generateRefreshGenerateTypeXlsResponseFixture,
	generateRefreshGenerateTypeXlsxResponseFixture,
	generateRefreshSrcTypeTableResponseFixture,
} from "./fixtures";

// Tauri API モック
vi.mock("@tauri-apps/plugin-dialog", () => ({ open: vi.fn() }));
vi.mock("@tauri-apps/api", () => ({ core: { invoke: vi.fn() } }));
vi.mock("@tauri-apps/api/path", () => ({
	isAbsolute: vi.fn().mockResolvedValue(false),
	sep: vi.fn().mockReturnValue("/"),
}));

// コンテキストフックのモック
vi.mock("../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
	useResourcesSettings: () => workspaceResourcesFixture.resources,
	useSetResourcesSettings: () => vi.fn(),
}));
vi.mock("../../../context/EnvironmentProvider", () => ({
	useEnvironment: () => environmentFixture,
}));

// JDBC API フックのモック
vi.mock("../../../hooks/useJdbc", () => ({
	useJdbcConnectionTest: () => vi.fn(),
	useJdbcSaveProperties: () => vi.fn(),
	useDeleteJdbcProperties: () => vi.fn(),
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

// template欄のdropdownを開き、generate項目の有無を返す
async function templateGenerateMenuItem(): Promise<HTMLElement | null> {
	const templateInput = document.querySelector('input[name="-template"]');
	const menuButton = templateInput
		?.closest("div.flex")
		?.querySelector('button[title="setting"], button:has(title)');
	if (!menuButton) {
		return null;
	}
	await userEvent.setup().click(menuButton);
	return screen.queryByRole("button", { name: "generate" });
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

			expect(await templateGenerateMenuItem()).toBeInTheDocument();
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

			expect(await templateGenerateMenuItem()).not.toBeInTheDocument();
		});
	});
});
