import "./styles.css";
import "./App.css";
import React, { useState } from "react";
import ReactDOM from "react-dom/client";
import Layout from "./app/main/Layout";
import StartupForm from "./app/startup/StartupForm";
import EnvironmentProvider from "./context/EnvironmentProvider";
import WorkspaceResourcesProvider from "./context/WorkspaceResourcesProvider";

const App: React.FC = () => {
	const [isStartupFormVisible, setIsStartupFormVisible] = useState(true);

	const handleSelect = () => {
		setIsStartupFormVisible(false);
	};

	return (
		<React.StrictMode>
			<EnvironmentProvider>
				<WorkspaceResourcesProvider>
					{isStartupFormVisible ? (
						<StartupForm onSelect={handleSelect} />
					) : (
						<Layout />
					)}
				</WorkspaceResourcesProvider>
			</EnvironmentProvider>
		</React.StrictMode>
	);
};

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
	<App />,
);
