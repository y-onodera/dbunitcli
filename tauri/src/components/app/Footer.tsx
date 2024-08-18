import { tauri } from "@tauri-apps/api";
import { Body, fetch, ResponseType } from "@tauri-apps/api/http";
import { useState } from "react";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import { formData } from "./Form";
import { useEnviroment } from "../../context/EnviromentProvider";

export default function Footer() {
  const [resultDir,setResultDir] = useState('')
  const [hidden,setHidden] = useState(true)
  const [loading,setLoading] = useState(false)
  const environment = useEnviroment();
  const parameter = useSelectParameter();
  const handleClickSave = async (command:string,name:string) => {      
    await fetch(environment.apiUrl+ command + "/save"
      ,{
        method:"POST"
        ,responseType:ResponseType.Text
        ,headers: {'Content-Type': 'application/json'}
        ,body: Body.json({name,input:formData(false)})
      })
    .then(response => {
      if (!response.ok) {
        console.error('response.ok:', response.ok);
        console.error('esponse.status:', response.status);
        throw new Error(response.data as string);
      }
      alert(response.data)
    })
    .catch((ex)=>alert(ex))
  }
  const handleClickExec = async (command:string,name:string) => {
    const input = formData(true)
    await fetch(environment.apiUrl+ command + "/exec"
      ,{
        method:"POST"
        ,responseType:ResponseType.Text
        ,headers: {'Content-Type': 'application/json'}
        ,body: Body.json({name,input})
      })
    .then(response => {
      if (!response.ok) {
        console.error('response.ok:', response.ok);
        console.error('esponse.status:', response.status);
        throw new Error(response.data as string);
      }
      setHidden(false);
      setLoading(false);
      setResultDir(response.data as string);
    })
    .catch((ex)=>alert(ex))
  }
  const openDirectory = async function(path:string){
    await tauri.invoke('open_directory', {path})
  }
  if(loading) {
    throw new Promise(()=>handleClickExec(parameter.command,parameter.name))
  }
  return (
      <>
          <div id="popup-modal" hidden={hidden} className="overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 ">
            <div className="relative p-4 w-full max-w-md max-h-full">
              <div className="relative bg-white rounded-lg shadow dark:bg-gray-700">
                <div className="p-4 md:p-5 text-center">
                  <h3 className="mb-5 text-lg font-normal text-gray-500 dark:text-gray-400">Execution Success</h3>
                  {resultDir &&
                    <button type="button" onClick={()=> openDirectory(resultDir) }
                            className="text-center text-sm font-semibold text-white 
                                       bg-indigo-500 
                                       rounded-e-lg rounded-s-gray-100 rounded-s-2 
                                       border border-gray-300 
                                       transition 
                                       duration-100 
                                       ring-indigo-300 
                                       focus-visible:ring 
                                       hover:bg-indigo-600 
                                       active:bg-indigo-700 
                                       md:text-base">
                        Open Directory
                    </button>
                  }
                  <button type="button" onClick={()=> {setHidden(true);setResultDir('');}}
                          className="text-center text-sm 
                                     bg-white 
                                     rounded-e-lg rounded-s-gray-100 rounded-s-2 
                                     border border-gray-300 
                                     transition 
                                     duration-100 
                                     ring-glay-300 
                                     focus-visible:ring 
                                     hover:bg-glay-600 
                                     active:bg-glay-700 
                                     md:text-base">
                    Close
                  </button>
                </div>
              </div>
            </div>
          </div>
         {parameter.command &&
            <div className="fixed bottom-0 right-1 
                            w-full z-50 
                            flex items-center justify-end ">
              <button className="text-center text-sm font-semibold text-white 
                                 bg-indigo-500 
                                 rounded-e-lg rounded-s-gray-100 rounded-s-2 
                                 border border-gray-300 
                                 transition 
                                 duration-100 
                                 ring-indigo-300 
                                 focus-visible:ring 
                                 hover:bg-indigo-600 
                                 active:bg-indigo-700 
                                 md:text-base"
                      onClick={() => {
                          const input = formData(true)
                          if(!input.validationError) {
                            setLoading(true)
                          }
                        }
                      }
                      >Exec</button>
              <button className="text-center text-sm font-semibold text-white 
                                 bg-indigo-500 
                                 rounded-e-lg rounded-s-gray-100 rounded-s-2 
                                 border border-gray-300 
                                 transition 
                                 duration-100 
                                 ring-indigo-300 
                                 focus-visible:ring 
                                 hover:bg-indigo-600 
                                 active:bg-indigo-700 
                                 md:text-base"
                        onClick={()=>handleClickSave(parameter.command,parameter.name)}>Save</button>
              <span className="text-sm text-gray-500">*Required</span>
            </div>
          }
      </>
    );
}