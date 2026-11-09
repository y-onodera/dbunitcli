import { act, render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import {
	DatasetSrcInfoProvider,
	useDatasetSrcInfo,
	useSetDatasetSrcInfo,
} from "../../context/DatasetSrcInfoProvider";
import {
	useJdbcConnectionState,
	useSetJdbcConnectionState,
} from "../../context/JdbcConnectionProvider";
import type { DatasetSrcInfo } from "../../model/CommandOption";

const initial: DatasetSrcInfo = { values: { src: "a.csv" } };

type Captured = {
	info: ReturnType<typeof useDatasetSrcInfo>;
	setInfo: ReturnType<typeof useSetDatasetSrcInfo>;
	jdbc: ReturnType<typeof useJdbcConnectionState>;
	setJdbc: ReturnType<typeof useSetJdbcConnectionState>;
};

function Probe({ onRender }: { onRender: (captured: Captured) => void }) {
	onRender({
		info: useDatasetSrcInfo(),
		setInfo: useSetDatasetSrcInfo(),
		jdbc: useJdbcConnectionState(),
		setJdbc: useSetJdbcConnectionState(),
	});
	return null;
}

const renderWithResetKey = (resetKey: string) => {
	const captured: { current?: Captured } = {};
	const capture = (value: Captured) => {
		captured.current = value;
	};
	const tree = (key: string) => (
		<DatasetSrcInfoProvider initialValue={initial} resetKey={key}>
			<Probe onRender={capture} />
		</DatasetSrcInfoProvider>
	);
	const { rerender } = render(tree(resetKey));
	return {
		get: () => captured.current as Captured,
		changeResetKey: (key: string) => rerender(tree(key)),
	};
};

describe("DatasetSrcInfoProviderのテスト", () => {
	it("resetKeyが同じあいだは入力値・接続状態を保つ", () => {
		const { get, changeResetKey } = renderWithResetKey("csv");
		act(() => {
			get().setInfo({ values: { src: "b.csv" } });
			get().setJdbc({ jdbcValues: { url: "x" }, connectionOk: true });
		});
		changeResetKey("csv");
		expect(get().info).toStrictEqual({ values: { src: "b.csv" } });
		expect(get().jdbc.connectionOk).toBe(true);
	});

	it("resetKeyが変わったら初期値・未接続に戻す", () => {
		const { get, changeResetKey } = renderWithResetKey("table");
		act(() => {
			get().setInfo({ values: { src: "T1" } });
			get().setJdbc({ jdbcValues: { url: "x" }, connectionOk: true });
		});
		changeResetKey("sql");
		expect(get().info).toStrictEqual(initial);
		expect(get().jdbc).toStrictEqual({ jdbcValues: {}, connectionOk: false });
	});
});
