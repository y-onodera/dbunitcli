import "./App.css";
import React, { useState } from "react";
import ReactDOM from "react-dom/client";
import Layout from "./app/main/Layout";
import AppErrorBoundary from "./app/startup/AppErrorBoundary";
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
			<AppErrorBoundary>
				<EnvironmentProvider>
					<WorkspaceResourcesProvider>
						{isStartupFormVisible ? (
							<StartupForm onSelect={handleSelect} />
						) : (
							<Layout />
						)}
					</WorkspaceResourcesProvider>
				</EnvironmentProvider>
			</AppErrorBoundary>
		</React.StrictMode>
	);
};

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
	<App />,
);
