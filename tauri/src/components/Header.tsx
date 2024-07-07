import "../App.css";

export default function Header() {
    return (
        <div className="px-3 py-3 lg:px-5 lg:pl-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center justify-start rtl:justify-end">
            <h1>DBunit CLI</h1>
            </div>
          </div>
        </div>
    );
}
