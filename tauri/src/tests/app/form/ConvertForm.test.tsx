import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { ConvertForm } from "../../../app/form/ConvertForm";
import { SelectParameter } from "../../../model/CommandParam";
import { enviromentFixture, workspaceResourcesFixture } from "../../setup";
import {
	convertLoadResponseFixture,
	convertRefreshResultTypeXlsxResponseFixture,
	convertRefreshSrcTypeTableResponseFixture,
	convertRefreshSrcTypeXlsxResponseFixture,
} from "./fixtures";

// Tauri API モック（ファイル選択ダイアログ等の依存）
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

// JDBC API フックのモック（クリック時のみ API を呼ぶ）
vi.mock("../../../hooks/useJdbc", () => ({
	useJdbcConnectionTest: () => vi.fn(),
	useJdbcSaveProperties: () => vi.fn(),
	useDeleteJdbcProperties: () => vi.fn(),
}));

// フィクスチャから ConvertForm の props を生成する
// SelectParameter コンストラクタで srcData を DatasetSourceImpl にラップし、
// srcElements() / srcTypeSettings() / settingElements() メソッドを有効化する
// ※ JSON.parse/JSON.stringify はコンストラクタがフィクスチャの srcData を直接書き換えるための深いコピー
function makeConvertProps(fixture = convertLoadResponseFixture) {
	const sp = new SelectParameter(
		JSON.parse(JSON.stringify(fixture)),
		"convert",
		"test",
	);
	return {
		handleTypeSelect: vi.fn().mockResolvedValue(undefined),
		name: "test",
		convert: sp.convert,
	};
}

// DOM順序の検証: precedingEl が followingEl より前にあることを確認する
function expectPrecedesInDom(precedingEl: Element, followingEl: Element) {
	expect(
		precedingEl.compareDocumentPosition(followingEl) &
			Node.DOCUMENT_POSITION_FOLLOWING,
	).toBeTruthy();
}

