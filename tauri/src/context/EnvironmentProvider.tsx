import { getMatches } from "@tauri-apps/plugin-cli";
import type { ReactNode } from "react";
import { createContext, Suspense, use } from "react";

export type Environment = {
	apiUrl: string;
	workspace: string;
	dataset_base: string;
	result_base: string;
};
export const environmentContext = createContext<Environment>({} as Environment);
export default function EnvironmentProvider(props: { children: ReactNode }) {
	const getEnvironment = async () => {
		const matches = await getMatches();
		const port = matches.args.port.value ? matches.args.port.value : "8080";
		const workspace = matches.args.workspace.value
			? (matches.args.workspace.value as string)
			: ".";
		const dataset_base = matches.args["dataset.base"].value
			? (matches.args["dataset.base"].value as string)
			: ".";
		const result_base = matches.args["result.base"].value
			? (matches.args["result.base"].value as string)
			: ".";
		return {
			apiUrl: `${`http://localhost:${port}` as string}/dbunit-cli/`,
			workspace,
			dataset_base,
			result_base,
		};
	};
	return (
		<Suspense fallback={<div>Loading...</div>}>
			<CreateContext promise={getEnvironment()}>{props.children}</CreateContext>
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
