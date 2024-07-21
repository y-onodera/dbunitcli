import { getMatches } from "@tauri-apps/api/cli";
import { createContext, ReactNode, useContext, useEffect, useState } from "react";

export type Enviroment = {
  apiUrl:string
  loaded:boolean
}
const enviromentContext = createContext<Enviroment>({} as Enviroment);
const matches = await getMatches()
export default function EnviromentProvider(props:{children:ReactNode}) {
  const [enviroment, setEnviroment] = useState<Enviroment>({apiUrl:"http://localhost:8080/dbunit-cli/",loaded:false} as Enviroment);
  useEffect(()=>{
      if (matches.args["port"] && matches.args["port"].value){
        const newEnviroment = {
          apiUrl :"http://localhost:" + matches.args["port"].value as string + "/dbunit-cli/",
          loaded :true
        } as Enviroment
        setEnviroment(newEnviroment)
      } else {
        setEnviroment({apiUrl:enviroment.apiUrl,loaded:true})
      }
  },[])
  return (
      <enviromentContext.Provider value={enviroment}>
          {enviroment.loaded ? props.children : "loading"}          
      </enviromentContext.Provider>
  );
}
export const useEnviroment = () => useContext(enviromentContext);
