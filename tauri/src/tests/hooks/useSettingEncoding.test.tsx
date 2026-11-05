import { renderHook } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { DatasetSrcInfoProvider } from "../../context/DatasetSrcInfoProvider";
import { SettingEncodingProvider } from "../../context/SettingEncodingProvider";
import { useSettingEncoding } from "../../hooks/useSettingEncoding";

function srcInfoWrapper(settingEncoding?: string) {
	return ({ children }: { children: React.ReactNode }) => (
		<DatasetSrcInfoProvider
			initialValue={{
				values: settingEncoding === undefined ? {} : { settingEncoding },
			}}
		>
			{children}
		</DatasetSrcInfoProvider>
	);
}

describe("useSettingEncodingのテスト", () => {
	it("SettingEncodingProviderの範囲外ではdataset読込フォームのsettingEncodingを返すことを確認", () => {
		const { result } = renderHook(() => useSettingEncoding(), {
			wrapper: srcInfoWrapper("MS932"),
		});
		expect(result.current).toBe("MS932");
	});

	it("SettingEncodingProviderの範囲内ではその値を優先することを確認（空文字も含む）", () => {
		const Wrapper = srcInfoWrapper("MS932");
		for (const encoding of ["UTF-8", ""]) {
			const { result } = renderHook(() => useSettingEncoding(), {
				wrapper: ({ children }: { children: React.ReactNode }) => (
					<Wrapper>
						<SettingEncodingProvider encoding={encoding}>
							{children}
						</SettingEncodingProvider>
					</Wrapper>
				),
			});
			expect(result.current).toBe(encoding);
		}
	});

	it("どちらも無ければ空文字を返すことを確認", () => {
		const { result } = renderHook(() => useSettingEncoding(), {
			wrapper: srcInfoWrapper(),
		});
		expect(result.current).toBe("");
	});
});
