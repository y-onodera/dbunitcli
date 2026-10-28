import { type CSSProperties, useState } from "react";
import SelectParameterProvider from "../../context/SelectParameterProvider";
import Form from "./Form";
import Header from "./Header";
import Sidebar from "./Sidebar";

export default function Layout() {
	const [sidebarWidth, setSidebarWidth] = useState(200);

	return (
		<SelectParameterProvider>
			<nav className="fixed top-0 w-full z-modal bg-pane-header border-b border-pane-header-border">
				<Header />
			</nav>
			<aside
				id="logo-sidebar"
				className="fixed top-0 left-0 z-sidebar h-screen pt-16 bg-pane-sidebar transition-transform -translate-x-full sm:translate-x-0"
				aria-label="Sidebar"
				style={{ width: `${sidebarWidth}px` }}
			>
				<Sidebar setSidebarWidth={setSidebarWidth} />
			</aside>
			{/* --sidebar-width は fixed 配置の Footer の左端合わせにも使う */}
			<div
				className="p-2 min-h-screen bg-pane-form ml-(--sidebar-width)"
				style={{ "--sidebar-width": `${sidebarWidth}px` } as CSSProperties}
			>
				<Form />
			</div>
		</SelectParameterProvider>
	);
}
