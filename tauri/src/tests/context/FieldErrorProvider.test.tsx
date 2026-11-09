import { renderHook } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import FieldErrorProvider, {
	useFieldErrors,
	useVisibleFields,
} from "../../context/FieldErrorProvider";

describe("FieldErrorProviderのテスト", () => {
	it("同じnameの欄が複数表示中なら、1つ消えても表示中のまま残る", () => {
		const { result, rerender } = renderHook(
			({ first, second }: { first: boolean; second: boolean }) => {
				useFieldErrors("-src.src", first);
				useFieldErrors("-src.src", second);
				return useVisibleFields();
			},
			{
				initialProps: { first: true, second: true },
				wrapper: FieldErrorProvider,
			},
		);
		expect(result.current.has("-src.src")).toBe(true);

		rerender({ first: false, second: true });
		expect(result.current.has("-src.src")).toBe(true);

		rerender({ first: false, second: false });
		expect(result.current.has("-src.src")).toBe(false);
	});
});
