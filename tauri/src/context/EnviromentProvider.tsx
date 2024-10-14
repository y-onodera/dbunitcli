import { getMatches } from "@tauri-apps/api/cli";
import {
	type ReactNode,
	createContext,
	useContext,
	useEffect,
	useState,
} from "react";

export type Enviroment = {
	apiUrl: string;
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
			setEnviroment({
				apiUrl: `${`http://localhost:${port}` as string}/dbunit-cli/`,
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
