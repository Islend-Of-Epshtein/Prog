using System.Collections;
using System.Reflection;
using System.Xml.Linq;

namespace L3S4
{
    internal class App2 : App0
    {
        private Type? menu, auth, menuItem;
        private object? menuObj, authObj;
        private IEnumerable result;

        public App2(string menuPath, string usersPath)
        {
            menu = Assembly.LoadFrom("MenuLibrary.dll").GetType("MenuLibrary.MenuModel");
            auth = Assembly.LoadFrom("AuthLibrary.dll").GetType("AuthLibrary.AuthService");

            menuObj = Activator.CreateInstance(menu, menuPath);
            authObj = Activator.CreateInstance(auth, usersPath);

            result = new List<object>();
        }

        public override bool CheckPassword(string Name, string Password)
        {
            MethodInfo? LoginAuth = auth.GetMethod("Login");
            MethodInfo? ApplyMenu = menu.GetMethod("ApplyPermissions");
            MethodInfo? GetRootMenu = menu.GetMethod("GetRootMenu");

            object? logincheck = LoginAuth?.Invoke(authObj, new string[] { Name, Password });
            if (logincheck is not null)
            {
                ApplyMenu?.Invoke(menuObj, new object[] { logincheck });
                object? RootMenu = GetRootMenu?.Invoke(menuObj, null);
                result = RootMenu as IEnumerable;
                ConvertMenuItemsAndAddToBase();
                return true;
            }

            return false;
        }

        private void ConvertMenuItemsAndAddToBase()
        {
            List<Element> elements = new List<Element>();
            Assembly menuitems = Assembly.LoadFrom("MenuLibrary.dll");
            menuItem = menuitems.GetType("MenuLibrary.MenuItem");

            PropertyInfo? titleProp = menuItem.GetProperty("Title");
            PropertyInfo? levelProp = menuItem.GetProperty("Level");
            PropertyInfo? methodNameProp = menuItem.GetProperty("MethodName");

            foreach (var item in result)
            {
                int Level = (int)levelProp.GetValue(item);
                string Name = titleProp.GetValue(item) as string ?? string.Empty;
                string MethodName = methodNameProp.GetValue(item) as string ?? string.Empty;

                Element element = new Element(Level, Name, MethodName);
                elements.Add(element);
            }

            SetElements(elements);
        }
    }
}