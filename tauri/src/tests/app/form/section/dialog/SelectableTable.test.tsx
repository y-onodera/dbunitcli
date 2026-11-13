import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import SelectableTable, {
	type ExtraColumn,
} from "../../../../../app/form/section/dialog/SelectableTable";
import { useTableSelection } from "../../../../../hooks/useTableSelection";

function Harness({
	items,
	emptyMessage,
	extraColumns,
}: {
	items: string[];
	emptyMessage?: string;
	extraColumns?: ExtraColumn[];
}) {
	const { selected, toggle, toggleAll } = useTableSelection();
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
				extraColumns={extraColumns}
			/>
			<output data-testid="selected">{[...selected].sort().join(",")}</output>
		</>
	);
}

describe("SelectableTable", () => {
	it("extraColumnsの列を並べ、その値でもフィルタする", async () => {
		const user = userEvent.setup();
		const owners: Record<string, string> = { USERS: "alice", ORDERS: "bob" };
		render(
			<Harness
				items={["USERS", "ORDERS"]}
				extraColumns={[{ label: "owner", value: (item) => owners[item] }]}
			/>,
		);
		expect(
			screen.getByRole("columnheader", { name: "owner" }),
		).toBeInTheDocument();
		await user.type(screen.getByPlaceholderText("Filter..."), "bob");
		expect(screen.queryByText("USERS")).toBeNull();
		expect(screen.getByRole("cell", { name: "bob" })).toBeInTheDocument();
	});

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
		await user.click(screen.getByRole("checkbox", { name: "Select all" }));
		expect(screen.getByTestId("selected").textContent).toBe("USERS,USER_ROLES");
	});

	it("フィルタの×で絞り込みを解除する", async () => {
		const user = userEvent.setup();
		render(<Harness items={["USERS", "ORDERS"]} />);
		await user.type(screen.getByPlaceholderText("Filter..."), "user");
		expect(screen.queryByText("ORDERS")).toBeNull();

		await user.click(screen.getByRole("button", { name: "clear filter" }));
		expect(screen.getByPlaceholderText("Filter...")).toHaveValue("");
		expect(screen.getByText("ORDERS")).toBeInTheDocument();
	});

	it("itemsが空でemptyMessage指定時はメッセージを表示する", () => {
		render(<Harness items={[]} emptyMessage="No columns found" />);
		expect(screen.getByText("No columns found")).toBeTruthy();
		expect(screen.queryByRole("table")).toBeNull();
	});
});
