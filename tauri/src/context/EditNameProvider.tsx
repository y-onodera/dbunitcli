import { createContext, Dispatch, ReactNode, SetStateAction, useContext, useState } from "react";

export type EditName = {
    name:string;
    command:string;
    x:number;
    y:number;
    afterEdge:boolean
    setMenuList:Function;
}
const editNameContext = createContext<EditName>({} as EditName);
const setEditNameContext = createContext<Dispatch<SetStateAction<EditName>>>(
    () => undefined
);
export default function EditNmaeProvider(props:{children:ReactNode}) {
    const [editName, setEditName] = useState<EditName>({} as EditName);
    return (
        <editNameContext.Provider value={editName}>
          <setEditNameContext.Provider value={setEditName}>
            {props.children}
          </setEditNameContext.Provider>
        </editNameContext.Provider>
    );
}
export const useEditName = () => useContext(editNameContext);
export const useSetEditName = () => useContext(setEditNameContext);