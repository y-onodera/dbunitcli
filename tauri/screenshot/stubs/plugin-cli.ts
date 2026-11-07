// 起動引数は使わず、sidecar の起動時の設定に任せる
export const getMatches = async () => ({
	args: {
		port: { value: "8080" },
		workspace: { value: "" },
		"dataset.base": { value: "" },
		"result.base": { value: "" },
	},
});
