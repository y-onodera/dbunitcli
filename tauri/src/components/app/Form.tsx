import { ReactNode } from "react";
import { Body, fetch, ResponseType } from "@tauri-apps/api/http";
import { environment } from "../../feature/httpClient";
import { currentCommand, useSelectParameter } from "../../context/SelectParameterProvider";

const formid="commandForm"
export function formData(){
    const formData = new FormData(document.querySelector('#'+formid) as HTMLFormElement);
    return Object.fromEntries(formData.entries());
}
export default function Form(props:{children:ReactNode}){
    const parameter = useSelectParameter();
    const handleClickSave = async (command:string,name:string) => {      
      await fetch(environment.serverUrl()+ command +"/save/"+name
        ,{
          method:"POST"
          ,responseType:ResponseType.Text
          ,headers: {'Content-Type': 'application/json'}
          ,body: Body.json(formData())
        })
      .then(response => {
        alert(response.data)
      })
      .catch((ex)=>alert(ex))
    }
    const handleClickExec = async (command:string,name:string) => {      
      await fetch(environment.serverUrl()+ command +"/exec/"+name
        ,{
          method:"POST"
          ,responseType:ResponseType.Text
          ,headers: {'Content-Type': 'application/json'}
          ,body: Body.json(formData())
        })
      .then(response => {
        alert(response.data)
      })
      .catch((ex)=>alert(ex))
    }
    return (
    <>
    <div className="p-4 rounded-lg mt-14">
      <form id={formid} className="grid gap-6 mb-6 grid-cols-1"
        onSubmit={(e) => {
          e.preventDefault();
        }}
      >
        {props.children}
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
                  onClick={()=>handleClickExec(currentCommand(parameter),parameter.name)}>Exec</button>
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
                    onClick={()=>handleClickSave(currentCommand(parameter),parameter.name)}>Save</button>
          <span className="text-sm text-gray-500">*Required</span>
        </div>
      </form>
    </div>
  </>
  );
}  