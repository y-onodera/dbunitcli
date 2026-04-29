import { useState } from "react";
import SelectParameterProvider from "../../context/SelectParameterProvider";
import Form from "./Form";
import Header from "./Header";
import Sidebar from "./Sidebar";

export default function Layout() {
	const [sidebarWidth, setSidebarWidth] = useState(200);

	return (
		<SelectParameterProvider>
			<nav className="fixed top-0 w-full z-50 bg-surface-muted border-b border-border-subtle">
				<Header />
			</nav>
			<aside
				id="logo-sidebar"
				className="fixed top-0 left-0 z-40 h-screen pt-16 transition-transform -translate-x-full border-r border-border-subtle sm:translate-x-0"
				aria-label="Sidebar"
				style={{ width: `${sidebarWidth}px` }}
			>
				<Sidebar setSidebarWidth={setSidebarWidth} />
			</aside>
			<div className="p-2" style={{ marginLeft: `${sidebarWidth}px` }}>
				<Form />
			</div>
		</SelectParameterProvider>
	);
}
