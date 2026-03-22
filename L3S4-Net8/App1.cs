

//Раннее связывание. - если нету, то даже не запустится

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
            this.menuItems = new List<MenuItem>();  
        }
        public override bool CheckPassword(string Name, string Password)
        {
            Dictionary<string, int> notAveable = authLibrary.Login(Name, Password);
            if (notAveable != null) {
                menuModel.ApplyPermissions(notAveable);
                menuItems = menuModel.GetRootMenu();
                List<Element> elements = new List<Element>();
                ConvertMenuItemsAndAddToBase(elements, menuItems);
                base.elements = elements;
                return true;  
            }
            else { return false; }    
        }
        private void ConvertMenuItemsAndAddToBase(List<Element> elements, List<MenuItem> items)
        {
            foreach (var item in items)
            {
                Element element = new Element(item.Level, item.Title,item.Status, item.MethodName);
                elements.Add(element);
                if (item.Children!=null) 
                {
                    ConvertMenuItemsAndAddToBase(elements, item.Children);
                }
            }
        }
    }
}
