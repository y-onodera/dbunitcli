import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import DatasetTableNamesPreviewButton from "../../../../../app/form/section/dialog/DatasetTableNamesPreviewButton";
import type { TablePreview } from "../../../../../hooks/useDatasetSettings";

// jsdomにはshowModalが未実装のためモック
HTMLDialogElement.prototype.showModal = vi.fn(function (
	this: HTMLDialogElement,
) {
	this.setAttribute("open", "");
});

const { previewFixture } = vi.hoisted(() => ({
	previewFixture: { current: null as TablePreview | null },
}));
vi.mock("../../../../../hooks/useDatasetSettings", () => ({
	useDatasetTableNames: () => ({
		tableNames: ["big"],
		loading: false,
		logs: [],
	}),
	useDatasetTablePreview: () => ({
		preview: previewFixture.current,
		loading: false,
		logs: [],
	}),
}));

async function openPreview() {
	const user = userEvent.setup();
	render(<DatasetTableNamesPreviewButton title="Preview Before Settings" />);
	await user.click(
		screen.getByRole("button", { name: "Preview Before Settings" }),
	);
	await user.click(screen.getByRole("button", { name: "preview" }));
}

describe("DatasetTableNamesPreviewButtonのテスト", () => {
	it("全行数より表示行が少ないときは部分表示であることを示す", async () => {
		previewFixture.current = {
			headers: ["key"],
			rows: [["1"], ["2"]],
			totalRows: 150,
		};
		await openPreview();
		expect(
			screen.getByText("Showing first 2 of 150 rows (partial preview)"),
		).toBeInTheDocument();
	});

	it("全行を表示しているときは行数だけを示す", async () => {
		previewFixture.current = {
			headers: ["key"],
			rows: [["1"], ["2"]],
			totalRows: 2,
		};
		await openPreview();
		expect(screen.getByText("2 rows")).toBeInTheDocument();
		expect(screen.queryByText(/partial preview/)).not.toBeInTheDocument();
	});
});
