import { render } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import DBFormSection from "../../../../../app/form/section/srctype/DBFormSection";
import type { SqlOptions } from "../../../../../model/CommandOption";
import { makeMinimalParam } from "../../../../setup";

vi.mock("../../../../../context/FieldErrorProvider", () => ({
	useFieldErrors: () => [],
}));

const srcType = { ...makeMinimalParam("srcType"), value: "sql" as const };

describe("DBFormSectionのテスト", () => {
	it("useJdbcMetaDataがあればチェックボックスを表示する", () => {
		const options: SqlOptions = {
			prefix: "src",
			srcType,
			useJdbcMetaData: {
				...makeMinimalParam("useJdbcMetaData"),
				value: "false",
			},
		};
		render(
			<DBFormSection options={options} handleToggleChecked={() => vi.fn()} />,
		);

		expect(
			document.querySelector(
				'input[type="checkbox"][name="-src.useJdbcMetaData"]',
			),
		).toBeInTheDocument();
	});

	it("useJdbcMetaDataが無ければ（generateType=sql等）落ちずに何も表示しない", () => {
		const options: SqlOptions = { prefix: "src", srcType };
		const { container } = render(
			<DBFormSection options={options} handleToggleChecked={() => vi.fn()} />,
		);

		expect(container).toBeEmptyDOMElement();
	});
});
