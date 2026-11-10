import { act, renderHook } from "@testing-library/react";
import type { ReactNode } from "react";
import { describe, expect, it } from "vitest";
import ParameterInputProvider, {
	useParamInputs,
	useSetParamInputs,
} from "../../context/ParameterInputProvider";
import SelectParameterProvider, {
	useSelectParameter,
	useSetSelectParameterState,
} from "../../context/SelectParameterProvider";
import type { InputParam, Options } from "../../model/SelectParameter";
import { SelectParameter } from "../../model/SelectParameter";

// 読込と同じく、応答のinputParamから作る
const loaded = (name: string, inputParam?: InputParam) =>
	new SelectParameter(
		{ srcData: {}, convertResult: {}, inputParam } as unknown as Options,
		"convert",
		name,
	);

// refresh・renameと同じく、選択中のinputParamを引き継ぐ
const carried = (current: SelectParameter, name = current.name) =>
	new SelectParameter(
		current.options,
		current.command,
		name,
		current.inputParam,
	);

const wrapper = ({ children }: { children: ReactNode }) => (
	<SelectParameterProvider>
		<ParameterInputProvider>{children}</ParameterInputProvider>
	</SelectParameterProvider>
);

const renderParamInputs = () => {
	const seen: { name?: string; paramInputs: InputParam }[] = [];
	const hook = renderHook(
		() => {
			const parameter = useSelectParameter();
			const paramInputs = useParamInputs();
			seen.push({ name: parameter?.name, paramInputs });
			return {
				paramInputs,
				setParamInputs: useSetParamInputs(),
				setParameter: useSetSelectParameterState(),
			};
		},
		{ wrapper },
	);
	return { ...hook, seen };
};

describe("ParameterInputProvider", () => {
	it("パラメータを選ぶまでは空", () => {
		const { result } = renderParamInputs();
		expect(result.current.paramInputs).toEqual({});
	});

	it("読み込んだパラメータの保存済みの-Pを、切り替えた描画のうちに見せる", () => {
		const { result, seen } = renderParamInputs();
		act(() => result.current.setParameter(loaded("a", { id: "1" })));
		act(() => result.current.setParameter(loaded("b", { id: "2" })));

		expect(result.current.paramInputs).toEqual({ id: "2" });
		expect(seen).toContainEqual({ name: "a", paramInputs: { id: "1" } });
		expect(seen).not.toContainEqual({ name: "b", paramInputs: { id: "1" } });
		expect(seen).not.toContainEqual({ name: "a", paramInputs: {} });
	});

	it("refresh・renameでは編集した-Pを残す", () => {
		const { result } = renderParamInputs();
		act(() => result.current.setParameter(loaded("a", { id: "1" })));
		act(() => result.current.setParamInputs({ id: "9" }));

		act(() =>
			result.current.setParameter((current) => current && carried(current)),
		);
		expect(result.current.paramInputs).toEqual({ id: "9" });

		act(() =>
			result.current.setParameter(
				(current) => current && carried(current, "renamed"),
			),
		);
		expect(result.current.paramInputs).toEqual({ id: "9" });
	});

	it("同じパラメータを読み込み直すと保存済みの-Pに戻す", () => {
		const { result } = renderParamInputs();
		act(() => result.current.setParameter(loaded("a", { id: "1" })));
		act(() => result.current.setParamInputs({ id: "9" }));

		act(() => result.current.setParameter(loaded("a", { id: "1" })));
		expect(result.current.paramInputs).toEqual({ id: "1" });
	});

	it("-Pを編集した直後に別のパラメータへ切り替えると、そのパラメータの保存済みの-Pにする", () => {
		const { result } = renderParamInputs();
		act(() => result.current.setParameter(loaded("a", { id: "1" })));
		act(() => result.current.setParamInputs({ id: "9" }));

		act(() => result.current.setParameter(loaded("b")));
		expect(result.current.paramInputs).toEqual({});
	});
});
