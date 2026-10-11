import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { describe, expect, it } from "vitest";
import SelectableTable from "../../../../../app/form/section/dialog/SelectableTable";
import { useTableSelection } from "../../../../../hooks/useTableSelection";

function Harness({
	items,
	emptyMessage,
}: {
	items: string[];
	emptyMessage?: string;
}) {
	const { selected, toggle, toggleAll } = useTableSelection();
	const [extra] = useState(true);
	return (
		<>
			<SelectableTable
				items={items}
				selected={selected}
				onToggle={toggle}
				onToggleAll={toggleAll}
				columnLabel="Table Name"
				filterPlaceholder="Filter..."
				emptyMessage={emptyMessage}
				renderExtraRows={(item) =>
					extra && item === "ORDERS" ? (
						<tr>
							<td />
							<td>extra-row</td>
						</tr>
					) : null
				}
			/>
			<output data-testid="selected">{[...selected].sort().join(",")}</output>
		</>
	);
}

describe("SelectableTable", () => {
	it("行クリックで選択が切り替わる", async () => {
		const user = userEvent.setup();
		render(<Harness items={["USERS", "ORDERS"]} />);
		await user.click(screen.getByRole("cell", { name: "USERS" }));
		expect(screen.getByTestId("selected").textContent).toBe("USERS");
		await user.click(screen.getByRole("cell", { name: "USERS" }));
		expect(screen.getByTestId("selected").textContent).toBe("");
	});

	it("フィルタで絞り込み、全選択はフィルタ後の行だけが対象になる", async () => {
		const user = userEvent.setup();
		render(<Harness items={["USERS", "ORDERS", "USER_ROLES"]} />);
		await user.type(screen.getByPlaceholderText("Filter..."), "user");
		expect(screen.queryByText("ORDERS")).toBeNull();
		await user.click(screen.getAllByRole("checkbox")[0]);
		expect(screen.getByTestId("selected").textContent).toBe("USERS,USER_ROLES");
	});

	it("追加行を行の直下へ描画する", () => {
		render(<Harness items={["USERS", "ORDERS"]} />);
		expect(screen.getByText("extra-row")).toBeTruthy();
	});

	it("itemsが空でemptyMessage指定時はメッセージを表示する", () => {
		render(<Harness items={[]} emptyMessage="No columns found" />);
		expect(screen.getByText("No columns found")).toBeTruthy();
		expect(screen.queryByRole("table")).toBeNull();
	});
});
