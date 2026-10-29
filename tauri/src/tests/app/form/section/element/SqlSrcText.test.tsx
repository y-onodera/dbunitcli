import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import SqlSrcText from "../../../../../app/form/section/element/SqlSrcText";
import type { CommandOption } from "../../../../../model/CommandOption";
import {
	environmentFixture,
	workspaceResourcesFixture,
} from "../../../../setup";

vi.mock("@tauri-apps/plugin-dialog", () => ({ open: vi.fn() }));
vi.mock("@tauri-apps/api", () => ({ core: { invoke: vi.fn() } }));
vi.mock("../../../../../context/WorkspaceResourcesProvider", () => ({
	useWorkspaceContext: () => workspaceResourcesFixture.context,
	useResourcesSettings: () => workspaceResourcesFixture.resources,
	useSetResourcesSettings: () => vi.fn(),
}));
vi.mock("../../../../../context/EnvironmentProvider", () => ({
	useEnvironment: () => environmentFixture,
}));
vi.mock("../../../../../hooks/useWorkspaceResources", () => ({
	useResolveAbsolutePath: () => vi.fn(async (path: string) => path),
}));

function srcElement(value: string): CommandOption {
	return {
		name: "src",
		value,
		attribute: {
			type: "FILE_OR_DIR",
			required: true,
			selectOption: [],
			defaultPath: "SQL",
		},
		optional: false,
	};
}

function renderSrc(srcType: string, value: string) {
	render(
		<SqlSrcText prefix="src" element={srcElement(value)} srcType={srcType} />,
	);
}

function datalistValues(): string[] {
	return Array.from(document.querySelectorAll("datalist option")).map(
		(option) => option.getAttribute("value") ?? "",
	);
}

describe("SqlSrcTextの候補とメニュー", () => {
	it.each([
		{ srcType: "sql", expected: ["query1.sql"] },
		{ srcType: "table", expected: ["tables1.txt"] },
		{ srcType: "csvq", expected: ["csvq1.sql"] },
	])(
		"srcType=$srcTypeのときは対応するresources配下のファイルを候補にする",
		({ srcType, expected }) => {
			renderSrc(srcType, "");

			expect(datalistValues()).toStrictEqual(expected);
		},
	);

	it("候補にある値ならメニューにeditとdeleteを表示する", async () => {
		renderSrc("sql", "query1.sql");
		await userEvent.click(screen.getByTitle("setting"));

		expect(screen.getByTitle("edit")).toBeInTheDocument();
		expect(screen.getByTitle("delete")).toBeInTheDocument();
	});

	it("候補にない値ならdeleteを表示しない", async () => {
		renderSrc("sql", "other.sql");
		await userEvent.click(screen.getByTitle("setting"));

		expect(screen.getByTitle("edit")).toBeInTheDocument();
		expect(screen.queryByTitle("delete")).not.toBeInTheDocument();
	});
});
