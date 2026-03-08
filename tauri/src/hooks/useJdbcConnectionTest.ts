import { useEnviroment } from "../context/EnviromentProvider";
import { fetchData, handleFetchError } from "../utils/fetchUtils";

export const useJdbcConnectionTest = () => {
	const { apiUrl } = useEnviroment();
	return async (
		jdbcValues: Record<string, string>,
	): Promise<{ success: boolean; message: string } | null> => {
		const params = {
			endpoint: `${apiUrl}jdbc/test`,
			options: {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({
					url: jdbcValues.jdbcUrl ?? "",
					user: jdbcValues.jdbcUser ?? "",
					pass: jdbcValues.jdbcPass ?? "",
					properties: jdbcValues.jdbcProperties ?? "",
				}),
			},
		};
		try {
			const response = await fetchData(params);
			return (await response.json()) as { success: boolean; message: string };
		} catch (e) {
			handleFetchError((e as Error).message, params);
			return null;
		}
	};
};
