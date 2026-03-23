using AuthLibrary;
using MenuLibrary;
using System.Collections.Generic;

namespace L3S4
{
    internal class App1 : App0
    {
        private AuthService authLibrary;
        private MenuModel menuModel;

        public App1(string menuPath, string usersPath)
        {
            authLibrary = new AuthService(usersPath);
            menuModel = new MenuModel(menuPath);
        }

        public override bool CheckPassword(string Name, string Password)
        {
            Dictionary<string, int> notAvailable = authLibrary.Login(Name, Password);
            if (notAvailable != null)
            {
                menuModel.ApplyPermissions(notAvailable);
                var rootItems = menuModel.GetRootMenu();
                var elements = new List<Element>();
                ConvertMenuItemsAndAddToBase(elements, rootItems);
                SetElements(elements);
                return true;
            }
            return false;
        }

        private void ConvertMenuItemsAndAddToBase(List<Element> elements, List<MenuItem> items)
        {
            foreach (var item in items)
            {
                // Level → TreeNumber, Status → Access
                Element element = new Element(item.Level, item.Title, item.Status, item.MethodName);
                elements.Add(element);
                if (item.Children != null)
                    ConvertMenuItemsAndAddToBase(elements, item.Children);
            }
        }
    }
}