import { render } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { RunForm } from "../../../app/form/RunForm";
import type { RunOptions } from "../../../model/SelectParameter";
import { enviromentFixture, workspaceResourcesFixture } from "../../setup";
import {
	runLoadResponseFixture,
	runRefreshScriptTypeCmdResponseFixture,
	runRefreshScriptTypeSqlResponseFixture,
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
}));
vi.mock("../../../context/EnviromentProvider", () => ({
	useEnviroment: () => enviromentFixture,
}));

vi.mock("../../../hooks/useJdbc", () => ({
	useJdbcConnectionTest: () => vi.fn(),
	useJdbcSaveProperties: () => vi.fn(),
	useDeleteJdbcProperties: () => vi.fn(),
}));

function makeRunProps(
	fixture:
		| typeof runLoadResponseFixture
		| typeof runRefreshScriptTypeCmdResponseFixture
		| typeof runRefreshScriptTypeSqlResponseFixture = runLoadResponseFixture,
): { handleTypeSelect: () => Promise<void>; name: string; run: RunOptions } {
	return {
		handleTypeSelect: vi.fn().mockResolvedValue(undefined),
		name: "test",
		run: fixture as unknown as RunOptions,
	};
}

describe("RunFormの描画テスト", () => {
	describe("sql scriptType（loadレスポンス）", () => {
		it("run・template・jdbc・srcのセクションがこの順で表示される", () => {
			render(<RunForm {...makeRunProps()} />);

			const legends = document.querySelectorAll("fieldset legend");
			expect(legends[0]).toHaveTextContent("run");
			expect(legends[1]).toHaveTextContent("template");
			expect(legends[2]).toHaveTextContent("jdbc");
			expect(legends[3]).toHaveTextContent("src");
		});

		it("runセクションにscriptTypeが含まれる", () => {
			render(<RunForm {...makeRunProps()} />);

			expect(
				document.querySelector('select[name="-scriptType"]'),
			).toBeInTheDocument();
		});

		it("templateセクションにencodingが含まれる", () => {
			render(<RunForm {...makeRunProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-template.encoding"]'),
			).toBeInTheDocument();
		});

		it("jdbcセクションにjdbcUrl・jdbcUser・jdbcPassが含まれる", () => {
			render(<RunForm {...makeRunProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-jdbc.jdbcUrl"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-jdbc.jdbcUser"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-jdbc.jdbcPass"]'),
			).toBeInTheDocument();
		});

		it("srcセクションにsrcが含まれる", () => {
			render(<RunForm {...makeRunProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-src.src"]'),
			).toBeInTheDocument();
		});

		it("traversal option要素（recursive等）は初期状態でhiddenになっている", () => {
			render(<RunForm {...makeRunProps()} />);

			expect(
				document.querySelector('input[type="checkbox"][name="-src.recursive"]'),
			).not.toBeVisible();
			expect(
				document.querySelector('input[type="text"][name="-src.regInclude"]'),
			).not.toBeVisible();
		});
	});

	describe("cmd scriptType（scriptType=cmdのrefreshレスポンス）", () => {
		it("templateセクションは表示されない", () => {
			render(
				<RunForm {...makeRunProps(runRefreshScriptTypeCmdResponseFixture)} />,
			);

			const legends = document.querySelectorAll("fieldset legend");
			const legendTexts = Array.from(legends).map((l) => l.textContent);
			expect(legendTexts).not.toContain("template");
		});

		it("jdbcセクションは表示されない", () => {
			render(
				<RunForm {...makeRunProps(runRefreshScriptTypeCmdResponseFixture)} />,
			);

			const legends = document.querySelectorAll("fieldset legend");
			const legendTexts = Array.from(legends).map((l) => l.textContent);
			expect(legendTexts).not.toContain("jdbc");
		});

		it("srcセクションは表示される", () => {
			render(
				<RunForm {...makeRunProps(runRefreshScriptTypeCmdResponseFixture)} />,
			);

			const legends = document.querySelectorAll("fieldset legend");
			const legendTexts = Array.from(legends).map((l) => l.textContent);
			expect(legendTexts).toContain("src");
		});
	});

	describe("sql scriptType（refreshレスポンス）", () => {
		it("templateセクションが表示される", () => {
			render(
				<RunForm {...makeRunProps(runRefreshScriptTypeSqlResponseFixture)} />,
			);

			expect(
				document.querySelector('input[type="text"][name="-template.encoding"]'),
			).toBeInTheDocument();
		});

		it("jdbcセクションが表示される", () => {
			render(
				<RunForm {...makeRunProps(runRefreshScriptTypeSqlResponseFixture)} />,
			);

			expect(
				document.querySelector('input[type="text"][name="-jdbc.jdbcUrl"]'),
			).toBeInTheDocument();
		});
	});
});
