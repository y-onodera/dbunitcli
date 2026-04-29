import { render } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { GenerateForm } from "../../../app/form/GenerateForm";
import type { GenerateOptions } from "../../../model/SelectParameter";
import { environmentFixture, workspaceResourcesFixture } from "../../setup";
import {
	generateLoadResponseFixture,
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

function makeGenerateProps(fixture: GenerateOptions = generateLoadResponseFixture): {
	handleTypeSelect: () => Promise<void>;
	name: string;
	generate: GenerateOptions;
} {
	return {
		handleTypeSelect: vi.fn().mockResolvedValue(undefined),
		name: "test",
		generate: fixture,
	};
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
});
