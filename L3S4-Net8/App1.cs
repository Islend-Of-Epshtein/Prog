using System.Collections.Generic;
using AuthLibrary;
using MenuLibrary;

namespace L3S4
{
    internal class App1 : App0
    {
        private AuthService authLibrary;
        private MenuModel menuModel;
        private List<MenuItem> menuItems;

        public App1(string menuPath, string usersPath)
        {
            authLibrary = new AuthService(usersPath);
            menuModel = new MenuModel(menuPath);
            menuItems = new List<MenuItem>();
        }

        public override bool CheckPassword(string Name, string Password)
        {
            Dictionary<string, int> notAveable = authLibrary.Login(Name, Password);
            if (notAveable != null)
            {
                menuModel.ApplyPermissions(notAveable);
                menuItems = menuModel.GetRootMenu();
                ConvertMenuItemsAndAddToBase();
                return true;
            }

            return false;
        }

        private void ConvertMenuItemsAndAddToBase()
        {
            List<Element> elements = new List<Element>();

            foreach (var item in menuItems)
            {
                Element element = new Element(item.Level, item.Title, item.MethodName);
                elements.Add(element);
            }

            SetElements(elements);
        }
    }
}