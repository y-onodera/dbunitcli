import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { describe, expect, it } from "vitest";
import { Arrays } from "../../../components/dialog";

// モデルのreplaceArrayと同じく、空の値は追加しない
function replaceAt(values: string[], text: string, index: number): string[] {
	if (index < values.length) {
		return values.map((v, i) => (i === index ? text : v));
	}
	return text ? [...values, text] : values;
}

function ArraysHarness({ initial }: { initial: string[] }) {
	const [values, setValues] = useState(initial);
	return (
		<Arrays
			name="include"
			values={values}
			handleChange={(text, index) =>
				setValues((cur) => replaceAt(cur, text, index))
			}
			handleRemove={(index) =>
				setValues((cur) => cur.filter((_, i) => i !== index))
			}
		/>
	);
}

function inputs() {
	return Array.from(
		document.querySelectorAll<HTMLInputElement>('input[name="include"]'),
	);
}

function inputValues() {
	return inputs().map((input) => input.value);
}

async function retype(
	user: ReturnType<typeof userEvent.setup>,
	input: HTMLInputElement,
	text: string,
) {
	await user.clear(input);
	await user.type(input, text);
	await user.tab();
}

describe("Arraysのテスト", () => {
	it("Addで空の行を出し、入力を確定するまでAddボタンを出さない", async () => {
		const user = userEvent.setup();
		render(<ArraysHarness initial={["COL_A"]} />);

		await user.click(screen.getByRole("button", { name: "add" }));
		expect(inputValues()).toEqual(["COL_A", ""]);
		expect(
			screen.queryByRole("button", { name: "add" }),
		).not.toBeInTheDocument();

		await retype(user, inputs()[1], "COL_B");
		expect(inputValues()).toEqual(["COL_A", "COL_B"]);
		expect(screen.getByRole("button", { name: "add" })).toBeInTheDocument();
	});

	it("Addで出した行を空のまま確定するか削除すると、行を消してAddボタンを戻す", async () => {
		const user = userEvent.setup();
		render(<ArraysHarness initial={["COL_A"]} />);

		await user.click(screen.getByRole("button", { name: "add" }));
		await user.click(inputs()[1]);
		await user.tab();
		expect(inputValues()).toEqual(["COL_A"]);
		expect(screen.getByRole("button", { name: "add" })).toBeInTheDocument();

		await user.click(screen.getByRole("button", { name: "add" }));
		await user.click(screen.getByRole("button", { name: "remove" }));
		expect(inputValues()).toEqual(["COL_A"]);
		expect(screen.getByRole("button", { name: "add" })).toBeInTheDocument();
	});

	it("Addで出した行に他の行と同じ値を入力しても追加しない", async () => {
		const user = userEvent.setup();
		render(<ArraysHarness initial={["COL_A"]} />);

		await user.click(screen.getByRole("button", { name: "add" }));
		await retype(user, inputs()[1], "COL_A");
		expect(inputValues()).toEqual(["COL_A", ""]);
	});

	it("他の行と同じ値を入力したら確定せず、入力前の値に戻す", async () => {
		const user = userEvent.setup();
		render(<ArraysHarness initial={["COL_A", "COL_B"]} />);

		await retype(user, inputs()[1], "COL_A");
		expect(inputValues()).toEqual(["COL_A", "COL_B"]);
	});

	it("読込んだ値に重複があっても、編集・削除後の表示が値と一致する", async () => {
		const user = userEvent.setup();
		render(<ArraysHarness initial={["COL_A", "COL_X", "COL_X"]} />);

		await retype(user, inputs()[1], "COL_B");
		expect(inputValues()).toEqual(["COL_A", "COL_B", "COL_X"]);

		await user.click(screen.getAllByRole("button", { name: "remove" })[1]);
		expect(inputValues()).toEqual(["COL_A", "COL_B"]);
	});

	it("行を削除すると後続の行が前に詰まって表示される", async () => {
		const user = userEvent.setup();
		render(<ArraysHarness initial={["COL_A", "COL_B", "COL_C"]} />);

		await user.click(screen.getAllByRole("button", { name: "remove" })[0]);
		expect(inputValues()).toEqual(["COL_A", "COL_C"]);
	});
});
