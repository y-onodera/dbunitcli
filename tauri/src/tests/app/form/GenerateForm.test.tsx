import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { GenerateForm } from "../../../app/form/GenerateForm";
import type { GenerateParams } from "../../../model/SelectParameter";
import { enviromentFixture, workspaceResourcesFixture } from "../../setup";
import {
	generateLoadResponseFixture,
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
vi.mock("../../../context/EnviromentProvider", () => ({
	useEnviroment: () => enviromentFixture,
}));

// JDBC API フックのモック
vi.mock("../../../hooks/useJdbc", () => ({
	useJdbcConnectionTest: () => vi.fn(),
	useJdbcSaveProperties: () => vi.fn(),
	useDeleteJdbcProperties: () => vi.fn(),
}));

function makeGenerateProps(
	fixture:
		| typeof generateLoadResponseFixture
		| typeof generateRefreshSrcTypeTableResponseFixture = generateLoadResponseFixture,
): { handleTypeSelect: () => Promise<void>; name: string; generate: GenerateParams } {
	return {
		handleTypeSelect: vi.fn().mockResolvedValue(undefined),
		name: "test",
		generate: fixture as unknown as GenerateParams,
	};
}

describe("GenerateFormの描画テスト", () => {
	describe("csvソース（loadレスポンス）", () => {
		it("generate・template・srcのセクションがこの順で表示される", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			const legends = document.querySelectorAll("fieldset legend");
			expect(legends[0]).toHaveTextContent("generate");
			expect(legends[1]).toHaveTextContent("template");
			expect(legends[2]).toHaveTextContent("src");
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

		it("csv option要素（delimiter等）は初期状態でhiddenになっている", () => {
			render(<GenerateForm {...makeGenerateProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-src.delimiter"]'),
			).not.toBeVisible();
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

		it("Show table optionが表示される", () => {
			render(
				<GenerateForm
					{...makeGenerateProps(generateRefreshSrcTypeTableResponseFixture)}
				/>,
			);

			expect(screen.getByText(/Show table option/)).toBeInTheDocument();
		});
	});
});
