import { cleanup } from "@testing-library/react";
import "@testing-library/jest-dom";
import { afterEach, vi } from "vitest";
import type { Environment } from "../context/EnvironmentProvider";
import type { CommandOption } from "../model/CommandOption";
import {
	ParameterList,
	ResourcesSettings,
	WorkspaceContext,
	type WorkspaceResources,
} from "../model/WorkspaceResources";

export function makeMinimalParam(name: string): CommandOption {
	return {
		name,
		value: "",
		attribute: {
			type: "TEXT",
			required: false,
			selectOption: [],
			defaultPath: "WORKSPACE",
		},
		optional: false,
	};
}

export const workspaceResourcesFixture: WorkspaceResources = {
	context: WorkspaceContext.from({
		workspace: "test-workspace",
		datasetBase: "dataset",
		resultBase: "result",
		settingBase: "setting",
		templateBase: "template",
		parameterizeTemplateBase: "option/parameterize/template",
		jdbcBase: "jdbc",
		xlsxSchemaBase: "xlsx",
	}),
	parameterList: ParameterList.from({
		convert: ["convert1", "convert2"],
		compare: ["compare1"],
		generate: ["generate1"],
		run: ["run1"],
		parameterize: ["param1"],
	}),
	resources: ResourcesSettings.from({
		datasetSettings: ["setting1"],
		xlsxSchemas: ["schema1"],
		jdbcFiles: ["jdbc1"],
		templateFiles: ["template1"],
	}),
};

export const environmentFixture: Environment = {
	apiUrl: "http://localhost:8080/",
	workspace: "",
	dataset_base: "",
	result_base: "",
};

// テスト後のクリーンアップ
afterEach(() => {
	cleanup();
	vi.clearAllMocks();
});
