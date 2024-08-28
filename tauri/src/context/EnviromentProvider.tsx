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
const matches = await getMatches();
export default function EnviromentProvider(props: { children: ReactNode }) {
	const [enviroment, setEnviroment] = useState<Enviroment>({
		loaded: false,
	} as Enviroment);
	useEffect(() => {
		if (matches.args.port?.value) {
			const newEnviroment = {
				apiUrl: `${`http://localhost:${matches.args.port.value}` as string}/dbunit-cli/`,
				loaded: true,
			} as Enviroment;
			setEnviroment(newEnviroment);
		} else {
			setEnviroment({
				apiUrl: "http://localhost:8080/dbunit-cli/",
				loaded: true,
			});
		}
	}, []);
	return (
		<enviromentContext.Provider value={enviroment}>
			{enviroment.loaded ? props.children : "loading"}
		</enviromentContext.Provider>
	);
}
export const useEnviroment = () => useContext(enviromentContext);
