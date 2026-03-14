
using System.Collections.Generic;

//Раннее связывание.

using AuthLibrary;
using MenuLibrary;

namespace L3S4
{
    /// <summary>
    /// Вариант раннего связывания
    /// </summary>
    internal class App1 : App0
    {
        private AuthLibrary.AuthService authLibrary;
        private MenuLibrary.MenuModel menuModel;    
        private List<MenuItem> menuItems;
        public App1(string menuPath,string usersPath)
        { 
            this.authLibrary = new AuthService(usersPath);
            this.menuModel = new MenuModel(menuPath);
        }

        public override bool CheckPassword(string Name, string Password)
        {
            Dictionary<string, int> notAveable = authLibrary.Login(Name, Password);
            if (notAveable != null) {
                

                menuModel.ApplyPermissions(notAveable);
                menuItems =  menuModel.GetRootMenu();
                ConvertMenuItemsAndAddToBase();
                return true;  
            }
            else { return false; }    
            
        }
        private void ConvertMenuItemsAndAddToBase()
        {
            List<Element> elements = new List<Element>();
            foreach (var item in menuItems)
            {
                Element element = new Element(item.Level, item.Title, item.MethodName);
                elements.Add(element);
            }
            base.elements = elements;
        }
    }
}
