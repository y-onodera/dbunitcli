import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { describe, expect, it } from "vitest";
import { KeyValues } from "../../../components/dialog";
import { DatasetSetting } from "../../../model/DatasetSettings";

function KeyValuesHarness({ initial }: { initial: { [k: string]: string } }) {
	const [target, setTarget] = useState(new DatasetSetting({ string: initial }));
	return (
		<KeyValues
			name="string"
			values={target.string}
			handleChange={(index, value) =>
				setTarget((cur) => cur.replaceString(index, value))
			}
			handleRemove={(index) => setTarget((cur) => cur.removeString(index))}
		/>
	);
}

// 1行につき名前と式の2つの入力欄が並ぶ
function inputs() {
	return Array.from(
		document.querySelectorAll<HTMLInputElement>('input[name="string"]'),
	);
}

function inputValues() {
	return inputs().map((input) => input.value);
}

describe("KeyValuesのテスト", () => {
	it("Addで空の行を出し、名前を確定すると追加する", async () => {
		const user = userEvent.setup();
		render(<KeyValuesHarness initial={{ COL_A: "1" }} />);

		await user.click(screen.getByRole("button", { name: "add" }));
		expect(inputValues()).toEqual(["COL_A", "1", "", ""]);
		expect(
			screen.queryByRole("button", { name: "add" }),
		).not.toBeInTheDocument();

		await user.type(inputs()[2], "COL_B");
		await user.tab();
		expect(inputValues()).toEqual(["COL_A", "1", "COL_B", ""]);
		expect(screen.getByRole("button", { name: "add" })).toBeInTheDocument();
	});

	it("Addで出した行を削除すると、行を消してAddボタンを戻す", async () => {
		const user = userEvent.setup();
		render(<KeyValuesHarness initial={{ COL_A: "1" }} />);

		await user.click(screen.getByRole("button", { name: "add" }));
		await user.click(screen.getByRole("button", { name: "remove" }));
		expect(inputValues()).toEqual(["COL_A", "1"]);
		expect(screen.getByRole("button", { name: "add" })).toBeInTheDocument();
	});
});
