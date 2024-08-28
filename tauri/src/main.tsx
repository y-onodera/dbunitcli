import React from "react";
import ReactDOM from "react-dom/client";
import Layout from "./components/Layout";
import EnviromentProvider from "./context/EnviromentProvider";

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
	<React.StrictMode>
		<EnviromentProvider>
			<Layout />
		</EnviromentProvider>
	</React.StrictMode>,
);
