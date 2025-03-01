import { getMatches } from "@tauri-apps/plugin-cli";
import {
	type ReactNode,
	createContext,
	useContext,
	useEffect,
	useState,
} from "react";

export type Enviroment = {
	apiUrl: string;
	workspace: string;
	dataset_base: string;
	result_base: string;
	loaded: boolean;
};
const enviromentContext = createContext<Enviroment>({} as Enviroment);
export default function EnviromentProvider(props: { children: ReactNode }) {
	const [enviroment, setEnviroment] = useState<Enviroment>({
		loaded: false,
	} as Enviroment);
	useEffect(() => {
		const getEnviroment = async () => {
			const matches = await getMatches();
			const port = matches.args.port.value ? matches.args.port.value : "8080"
			const workspace = matches.args.workspace.value ? matches.args.workspace.value as string : ".";
			const dataset_base = matches.args['dataset.base'].value ? matches.args['dataset.base'].value as string : ".";
			const result_base = matches.args['result.base'].value ? matches.args['result.base'].value as string : ".";
			setEnviroment({
				apiUrl: `${`http://localhost:${port}` as string}/dbunit-cli/`,
				workspace,
				dataset_base,
				result_base,
				loaded: true,
			});
		}
		getEnviroment()
	}, []);
	return (
		<enviromentContext.Provider value={enviroment}>
			{enviroment.loaded ? props.children : "loading"}
		</enviromentContext.Provider>
	);
}
export const useEnviroment = () => useContext(enviromentContext);
