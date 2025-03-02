import { useState } from "react";
import EditNmaeProvider from "../context/EditNameProvider";
import SelectParameterProvider from "../context/SelectParameterProvider";
import Form from "./Form";
import Header from "./Header";
import Sidebar from "./Sidebar";
import "../styles.css";

export default function Layout() {
	const [sidebarWidth, setSidebarWidth] = useState(200);

	return (
		<>
			<SelectParameterProvider>
				<EditNmaeProvider>
					<nav
						className="fixed top-0
                                    w-full z-50 
                                    bg-gray-100
                                    border-b border-gray-200"
					>
						<Header />
					</nav>
					<aside
						id="logo-sidebar"
						className="fixed top-0 left-0 
                                    z-40 h-screen 
                                    pt-16 
                                    transition-transform 
                                    -translate-x-full 
                                    border-r border-gray-200 
                                    sm:translate-x-0"
						aria-label="Sidebar"
						style={{ width: `${sidebarWidth}px` }}
					>
						<Sidebar setSidebarWidth={setSidebarWidth} />
					</aside>
					<div className="p-2 " style={{ marginLeft: `${sidebarWidth}px` }}>
						<Form />
					</div>
				</EditNmaeProvider>
			</SelectParameterProvider>
		</>
	);
}
