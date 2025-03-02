import React, { useState } from "react";
import ReactDOM from "react-dom/client";
import Layout from "./components/Layout";
import StartupForm from "./components/StartupForm";
import EnviromentProvider from "./context/EnviromentProvider";
import WorkspaceResourcesProvider from "./context/WorkspaceResourcesProvider";

const App: React.FC = () => {
	const [isStartupFormVisible, setIsStartupFormVisible] = useState(true);

	const handleSelect = () => {
		setIsStartupFormVisible(false);
	};

	return (
		<React.StrictMode>
			<EnviromentProvider>
				<WorkspaceResourcesProvider>
					{isStartupFormVisible ? (
						<StartupForm onSelect={handleSelect} />
					) : (
						<Layout />
					)}
				</WorkspaceResourcesProvider>
			</EnviromentProvider>
		</React.StrictMode>
	);
};

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(<App />);
