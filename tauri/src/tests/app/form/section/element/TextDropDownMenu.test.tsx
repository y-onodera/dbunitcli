import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { TextDropDownMenu } from "../../../../../app/form/section/element/Text";
import { DeleteButton } from "../../../../../components/element/ButtonIcon";
import { makeMinimalParam } from "../../../../setup";

vi.mock("@tauri-apps/plugin-dialog", () => ({ open: vi.fn() }));
vi.mock("@tauri-apps/api", () => ({ core: { invoke: vi.fn() } }));
vi.mock("../../../../../hooks/useWorkspaceResources", () => ({
	useResolveAbsolutePath: () => vi.fn(async (path: string) => path),
}));

const element = makeMinimalParam("src");

function renderMenu(props: { path: string; isValueInDatalist?: boolean }) {
	render(
		<TextDropDownMenu
			path={props.path}
			setPath={vi.fn()}
			prefix=""
			element={element}
			isValueInDatalist={props.isValueInDatalist}
			removeButton={() => <DeleteButton handleClick={vi.fn()} />}
		/>,
	);
}

async function openMenu() {
	await userEvent.click(screen.getByTitle("setting"));
}

describe("TextDropDownMenuの削除ボタン表示", () => {
	it.each<{
		case: string;
		path: string;
		isValueInDatalist?: boolean;
		shown: boolean;
	}>([
		{
			case: "候補にある値",
			path: "a.sql",
			isValueInDatalist: true,
			shown: true,
		},
		{
			case: "候補にない値",
			path: "a.sql",
			isValueInDatalist: false,
			shown: false,
		},
		{ case: "候補一覧なし・値あり", path: "a.sql", shown: true },
		{ case: "候補一覧なし・値なし", path: "", shown: false },
	])("$caseのとき表示=$shown", async ({ path, isValueInDatalist, shown }) => {
		renderMenu({ path, isValueInDatalist });
		await openMenu();

		if (shown) {
			expect(screen.getByTitle("delete")).toBeInTheDocument();
		} else {
			expect(screen.queryByTitle("delete")).not.toBeInTheDocument();
		}
	});
});
