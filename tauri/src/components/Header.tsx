import "../App.css";
import { currentCommand, useSelectParameter } from "../context/SelectParameterProvider";

export default function Header() {
    const selected = useSelectParameter()
    return (
        <div className="px-3 py-3 lg:px-5 lg:pl-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center justify-start rtl:justify-end">
              <h1>DBunit CLI</h1>
            </div>
            <h1>{currentCommand(selected)+": "+selected.name}</h1>
          </div>
        </div>
    );
}
