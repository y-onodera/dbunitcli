import NameEditMenu from "./sidebar/NameEditMenu";
import NamedParameters from "./sidebar/NamedParameters";
import "../App.css";

export default function Sidebar() {
  return (
        <div className="h-full px-3 pb-4 overflow-y-auto dark:bg-gray-800">
          <NameEditMenu/>
          <ul className="space-y-2 font-medium">
            <NamedParameters/>
          </ul>
        </div>
  );
}