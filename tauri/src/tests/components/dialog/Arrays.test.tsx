import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { describe, expect, it } from "vitest";
import { Arrays } from "../../../components/dialog";

function ArraysHarness({ initial }: { initial: string[] }) {
	const [values, setValues] = useState(initial);
	return (
		<Arrays
			name="include"
			values={values}
			handleChange={(text, index) =>
				setValues((cur) =>
					index < cur.length
						? cur.map((v, i) => (i === index ? text : v))
						: [...cur, text],
				)
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
	it("追加した行が未編集のうちはAddボタンを出さず、同じ値の行を増やさない", async () => {
		const user = userEvent.setup();
		render(<ArraysHarness initial={["COL_A"]} />);

		await user.click(screen.getByRole("button", { name: "add" }));
		expect(inputValues()).toEqual(["COL_A", "new item"]);
		expect(
			screen.queryByRole("button", { name: "add" }),
		).not.toBeInTheDocument();

		await retype(user, inputs()[1], "COL_B");
		expect(inputValues()).toEqual(["COL_A", "COL_B"]);
		expect(screen.getByRole("button", { name: "add" })).toBeInTheDocument();
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