describe("ConvertFormの描画テスト", () => {
	describe("csvソース・xlsxResult（loadレスポンス）", () => {
		it("srcとresultのセクションがこの順で表示される", () => {
			render(<ConvertForm {...makeConvertProps()} />);

			const legends = document.querySelectorAll("fieldset legend");
			expect(legends[0]).toHaveTextContent("src");
			expect(legends[1]).toHaveTextContent("result");
		});

		it("srcセクションにsrcType・src・encodingが含まれる", () => {
			render(<ConvertForm {...makeConvertProps()} />);

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

		it("traversal option要素（recursive・regInclude等）は初期状態でhiddenになっている", () => {
			render(<ConvertForm {...makeConvertProps()} />);

			expect(
				document.querySelector(
					'input[type="checkbox"][name="-src.recursive"]',
				),
			).not.toBeVisible();
			expect(
				document.querySelector('input[type="text"][name="-src.regInclude"]'),
			).not.toBeVisible();
			expect(
				document.querySelector('input[type="text"][name="-src.extension"]'),
			).not.toBeVisible();
		});

		it("Show traversal optionはrecursiveの前のDOM位置に配置される", () => {
			render(<ConvertForm {...makeConvertProps()} />);

			expectPrecedesInDom(
				screen.getByText(/Show traversal option/),
				document.querySelector(
					'input[type="checkbox"][name="-src.recursive"]',
				)!,
			);
		});

		it("Show traversal optionをクリックするとtraversal option要素が表示される", async () => {
			render(<ConvertForm {...makeConvertProps()} />);

			await userEvent.click(screen.getByText(/Show traversal option/));

			expect(
				document.querySelector(
					'input[type="checkbox"][name="-src.recursive"]',
				),
			).toBeVisible();
			expect(
				document.querySelector('input[type="text"][name="-src.regInclude"]'),
			).toBeVisible();
		});

		it("トグル後のボタンテキストがHide traversal optionになる", async () => {
			render(<ConvertForm {...makeConvertProps()} />);

			await userEvent.click(screen.getByText(/Show traversal option/));

			expect(screen.getByText(/Hide traversal option/)).toBeInTheDocument();
		});

		it("csv option要素（headerName・delimiter等）は初期状態でhiddenになっている", () => {
			render(<ConvertForm {...makeConvertProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-src.headerName"]'),
			).not.toBeVisible();
			expect(
				document.querySelector('input[type="text"][name="-src.delimiter"]'),
			).not.toBeVisible();
			expect(
				document.querySelector(
					'input[type="checkbox"][name="-src.ignoreQuoted"]',
				),
			).not.toBeVisible();
		});

		it("Show csv optionはheaderNameの前のDOM位置に配置される", () => {
			render(<ConvertForm {...makeConvertProps()} />);

			expectPrecedesInDom(
				screen.getByText(/Show csv option/),
				document.querySelector(
					'input[type="text"][name="-src.headerName"]',
				)!,
			);
		});

		it("resultセクションにexcelTableが含まれる", () => {
			render(<ConvertForm {...makeConvertProps()} />);

			expect(
				document.querySelector(
					'input[type="text"][name="-result.excelTable"]',
				),
			).toBeInTheDocument();
		});
	});

	describe("xlsxソース（srcType=xlsxのrefreshレスポンス）", () => {
		it("srcセクションにxlsxSchemaが含まれる", () => {
			render(
				<ConvertForm
					{...makeConvertProps(convertRefreshSrcTypeXlsxResponseFixture)}
				/>,
			);

			expect(
				document.querySelector('input[type="text"][name="-src.xlsxSchema"]'),
			).toBeInTheDocument();
		});

		it("csvオプション（delimiter）は含まれない", () => {
			render(
				<ConvertForm
					{...makeConvertProps(convertRefreshSrcTypeXlsxResponseFixture)}
				/>,
			);

			expect(
				document.querySelector('[name="-src.delimiter"]'),
			).not.toBeInTheDocument();
		});

		it("Show xlsx optionが表示される", () => {
			render(
				<ConvertForm
					{...makeConvertProps(convertRefreshSrcTypeXlsxResponseFixture)}
				/>,
			);

			expect(screen.getByText(/Show xlsx option/)).toBeInTheDocument();
		});

		it("resultセクションにoutputEncodingが含まれる（csv result）", () => {
			render(
				<ConvertForm
					{...makeConvertProps(convertRefreshSrcTypeXlsxResponseFixture)}
				/>,
			);

			expect(
				document.querySelector(
					'input[type="text"][name="-result.outputEncoding"]',
				),
			).toBeInTheDocument();
		});
	});

	describe("tableソース（srcType=tableのrefreshレスポンス）", () => {
		it("srcセクションにJDBC要素（jdbcUrl・jdbcUser・jdbcPass）が含まれる", () => {
			render(
				<ConvertForm
					{...makeConvertProps(convertRefreshSrcTypeTableResponseFixture)}
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
				<ConvertForm
					{...makeConvertProps(convertRefreshSrcTypeTableResponseFixture)}
				/>,
			);

			expect(screen.getByText(/Show table option/)).toBeInTheDocument();
		});
	});

	describe("xlsxResult（resultType=xlsxのrefreshレスポンス）", () => {
		it("resultセクションにexcelTableが含まれる", () => {
			render(
				<ConvertForm
					{...makeConvertProps(convertRefreshResultTypeXlsxResponseFixture)}
				/>,
			);

			expect(
				document.querySelector(
					'input[type="text"][name="-result.excelTable"]',
				),
			).toBeInTheDocument();
		});

		it("resultセクションにoutputEncodingは含まれない", () => {
			render(
				<ConvertForm
					{...makeConvertProps(convertRefreshResultTypeXlsxResponseFixture)}
				/>,
			);

			expect(
				document.querySelector('[name="-result.outputEncoding"]'),
			).not.toBeInTheDocument();
		});
	});
});
