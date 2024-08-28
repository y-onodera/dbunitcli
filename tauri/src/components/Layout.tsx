import EditNmaeProvider from "../context/EditNameProvider";
import SelectParameterProvider from "../context/SelectParameterProvider";
import Header from "./Header";
import CommandForm from "./app/CommandForm";
import Footer from "./app/Footer";
import Form from "./app/Form";
import NameEditMenu from "./sidebar/NameEditMenu";
import NamedParameters from "./sidebar/NamedParameters";
import "../styles.css";
import { Suspense } from "react";

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
						<div className="h-full px-3 pb-4 overflow-y-auto dark:bg-gray-800">
							<NameEditMenu />
							<ul className="space-y-2 font-medium">
								<NamedParameters />
							</ul>
						</div>
					</aside>
					<div className="p-4 sm:ml-48">
						<Form>
							<CommandForm />
							<Suspense
								fallback={
									<div className="overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 ">
										<div className="relative p-4 w-full max-w-md max-h-full">
											<div className="relative bg-white rounded-lg shadow dark:bg-gray-700">
												<div className="p-4 md:p-5 flex flex-col justify-center items-center">
													<h3 className="mb-5 text-lg font-normal text-gray-500 dark:text-gray-400">
														Now Execution
													</h3>
													<div className="block animate-spin h-10 w-10 border-4 border-blue-500 rounded-full border-t-transparent" />
												</div>
											</div>
										</div>
									</div>
								}
							>
								<Footer />
							</Suspense>
						</Form>
					</div>
				</EditNmaeProvider>
			</SelectParameterProvider>
		</>
	);
}
