import EditNmaeProvider from "../context/EditNameProvider";
import SelectParameterProvider from "../context/SelectParameterProvider";
import Form from "./Form";
import Header from "./Header";
import Sidebar from "./Sidebar";
import "../styles.css";

export default function Layout() {
	return (
		<>
			<SelectParameterProvider>
				<EditNmaeProvider>
					<nav
						className="fixed top-0
                            z-50 
                            w-full 
                            bg-white 
                            border-b border-gray-200 
                            dark:bg-gray-800 dark:border-gray-700"
					>
						<Header />
					</nav>
					<aside
						id="logo-sidebar"
						className="fixed top-0 left-0 
                                                z-40 
                                                w-4=8 h-screen 
                                                pt-20 
                                                transition-transform 
                                                -translate-x-full 
                                                border-r border-gray-200 
                                                sm:translate-x-0 
                                                dark:bg-gray-800 dark:border-gray-700"
						aria-label="Sidebar"
					>
						<Sidebar />
					</aside>
					<div className="p-4 sm:ml-48">
						<Form />
					</div>
				</EditNmaeProvider>
			</SelectParameterProvider>
		</>
	);
}
