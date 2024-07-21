import Header from "./Header";
import SelectParameterProvider from "../context/SelectParameterProvider";
import EditNmaeProvider from "../context/EditNameProvider";
import NameEditMenu from "./sidebar/NameEditMenu";
import NamedParameters from "./sidebar/NamedParameters";
import Form from "./app/Form";
import CommandForm from "./app/CommandForm";
import Footer from "./app/Footer";
import "../styles.css";

export default function Layout() {
  return (
      <>
        <nav className="fixed top-0
                        z-50 
                        w-full 
                        bg-white 
                        border-b border-gray-200 
                        dark:bg-gray-800 dark:border-gray-700">
          <Header />
        </nav>
        <SelectParameterProvider>
          <aside id="logo-sidebar" className="fixed top-0 left-0 
                                              z-40 
                                              w-4=8 h-screen 
                                              pt-20 
                                              transition-transform 
                                              -translate-x-full 
                                              border-r border-gray-200 
                                              sm:translate-x-0 
                                              dark:bg-gray-800 dark:border-gray-700" aria-label="Sidebar">
            <div className="h-full px-3 pb-4 overflow-y-auto dark:bg-gray-800">
              <EditNmaeProvider>
                <NameEditMenu/>
                <ul className="space-y-2 font-medium">
                  <NamedParameters/>
                </ul>
              </EditNmaeProvider>
            </div>
          </aside>
          <div className="p-4 sm:ml-48">
            <Form>
             <CommandForm />
             <Footer />
            </Form>
          </div>
        </SelectParameterProvider>
      </>
  )
}