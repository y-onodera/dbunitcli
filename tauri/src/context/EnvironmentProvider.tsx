import { getMatches } from "@tauri-apps/plugin-cli";
import type { ReactNode } from "react";
import { createContext, Suspense, use, useState } from "react";
import SidecarLoading from "../app/startup/SidecarLoading";
import { API_CONTEXT_PATH } from "../utils/apiPath";

export type Environment = {
	apiUrl: string;
	workspace: string;
	dataset_base: string;
	result_base: string;
};
export const environmentContext = createContext<Environment>({} as Environment);

type CliArgs = Awaited<ReturnType<typeof getMatches>>["args"];

// 未指定・空文字の引数は既定値にする
function argOrDefault(args: CliArgs, name: string, defaultValue: string) {
	const value = args[name]?.value;
	return typeof value === "string" && value !== "" ? value : defaultValue;
}

async function loadEnvironment(): Promise<Environment> {
	const { args } = await getMatches();
	return {
		apiUrl: `http://localhost:${argOrDefault(args, "port", "8080")}${API_CONTEXT_PATH}`,
		workspace: argOrDefault(args, "workspace", "."),
		dataset_base: argOrDefault(args, "dataset.base", "."),
		result_base: argOrDefault(args, "result.base", "."),
	};
}

export default function EnvironmentProvider(props: { children: ReactNode }) {
	// use()に渡すPromiseは再描画で作り直すと再サスペンドするため、初回だけ作る
	const [promise] = useState(loadEnvironment);
	return (
		<Suspense fallback={<SidecarLoading />}>
			<CreateContext promise={promise}>{props.children}</CreateContext>
		</Suspense>
	);
}
function CreateContext(props: {
	promise: Promise<Environment>;
	children: ReactNode;
}) {
	const environment = use(props.promise);
	return (
		<environmentContext.Provider value={environment}>
			{props.children}
		</environmentContext.Provider>
	);
}
export const useEnvironment = () => use(environmentContext);
