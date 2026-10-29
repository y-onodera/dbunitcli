import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { RemoveResource } from "../../../../../app/form/section/dialog/ResourceEditButton";

afterEach(() => {
	vi.unstubAllGlobals();
});

function renderRemove(result: string) {
	const deleteResource = vi.fn().mockResolvedValue(result);
	const setPath = vi.fn();
	const onRemoved = vi.fn();
	render(
		<RemoveResource
			path="a.json"
			setPath={setPath}
			onRemoved={onRemoved}
			deleteResource={deleteResource}
		/>,
	);
	return { deleteResource, setPath, onRemoved };
}

describe("RemoveResourceの削除後処理", () => {
	it("削除に成功したらパスを空にしてonRemovedを呼ぶ", async () => {
		vi.stubGlobal("confirm", vi.fn().mockReturnValue(true));
		const { deleteResource, setPath, onRemoved } = renderRemove("success");

		await userEvent.click(screen.getByTitle("delete"));

		await waitFor(() => expect(onRemoved).toHaveBeenCalledTimes(1));
		expect(deleteResource).toHaveBeenCalledWith("a.json");
		expect(setPath).toHaveBeenCalledWith("");
	});

	it("削除に失敗したらonRemovedを呼ばない", async () => {
		vi.stubGlobal("confirm", vi.fn().mockReturnValue(true));
		vi.stubGlobal("alert", vi.fn());
		const { setPath, onRemoved } = renderRemove("failed");

		await userEvent.click(screen.getByTitle("delete"));

		await waitFor(() => expect(window.alert).toHaveBeenCalled());
		expect(setPath).not.toHaveBeenCalled();
		expect(onRemoved).not.toHaveBeenCalled();
	});

	it("確認でキャンセルしたら削除せずonRemovedも呼ばない", async () => {
		vi.stubGlobal("confirm", vi.fn().mockReturnValue(false));
		const { deleteResource, onRemoved } = renderRemove("success");

		await userEvent.click(screen.getByTitle("delete"));

		expect(deleteResource).not.toHaveBeenCalled();
		expect(onRemoved).not.toHaveBeenCalled();
	});
});
