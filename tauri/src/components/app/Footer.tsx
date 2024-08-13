import { Body, fetch, ResponseType } from "@tauri-apps/api/http";
import { useSelectParameter } from "../../context/SelectParameterProvider";
import { formData } from "./Form";
import { useEnviroment } from "../../context/EnviromentProvider";

export default function Footer(){
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
        alert(response.data)
      })
      .catch((ex)=>alert(ex))
    }
    const handleClickExec = async (command:string,name:string) => {
      const input = formData(true)
      if(!input.validationError) {
        await fetch(environment.apiUrl+ command + "/exec"
          ,{
            method:"POST"
            ,responseType:ResponseType.Text
            ,headers: {'Content-Type': 'application/json'}
            ,body: Body.json({name,input})
          })
        .then(response => {
          alert(response.data)
        })
        .catch((ex)=>alert(ex))
      }
    }
  return (
      <>
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
                      onClick={()=>handleClickExec(parameter.command,parameter.name)}
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