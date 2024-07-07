import { useEffect, useState } from "react";
import { Body, fetch, ResponseType } from "@tauri-apps/api/http";
import { environment } from "../../feature/httpClient";
import { EditName, useSetEditName } from "../../context/EditNameProvider";
import { CommandParametersProp, CompareParameters, ConvertParameters, GenerateParameters, ParameterizeParameters, RunParameters, useSetSelectParameter } from "../../context/SelectParameterProvider";

type NamedParameters = {
    convert:string[]
    compare:string[]
    generate:string[]
    run:string[]
    parameterize:string[]
}
type NamedParameterProp = {
  command:string;
  namedParameters?:string[];
  handleParameterSelect:Function;
  handleEditNamed:Function;
}
export default function NamedParameters() {
    const setParameter = useSetSelectParameter();
    const handleParameterSelect = async (command:string,name:string) => {
      await fetch(environment.serverUrl()+ command +"/load"
        ,{
          method:"POST"
          ,responseType:ResponseType.JSON
          ,headers: {'Content-Type': 'application/json'}
          ,body: Body.json({name})
        })
      .then(response => {
        const newParam = {} as CommandParametersProp
        newParam.name = name;
        if (command == "convert"){
          newParam.convert = response.data as ConvertParameters
        } if (command == "compare"){
          newParam.compare = response.data as CompareParameters
        } if (command == "generate"){
          newParam.generate = response.data as GenerateParameters
        } if (command == "run"){
          newParam.run = response.data as RunParameters
        } if (command == "parameterize"){
          newParam.parameterize = response.data as ParameterizeParameters
        }
        setParameter(newParam)
      })
      .catch((ex)=>alert(ex))
    }
    const [parameters ,setParameters] = useState<NamedParameters>();
    const handlMenuInit = async () => {
      await fetch(environment.serverUrl()+"parameter/list"
        ,{method:"GET",responseType:ResponseType.JSON})
      .then(response => setParameters(response.data  as NamedParameters))
      .catch((ex)=>alert(ex))
    }
    useEffect(()=>{
      handlMenuInit();
    },[])
    const setEditName = useSetEditName()
    const handleEditNamed = (selected:EditName)=> setEditName(selected)
    return (
      <ul className="space-y-2 font-medium">
        <Category command="Convert" namedParameters={parameters?.convert} handleParameterSelect={handleParameterSelect} handleEditNamed={handleEditNamed}/>
        <Category command="Compare" namedParameters={parameters?.compare} handleParameterSelect={handleParameterSelect} handleEditNamed={handleEditNamed}/>
        <Category command="Generate" namedParameters={parameters?.generate} handleParameterSelect={handleParameterSelect} handleEditNamed={handleEditNamed}/>
        <Category command="Run" namedParameters={parameters?.run} handleParameterSelect={handleParameterSelect} handleEditNamed={handleEditNamed}/>
        <Category command="Parameterize" namedParameters={parameters?.parameterize} handleParameterSelect={handleParameterSelect} handleEditNamed={handleEditNamed}/>
      </ul>
    );
}
function Category(prop:NamedParameterProp) {
    const [close, setClose] = useState(true);
    const toggleMenu = () => setClose(!close);
    return (
      <li>
        <button type="button" onClick={toggleMenu} className="flex items-center 
                                       w-full p-2 
                                       text-base text-gray-900 
                                       transition duration-75 
                                       rounded-lg 
                                       group 
                                       ring-indigo-300 
                                       focus-visible:ring
                                       hover:bg-gray-100 
                                       dark:text-white dark:hover:bg-gray-700" 
                                       >
          <svg className="w-3 h-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 10 6" >
             <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="m1 1 4 4 4-4" transform={close?"rotate(270,5,2.5)":""}/>
          </svg>
          <span className="ms-2  
                           text-left 
                           rtl:text-right 
                           whitespace-nowrap">{prop.command}</span>
        </button>
        <ul id={prop.command+"-list"} className="py-1 space-y-1" hidden={close}>
          <Parameters command={prop.command} 
                 namedParameters={prop.namedParameters} 
                 handleParameterSelect={prop.handleParameterSelect} 
                 handleEditNamed={prop.handleEditNamed}/>
        </ul>
      </li>
    );
  }
  function Parameters(prop:NamedParameterProp) {
    const [menuList, setMenuList] = useState([] as string[]);
    useEffect( () => setMenuList(prop.namedParameters ? [...prop.namedParameters] : [...menuList]) ,[prop.namedParameters])
    const handleAddNewName = async () => {
        await fetch(environment.serverUrl()+ prop.command.toLowerCase() +"/add"
          ,{method:"GET",responseType:ResponseType.JSON})
        .then(response => setMenuList(response.data as string[]) )
        .catch((ex)=>alert(ex)) 
    }
    return (
      <>
        {menuList && menuList.map((menu,index) => {
          return (
            <li key={index} className="flex">
              <a href="#" onClick={()=>prop.handleParameterSelect(prop.command.toLowerCase(),menu)} className="flex items-center justify-start
                                      ms-2 w-full p-1 
                                      text-gray-900 
                                      transition duration-75 
                                      rounded-lg 
                                      focus:outline-none
                                      ring-indigo-300 
                                      focus-visible:ring
                                      hover:bg-gray-100 
                                      dark:text-white dark:hover:bg-gray-700">{menu}</a>
              <button onClick={(target)=>prop.handleEditNamed({command:prop.command.toLowerCase()
                                                             ,name:menu
                                                             ,x:target.clientX
                                                             ,y:target.clientY
                                                             ,afterEdge:target.clientY > 300
                                                             ,setMenuList})} 
                className="p-1
                           ring-indigo-300 
                           focus-visible:ring ">
                <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px" fill="#5f6368">
                  <path d="M480-160q-33 0-56.5-23.5T400-240q0-33 23.5-56.5T480-320q33 0 56.5 23.5T560-240q0 33-23.5 56.5T480-160Zm0-240q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400Zm0-240q-33 0-56.5-23.5T400-720q0-33 23.5-56.5T480-800q33 0 56.5 23.5T560-720q0 33-23.5 56.5T480-640Z"/>
                </svg>
              </button>
            </li>
          )
        })}
        <li>
          <button onClick={handleAddNewName} className="flex items-center justify-start border-2 border-gray-200 border-dashed
                                      ms-2 w-full p-1 
                                      text-gray-900 
                                      transition duration-75 
                                      rounded-lg 
                                      ring-indigo-300 
                                      focus-visible:ring 
                                      hover:bg-gray-100 
                                      dark:text-white dark:hover:bg-gray-700">
            <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px">
              <path d="M440-440H200v-80h240v-240h80v240h240v80H520v240h-80v-240Z"/>
            </svg>
          </button>
        </li>
      </>
    );
  }
  