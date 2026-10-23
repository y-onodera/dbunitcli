import { act, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { type ReactNode, useEffect } from "react";
import { describe, expect, it, vi } from "vitest";
import Footer from "../../../app/footer/Footer";
import {
	type Environment,
	environmentContext,
} from "../../../context/EnvironmentProvider";
import ParameterInputProvider from "../../../context/ParameterInputProvider";
import SelectParameterProvider, {
	useSetSelectParameterState,
} from "../../../context/SelectParameterProvider";
import WorkspaceResourcesProvider from "../../../context/WorkspaceResourcesProvider";
import { type Options, SelectParameter } from "../../../model/SelectParameter";
import type { FetchParams } from "../../../utils/fetchUtils";
import type { CollectedFormValues } from "../../../utils/formValues";
import { environmentFixture, workspaceResourcesFixture } from "../../setup";

const mockEnvironment: Environment = { ...environmentFixture };

const { mockFetchData } = vi.hoisted(() => {
	return {
		mockFetchData: vi.fn((_params: FetchParams) => {
			return Promise.resolve({
				json: () => Promise.resolve(workspaceResourcesFixture),
				text: () => Promise.resolve("result/dir"),
			} as Response);
		}),
	};
});
vi.mock("../../../utils/fetchUtils", () => ({
	fetchData: mockFetchData,
	handleFetchError: vi.fn(),
	getErrorMessage: vi.fn(),
}));

function SelectConvert({
	inputParam,
}: {
	inputParam?: Record<string, string>;
}) {
	const setParameter = useSetSelectParameterState();
	useEffect(() => {
		setParameter(
			new SelectParameter(
				{ srcData: {}, convertResult: {}, inputParam } as unknown as Options,
				"convert",
				"test",
			),
		);
	}, [setParameter, inputParam]);
	return null;
}

function renderFooter(
	inputParam?: Record<string, string>,
	formData: (validate: boolean) => CollectedFormValues = () => ({
		values: {},
		validationError: false,
	}),
) {
	const wrapper = ({ children }: { children: ReactNode }) => (
		<environmentContext.Provider value={mockEnvironment}>
			<WorkspaceResourcesProvider>
				<SelectParameterProvider>
					<SelectConvert inputParam={inputParam} />
					<ParameterInputProvider>{children}</ParameterInputProvider>
				</SelectParameterProvider>
			</WorkspaceResourcesProvider>
		</environmentContext.Provider>
	);
	return render(<Footer formData={formData} />, { wrapper });
}

describe("FooterのParameters (-P)表示", () => {
	it("-Pがなければバッジを出さずNo parametersと表示する", async () => {
		await act(async () => {
			renderFooter();
		});
		expect(screen.getByText("— No parameters")).toBeInTheDocument();
		expect(screen.queryByTestId("params-badge")).not.toBeInTheDocument();
	});

	it("保存済みの-Pがあれば件数のバッジと件数を表示する", async () => {
		const inputParam = { orderDate: "2026-10-01", id: "1" };
		await act(async () => {
			renderFooter(inputParam);
		});
		expect(screen.getByTestId("params-badge")).toHaveTextContent("2");
		expect(screen.getByText("— 2 parameter(s) set")).toBeInTheDocument();
	});
});

describe("Footerの実行ボタン", () => {
	const callsTo = (action: string) =>
		mockFetchData.mock.calls.filter(([params]) =>
			params.endpoint.endsWith(`convert/${action}`),
		);
	const bodyOf = (params: FetchParams) =>
		JSON.parse(String(params.options.body));

	it("Execはフォーム値と-Pを合わせて実行し結果を表示する", async () => {
		await act(async () => {
			renderFooter({ id: "1" }, () => ({
				values: { "-src": "data" },
				validationError: false,
			}));
		});
		await userEvent.click(screen.getByText("Exec"));

		expect(callsTo("exec")).toHaveLength(1);
		expect(bodyOf(callsTo("exec")[0][0])).toStrictEqual({
			name: "test",
			input: { "-src": "data", "-Pid": "1" },
		});
		expect(screen.getByText("Execution Success")).toBeInTheDocument();
		expect(screen.getByText("Open Directory")).toBeInTheDocument();
	});

	it("Execは入力検証エラーなら実行しない", async () => {
		await act(async () => {
			renderFooter(undefined, () => ({ values: {}, validationError: true }));
		});
		await userEvent.click(screen.getByText("Exec"));

		expect(callsTo("exec")).toHaveLength(0);
	});

	it("Saveは検証せずフォーム値と-Pを保存する", async () => {
		const formData = vi.fn((_validate: boolean) => ({
			values: { "-src": "data" },
			validationError: false,
		}));
		await act(async () => {
			renderFooter({ id: "1" }, formData);
		});
		await userEvent.click(screen.getByText("Save"));

		expect(formData).toHaveBeenCalledWith(false);
		expect(formData).not.toHaveBeenCalledWith(true);
		expect(bodyOf(callsTo("save")[0][0])).toStrictEqual({
			name: "test",
			input: { "-src": "data", "-Pid": "1" },
		});
		expect(screen.getByText("Save Success")).toBeInTheDocument();
	});

	it("Save Shellはフォーム値を送らずに実行する", async () => {
		await act(async () => {
			renderFooter();
		});
		await userEvent.click(screen.getByText("Save Shell"));

		expect(bodyOf(callsTo("shell")[0][0])).toStrictEqual({ name: "test" });
		expect(screen.getByText("Save Shell Success")).toBeInTheDocument();
	});

	it("Abandonedで中断すると実行中の結果を表示しない", async () => {
		await act(async () => {
			renderFooter();
		});
		let resolveExec: (response: Response) => void = () => {};
		// クリック後の最初のfetchがexec
		mockFetchData.mockImplementationOnce(
			() =>
				new Promise<Response>((resolve) => {
					resolveExec = resolve;
				}),
		);
		await userEvent.click(screen.getByText("Exec"));
		expect(screen.getByText("Now Execution")).toBeInTheDocument();
		expect(callsTo("exec")[0][0].signal?.aborted).toBe(false);

		await userEvent.click(screen.getByText("Abandoned"));
		expect(callsTo("exec")[0][0].signal?.aborted).toBe(true);
		await act(async () => {
			resolveExec({ text: () => Promise.resolve("") } as Response);
		});

		expect(screen.queryByText("Now Execution")).not.toBeInTheDocument();
		expect(screen.queryByText("Execution Success")).not.toBeInTheDocument();
	});
});
