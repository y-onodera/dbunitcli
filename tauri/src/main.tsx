import React from "react";
import ReactDOM from "react-dom/client";
import Layout from './components/Layout';

//Command.sidecar('backend/dbunit-cli-sidecar',["-Djava.home=C:/dev/IdeaProjects/dbunitcli/tauri/src-tauri/backend","-Dyo.dbunit.cli.sidecar.workspace=C:/dev/IdeaProjects/dbunitcli/sidecar/src/test/resources/workspace/sample"])
//             .execute()

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <Layout/>
  </React.StrictMode>,
);
