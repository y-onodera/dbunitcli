import { render } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ParameterizeForm } from "../../../app/form/ParameterizeForm";
import type { ParameterizeOptions } from "../../../model/SelectParameter";
import { enviromentFixture, workspaceResourcesFixture } from "../../setup";
import { parameterizeLoadResponseFixture } from "./fixtures";

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

function makeParameterizeProps(): {
	handleTypeSelect: () => Promise<void>;
	name: string;
	parameterize: ParameterizeOptions;
} {
	return {
		handleTypeSelect: vi.fn().mockResolvedValue(undefined),
		name: "test",
		parameterize:
			parameterizeLoadResponseFixture as unknown as ParameterizeOptions,
	};
}

describe("ParameterizeFormの描画テスト", () => {
	describe("csvパラメータ（loadレスポンス）", () => {
		it("execute・paramのセクションがこの順で表示される", () => {
			render(<ParameterizeForm {...makeParameterizeProps()} />);

			const legends = document.querySelectorAll("fieldset legend");
			expect(legends[0]).toHaveTextContent("execute");
			expect(legends[1]).toHaveTextContent("template");
			expect(legends[2]).toHaveTextContent("param");
		});

		it("executeセクションにunit・parameterize・ignoreFail・cmd・cmdParam・templateが含まれる", () => {
			render(<ParameterizeForm {...makeParameterizeProps()} />);

			expect(
				document.querySelector('select[name="-unit"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="checkbox"][name="-parameterize"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="checkbox"][name="-ignoreFail"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-cmd"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-cmdParam"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-template"]'),
			).toBeInTheDocument();
		});

		it("executeセクション内にtemplateOptionのencoding要素が含まれる", () => {
			render(<ParameterizeForm {...makeParameterizeProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-template.encoding"]'),
			).toBeInTheDocument();
		});

		it("paramセクションにsrcType・src・encodingが含まれる", () => {
			render(<ParameterizeForm {...makeParameterizeProps()} />);

			expect(
				document.querySelector('select[name="-param.srcType"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-param.src"]'),
			).toBeInTheDocument();
			expect(
				document.querySelector('input[type="text"][name="-param.encoding"]'),
			).toBeInTheDocument();
		});

		it("traversal option要素（recursive等）は初期状態でhiddenになっている", () => {
			render(<ParameterizeForm {...makeParameterizeProps()} />);

			expect(
				document.querySelector(
					'input[type="checkbox"][name="-param.recursive"]',
				),
			).not.toBeVisible();
			expect(
				document.querySelector('input[type="text"][name="-param.regInclude"]'),
			).not.toBeVisible();
		});

		it("csv option要素（delimiter等）は初期状態でhiddenになっている", () => {
			render(<ParameterizeForm {...makeParameterizeProps()} />);

			expect(
				document.querySelector('input[type="text"][name="-param.delimiter"]'),
			).not.toBeVisible();
		});
	});
});
