import { act, render, screen } from "@testing-library/react";
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
import { environmentFixture, workspaceResourcesFixture } from "../../setup";

const mockEnvironment: Environment = { ...environmentFixture };

const { mockFetchData } = vi.hoisted(() => {
	return {
		mockFetchData: vi.fn((_params: FetchParams) => {
			return Promise.resolve({
				json: () => Promise.resolve(workspaceResourcesFixture),
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

function renderFooter(inputParam?: Record<string, string>) {
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
	return render(
		<Footer formData={() => ({ values: {}, validationError: false })} />,
		{ wrapper },
	);
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
