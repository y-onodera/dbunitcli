import { renderHook } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import {
	OpenedResourceProvider,
	useOpenedResourceLocation,
} from "../../context/OpenedResourceProvider";
import type { ResourceKind } from "../../hooks/useResourceFiles";

const location = {
	folder: "/ws/other",
	defaultFolder: "/ws/resources/sql",
	fileName: "q.sql",
	defaultFolderHasFile: false,
};

function renderLocation(kind: ResourceKind, fileName: string) {
	return renderHook(() => useOpenedResourceLocation(kind, fileName), {
		wrapper: ({ children }: { children: React.ReactNode }) => (
			<OpenedResourceProvider
				kind="sql"
				fileName="other/q.sql"
				location={location}
			>
				{children}
			</OpenedResourceProvider>
		),
	}).result.current;
}

describe("OpenedResourceProviderのテスト", () => {
	it("読み込んだファイルと種類・名前が一致すれば、その場所を返すことを確認", () => {
		expect(renderLocation("sql", "other/q.sql")).toStrictEqual(location);
	});

	it("範囲内でも別のファイル（生成ダイアログの空の名前等）ならnullを返すことを確認", () => {
		expect(renderLocation("sql", "")).toBeNull();
		expect(renderLocation("table", "other/q.sql")).toBeNull();
	});

	it("範囲外ならnullを返すことを確認", () => {
		expect(
			renderHook(() => useOpenedResourceLocation("sql", "other/q.sql")).result
				.current,
		).toBeNull();
	});
});
