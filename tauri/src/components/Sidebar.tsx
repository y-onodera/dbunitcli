import "../App.css";
import NameEditMenu from "./sidebar/NameEditMenu";
import NamedParameters from "./sidebar/NamedParameters";

export default function Sidebar() {
	return (
		<div className="h-full px-3 pb-4 pt-4 overflow-y-auto ">
			<NameEditMenu />
			<NamedParameters />
		</div>
	);
}
