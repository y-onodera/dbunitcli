import { renderHook } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import {
	ResourceEncodingProvider,
	type ResourceEncodings,
	useResourceEncoding,
} from "../../context/ResourceEncodingProvider";
import type { ResourceKind } from "../../hooks/useResourceFiles";

function renderEncoding(
	kind: ResourceKind,
	outer: ResourceEncodings = {},
	inner: ResourceEncodings = {},
) {
	return renderHook(() => useResourceEncoding(kind), {
		wrapper: ({ children }: { children: React.ReactNode }) => (
			<ResourceEncodingProvider encodings={outer}>
				<ResourceEncodingProvider encodings={inner}>
					{children}
				</ResourceEncodingProvider>
			</ResourceEncodingProvider>
		),
	}).result.current;
}

describe("ResourceEncodingProviderのテスト", () => {
	it("範囲外・指定の無い種類は空文字を返すことを確認", () => {
		expect(renderHook(() => useResourceEncoding("sql")).result.current).toBe(
			"",
		);
		expect(renderEncoding("sql", { template: "MS932" })).toBe("");
	});

	it("種類ごとの文字コードを返すことを確認", () => {
		const encodings = { sql: "MS932", template: "UTF-8" };
		expect(renderEncoding("sql", encodings)).toBe("MS932");
		expect(renderEncoding("template", encodings)).toBe("UTF-8");
	});

	it("入れ子では内側の値で上書きし、内側に無い種類は外側の値を引き継ぐことを確認（空文字も上書きする）", () => {
		const outer = { generateSetting: "MS932", template: "MS932" };
		const inner = { template: "", sql: "UTF-8" };
		expect(renderEncoding("template", outer, inner)).toBe("");
		expect(renderEncoding("generateSetting", outer, inner)).toBe("MS932");
		expect(renderEncoding("sql", outer, inner)).toBe("UTF-8");
	});
});
