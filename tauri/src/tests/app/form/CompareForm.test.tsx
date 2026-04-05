import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { CompareForm } from "../../../app/form/CompareForm";
import type { CompareParams } from "../../../model/SelectParameter";
import { enviromentFixture, workspaceResourcesFixture } from "../../setup";
import {
	compareLoadResponseFixture,
	compareRefreshExpectSrcTypeCsvResponseFixture,
	compareRefreshNewSrcTypeTableResponseFixture,
	compareRefreshTargetTypeImageResponseFixture,
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

function makeCompareProps(
	fixture:
		| typeof compareLoadResponseFixture
		| typeof compareRefreshTargetTypeImageResponseFixture
		| typeof compareRefreshNewSrcTypeTableResponseFixture
		| typeof compareRefreshExpectSrcTypeCsvResponseFixture = compareLoadResponseFixture,
): { handleTypeSelect: () => Promise<void>; name: string; compare: CompareParams } {
	return {
		handleTypeSelect: vi.fn().mockResolvedValue(undefined),
		name: "test",
		compare: fixture as unknown as CompareParams,
	};
}

describe("CompareFormの描画テスト", () => {
	describe("data targetType（loadレスポンス）", () => {
		it("compare・new・old・result・expectのセクションがこの順で表示される", () => {
			render(<CompareForm {...makeCompareProps()} />);

			const legends = document.querySelectorAll("fieldset legend");
			expect(legends[0]).toHaveTextContent("compare");
			expect(legends[1]).toHaveTextContent("new");
			expect(legends[2]).toHaveTextContent("old");
			expect(legends[3]).toHaveTextContent("result");
			expect(legends[4]).toHaveTextContent("expect");
		});

		it("compareセクションにtargetTypeが含まれる", () => {
			render(<CompareForm {...makeCompareProps()} />);

			expect(
				document.querySelector('select[name="-targetType"]'),
			).toBeInTheDocument();
		});

		it("compareセクションにsetting・settingEncodingが含まれる", () => {
			render(<CompareForm {...makeCompareProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-setting"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-settingEncoding"]'),
			).toBeInTheDocument();
		});

		it("imageオプションセクションは表示されない", () => {
			render(<CompareForm {...makeCompareProps()} />);

			const legends = document.querySelectorAll("fieldset legend");
			const legendTexts = Array.from(legends).map((l) => l.textContent);
			expect(legendTexts).not.toContain("image");
		});

		it("newセクションにsrcType・src・encodingが含まれる", () => {
			render(<CompareForm {...makeCompareProps()} />);

			expect(
				document.querySelector('select[name="-new.srcType"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-new.src"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-new.encoding"]'),
			).toBeInTheDocument();
		});

		it("oldセクションにsrcType・src・encodingが含まれる", () => {
			render(<CompareForm {...makeCompareProps()} />);

			expect(
				document.querySelector('select[name="-old.srcType"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-old.src"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-old.encoding"]'),
			).toBeInTheDocument();
		});

		it("expectセクションにsrcTypeのみ表示される（srcType=none のため src 等は含まれない）", () => {
			render(<CompareForm {...makeCompareProps()} />);

			expect(
				document.querySelector('select[name="-expect.srcType"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('[name="-expect.src"]'),
			).not.toBeInTheDocument();
		});
	});

	describe("image targetType（targetType=imageのrefreshレスポンス）", () => {
		it("imageセクションが表示される", () => {
			render(
				<CompareForm
					{...makeCompareProps(compareRefreshTargetTypeImageResponseFixture)}
				/>,
			);

			const legends = document.querySelectorAll("fieldset legend");
			const legendTexts = Array.from(legends).map((l) => l.textContent);
			expect(legendTexts).toContain("image");
		});

		it("imageセクションにthreshold・pixelToleranceLevelが含まれる", () => {
			render(
				<CompareForm
					{...makeCompareProps(compareRefreshTargetTypeImageResponseFixture)}
				/>,
			);

			expect(
				document.querySelector('input[type="text"][name="-image.threshold"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector(
					'input[type="text"][name="-image.pixelToleranceLevel"]',
				),
			).toBeInTheDocument();
		});

		it("newセクションのsrcTypeがfileになっている", () => {
			render(
				<CompareForm
					{...makeCompareProps(compareRefreshTargetTypeImageResponseFixture)}
				/>,
			);

			const newSrcType = document.querySelector(
				'select[name="-new.srcType"]',
			) as HTMLSelectElement;
			expect(newSrcType).toBeInTheDocument();
			expect(newSrcType?.value).toBe("file");
		});

		it("oldセクションのsrcTypeがfileになっている", () => {
			render(
				<CompareForm
					{...makeCompareProps(compareRefreshTargetTypeImageResponseFixture)}
				/>,
			);

			const oldSrcType = document.querySelector(
				'select[name="-old.srcType"]',
			) as HTMLSelectElement;
			expect(oldSrcType).toBeInTheDocument();
			expect(oldSrcType?.value).toBe("file");
		});
	});

	describe("tableソース（newData srcType=tableのrefreshレスポンス）", () => {
		it("newセクションにJDBC要素（jdbcUrl・jdbcUser・jdbcPass）が含まれる", () => {
			render(
				<CompareForm
					{...makeCompareProps(compareRefreshNewSrcTypeTableResponseFixture)}
				/>,
			);

			expect(
				document.querySelector('input[type="text"][name="-new.jdbcUrl"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-new.jdbcUser"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-new.jdbcPass"]'),
			).toBeInTheDocument();
		});

		it("Show table optionが表示される", () => {
			render(
				<CompareForm
					{...makeCompareProps(compareRefreshNewSrcTypeTableResponseFixture)}
				/>,
			);

			expect(screen.getByText(/Show table option/)).toBeInTheDocument();
		});
	});

	describe("csvExpect（expectData srcType=csvのrefreshレスポンス）", () => {
		it("expectセクションにsrcType・src・encodingが含まれる", () => {
			render(
				<CompareForm
					{...makeCompareProps(compareRefreshExpectSrcTypeCsvResponseFixture)}
				/>,
			);

			expect(
				document.querySelector('select[name="-expect.srcType"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-expect.src"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-expect.encoding"]'),
			).toBeInTheDocument();
		});
	});
});
