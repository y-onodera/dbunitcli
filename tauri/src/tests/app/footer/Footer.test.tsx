import { act, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { type ReactNode, useEffect, useState } from "react";
import { describe, expect, it, vi } from "vitest";
import Footer from "../../../app/footer/Footer";
import Check from "../../../app/form/section/element/Check";
import Select from "../../../app/form/section/element/Select";
import Text from "../../../app/form/section/element/Text";
import {
	type Environment,
	environmentContext,
} from "../../../context/EnvironmentProvider";
import FieldErrorProvider from "../../../context/FieldErrorProvider";
import ParameterInputProvider from "../../../context/ParameterInputProvider";
import SelectParameterProvider, {
	useSetSelectParameterState,
} from "../../../context/SelectParameterProvider";
import WorkspaceResourcesProvider from "../../../context/WorkspaceResourcesProvider";
import type { CommandOption } from "../../../model/CommandOption";
import { type Options, SelectParameter } from "../../../model/SelectParameter";
import {
	type FetchParams,
	OptionValidationError,
} from "../../../utils/fetchUtils";
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
vi.mock("../../../utils/fetchUtils", async (importOriginal) => ({
	...(await importOriginal<typeof import("../../../utils/fetchUtils")>()),
	fetchData: mockFetchData,
	handleFetchError: vi.fn(),
	logFetchError: vi.fn(),
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

// 任意項目の折りたたみと同じく、ボタンで欄の表示を切り替える
function Collapsible({ element }: { element: CommandOption }) {
	const [hidden, setHidden] = useState(true);
	return (
		<>
			<input type="button" value="toggle" onClick={() => setHidden(!hidden)} />
			<Text prefix="src" element={element} hidden={hidden} />
		</>
	);
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
		expect(screen.getByText("Execution Success")).not.toHaveClass("text-error");
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

	it.each(["Exec", "Save", "Save Shell"])(
		"%sが失敗したらalertを出さず結果ダイアログにメッセージを出す",
		async (button) => {
			const alertSpy = vi.spyOn(window, "alert").mockImplementation(() => {});
			await act(async () => {
				renderFooter();
			});
			mockFetchData.mockRejectedValueOnce(new Error("line1\nline2"));
			await userEvent.click(screen.getByText(button));

			// 改行を含むメッセージもそのまま出す
			expect(
				screen.getByText((_, el) => el?.textContent === "line1\nline2"),
			).toHaveClass("text-error");
			expect(screen.queryByText("Open Directory")).not.toBeInTheDocument();
			expect(alertSpy).not.toHaveBeenCalled();
			alertSpy.mockRestore();
		},
	);

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

describe("Execのオプション検証エラー", () => {
	const textOption = (name: string): CommandOption => ({
		name,
		value: "",
		attribute: {
			type: "FILE",
			required: false,
			selectOption: [],
			defaultPath: "WORKSPACE",
		},
		optional: false,
	});

	const renderWithFields = (fields?: ReactNode) => {
		const wrapper = ({ children }: { children: ReactNode }) => (
			<environmentContext.Provider value={mockEnvironment}>
				<WorkspaceResourcesProvider>
					<SelectParameterProvider>
						<SelectConvert />
						<ParameterInputProvider>
							<FieldErrorProvider>{children}</FieldErrorProvider>
						</ParameterInputProvider>
					</SelectParameterProvider>
				</WorkspaceResourcesProvider>
			</environmentContext.Provider>
		);
		return render(
			<>
				{fields ?? (
					<>
						<Text prefix="src" element={textOption("src")} />
						<Text prefix="src" element={textOption("setting")} hidden />
					</>
				)}
				<Footer formData={() => ({ values: {}, validationError: false })} />
			</>,
			{ wrapper },
		);
	};

	it("表示中の欄の違反は欄の下に出し、出せない違反は結果ダイアログに出し、入力し直すと消す", async () => {
		const alertSpy = vi.spyOn(window, "alert").mockImplementation(() => {});
		await act(async () => {
			renderWithFields();
		});
		// 描画時のワークスペース読込が済んでから、execの応答だけを検証エラーにする
		mockFetchData.mockRejectedValueOnce(
			new OptionValidationError("invalid", [
				{ options: ["-src.src"], message: "src not found" },
				{ options: ["-src.setting"], message: "setting not found" },
				{ options: ["-cmd"], message: "cmd is required" },
			]),
		);
		await userEvent.click(screen.getByText("Exec"));

		// 失敗は成功と区別して赤字で出す
		expect(screen.getByText("Invalid Option")).toHaveClass("text-error");
		expect(screen.getByText("src not found")).toBeInTheDocument();
		// 非表示の欄の下には出さず、ダイアログにだけ出す
		expect(screen.getAllByText("setting not found")).toHaveLength(1);
		expect(screen.getByText("cmd is required")).toBeInTheDocument();
		expect(alertSpy).not.toHaveBeenCalled();

		await userEvent.type(screen.getByLabelText("-src.src"), "a");
		expect(screen.queryByText("src not found")).not.toBeInTheDocument();
		alertSpy.mockRestore();
	});

	it("ファイル選択等のボタンから値を変えても違反を消す", async () => {
		await act(async () => {
			renderWithFields(
				<Text prefix="src" element={textOption("src")}>
					{({ setPath }) => (
						<input
							type="button"
							value="choose"
							onClick={() => setPath("chosen.csv")}
						/>
					)}
				</Text>,
			);
		});
		mockFetchData.mockRejectedValueOnce(
			new OptionValidationError("invalid", [
				{ options: ["-src.src"], message: "src not found" },
			]),
		);
		await userEvent.click(screen.getByText("Exec"));
		expect(screen.getByText("src not found")).toBeInTheDocument();

		await userEvent.click(screen.getByText("Close"));
		await userEvent.click(screen.getByDisplayValue("choose"));
		expect(screen.getByLabelText("-src.src")).toHaveValue("chosen.csv");
		expect(screen.queryByText("src not found")).not.toBeInTheDocument();
	});

	it("折りたたんだ欄の違反は、展開すると欄の下に出す", async () => {
		await act(async () => {
			renderWithFields(<Collapsible element={textOption("setting")} />);
		});
		mockFetchData.mockRejectedValueOnce(
			new OptionValidationError("invalid", [
				{ options: ["-src.setting"], message: "setting not found" },
			]),
		);
		await userEvent.click(screen.getByText("Exec"));
		// 折りたたみ中はダイアログにだけ出す
		expect(screen.getAllByText("setting not found")).toHaveLength(1);

		await userEvent.click(screen.getByText("Close"));
		await userEvent.click(screen.getByDisplayValue("toggle"));
		expect(screen.getByText("setting not found")).toBeInTheDocument();
	});

	it("チェックボックスとセレクトの違反も欄の下に出し、入力し直すと消す", async () => {
		const handleTypeSelect = vi.fn(() => Promise.resolve());
		await act(async () => {
			renderWithFields(
				<>
					<Check
						prefix="result"
						element={{
							...textOption("exportHeader"),
							value: "true",
							attribute: { ...textOption("").attribute, type: "FLG" },
						}}
					/>
					<Select
						prefix="result"
						handleTypeSelect={handleTypeSelect}
						element={{
							...textOption("resultType"),
							value: "csv",
							attribute: {
								...textOption("").attribute,
								type: "ENUM",
								selectOption: ["csv", "xlsx"],
							},
						}}
					/>
				</>,
			);
		});
		mockFetchData.mockRejectedValueOnce(
			new OptionValidationError("invalid", [
				{ options: ["-result.exportHeader"], message: "not a boolean" },
				{ options: ["-result.resultType"], message: "bad type" },
			]),
		);
		await userEvent.click(screen.getByText("Exec"));

		expect(screen.getByText("not a boolean")).toBeInTheDocument();
		expect(screen.getByText("bad type")).toBeInTheDocument();

		await userEvent.click(screen.getByLabelText("-result.exportHeader"));
		expect(screen.queryByText("not a boolean")).not.toBeInTheDocument();
		await userEvent.selectOptions(
			screen.getByLabelText("-result.resultType"),
			"xlsx",
		);
		expect(screen.queryByText("bad type")).not.toBeInTheDocument();
		expect(handleTypeSelect).toHaveBeenCalledWith("xlsx");
	});
});
