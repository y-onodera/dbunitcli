import { cleanup } from "@testing-library/react";
import "@testing-library/jest-dom";
import { afterEach, vi } from "vitest";
import type { Enviroment } from "../context/EnviromentProvider";
import type { WorkspaceResources } from "../model/WorkspaceResources";

export const workspaceResourcesFixture: WorkspaceResources = {
	context: {
		workspace: 'test-workspace',
		datasetBase: 'dataset',
		resultBase: 'result',
		settingBase: 'setting',
		templateBase: 'template',
		jdbcBase: 'jdbc',
		xlsxSchemaBase: 'xlsx',
	},
	parameterList: {
		convert: ['convert1', 'convert2'],
		compare: ['compare1'],
		generate: ['generate1'],
		run: ['run1'],
		parameterize: ['param1'],
	},
	resources: {
		datasetSettings: ['setting1'],
		xlsxSchemas: ['schema1'],
		jdbcFiles: ['jdbc1'],
		templateFiles: ['template1'],
	},
};
export const enviromentFixture: Enviroment = {
	apiUrl: 'http://localhost:8080/',
	workspace: '',
	dataset_base: '',
	result_base: '',
	loaded: true
};

// テスト後のクリーンアップ
afterEach(() => {
	cleanup();
	vi.clearAllMocks();
});
