import { act, renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { environmentContext } from "../../context/EnvironmentProvider";
import WorkspaceResourcesProvider, {
	useResourcesSettings,
} from "../../context/WorkspaceResourcesProvider";
import {
	type ResourceKind,
	useDeleteResource,
} from "../../hooks/useDeleteResource";
import type { ResourcesSettingsBuilder } from "../../model/WorkspaceResources";
import type { FetchParams } from "../../utils/fetchUtils";
import { environmentFixture, workspaceResourcesFixture } from "../setup";

const mockRemainingFiles = ["remaining"];

const wrapper = ({ children }: { children: React.ReactNode }) => (
	<environmentContext.Provider value={environmentFixture}>
		<WorkspaceResourcesProvider>{children}</WorkspaceResourcesProvider>
	</environmentContext.Provider>
);

const { mockFetchData, mockFetchAndUpdate } = vi.hoisted(() => {
	const mockFetchData = vi.fn((params: FetchParams) => {
		if (params.endpoint.includes("/workspace/resources")) {
			return Promise.resolve(
				new Response(JSON.stringify(workspaceResourcesFixture)),
			);
		}
		if (params.endpoint.endsWith("/delete")) {
			return Promise.resolve(new Response(JSON.stringify(mockRemainingFiles)));
		}
		return Promise.resolve(new Response());
	});
	const mockFetchAndUpdate = vi.fn(
		async (params: FetchParams, onSuccess: (data: unknown) => void) => {
			const response = await mockFetchData(params);
			const data = await response.json();
			onSuccess(data);
			return "success" as const;
		},
	);
	return { mockFetchData, mockFetchAndUpdate };
});

vi.mock("../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
	fetchAndUpdate: mockFetchAndUpdate,
}));

describe("useDeleteResourceのテスト", () => {
	it.each<{
		kind: ResourceKind;
		endpoint: string;
		key: keyof ResourcesSettingsBuilder;
	}>([
		{
			kind: "datasetSetting",
			endpoint: "dataset-setting/delete",
			key: "datasetSettings",
		},
		{ kind: "xlsxSchema", endpoint: "xlsx-schema/delete", key: "xlsxSchemas" },
		{ kind: "jdbc", endpoint: "jdbc/delete", key: "jdbcFiles" },
		{ kind: "template", endpoint: "template/delete", key: "templateFiles" },
		{
			kind: "fixedColumnDef",
			endpoint: "fixed-column-def/delete",
			key: "fixedColumnDefs",
		},
	])(
		"kind=$kindのとき、$endpointへ名前を送り、応答で$keyを更新する",
		async ({ kind, endpoint, key }) => {
			const { result, rerender } = renderHook(
				() => ({
					deleteResource: useDeleteResource(kind),
					resources: useResourcesSettings(),
				}),
				{ wrapper },
			);

			// Providerがworkspaceの読込で一度suspendするため、解決を待つ
			await act(async () => {
				rerender();
			});

			let operationResult: string | undefined;
			await act(async () => {
				operationResult = await result.current.deleteResource("target");
			});

			expect(operationResult).toBe("success");
			expect(mockFetchAndUpdate).toHaveBeenCalledWith(
				expect.objectContaining({
					endpoint: `${environmentFixture.apiUrl}${endpoint}`,
					options: expect.objectContaining({ body: "target" }),
				}),
				expect.any(Function),
			);
			expect(result.current.resources[key]).toStrictEqual(mockRemainingFiles);
		},
	);
});
