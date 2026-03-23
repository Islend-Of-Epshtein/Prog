using System.Collections;
using System.Collections.Generic;
using System.Reflection;

namespace L3S4
{
    internal class App2 : App0
    {
        private Type? menu, auth, menuItem;
        private object? menuObj, authObj;
        private IEnumerable? result;
        private PropertyInfo? titleProp, levelProp, accessProp, methodNameProp, childProp;

        public App2(string menuPath, string usersPath)
        {
            menu = Assembly.LoadFrom("MenuLibrary.dll").GetType("MenuLibrary.MenuModel");
            auth = Assembly.LoadFrom("AuthLibrary.dll").GetType("AuthLibrary.AuthService");

            if (menu == null || auth == null)
                throw new System.Exception("Не удалось загрузить типы из сборок.");

            menuObj = Activator.CreateInstance(menu, menuPath);
            authObj = Activator.CreateInstance(auth, usersPath);
        }

        public override bool CheckPassword(string Name, string Password)
        {
            var loginAuth = auth?.GetMethod("Login");
            var applyMenu = menu?.GetMethod("ApplyPermissions");
            var getRootMenu = menu?.GetMethod("GetRootMenu");

            if (loginAuth == null || applyMenu == null || getRootMenu == null)
                return false;

            object? loginCheck = loginAuth.Invoke(authObj, new string[] { Name, Password });
            if (loginCheck != null)
            {
                applyMenu.Invoke(menuObj, new object[] { loginCheck });
                object? rootMenu = getRootMenu.Invoke(menuObj, null);
                result = rootMenu as IEnumerable;

                var menuItemType = Assembly.LoadFrom("MenuLibrary.dll").GetType("MenuLibrary.MenuItem");
                if (menuItemType == null) return false;

                titleProp = menuItemType.GetProperty("Title");
                levelProp = menuItemType.GetProperty("Level");
                accessProp = menuItemType.GetProperty("Status");
                methodNameProp = menuItemType.GetProperty("MethodName");
                childProp = menuItemType.GetProperty("Children");

                var elements = new List<Element>();
                ConvertMenuItemsAndAddToBase(elements, result);
                SetElements(elements);
                return true;
            }
            return false;
        }

        private void ConvertMenuItemsAndAddToBase(List<Element> elements, IEnumerable? items)
        {
            if (items == null) return;
            foreach (var item in items)
            {
                int level = (int)(levelProp?.GetValue(item) ?? 0);
                string name = titleProp?.GetValue(item) as string ?? "";
                int access = (int)(accessProp?.GetValue(item) ?? 0);
                string methodName = methodNameProp?.GetValue(item) as string ?? "";
                var children = childProp?.GetValue(item) as IEnumerable;

                elements.Add(new Element(level, name, access, methodName));

                if (children != null)
                    ConvertMenuItemsAndAddToBase(elements, children);
            }
        }
    }
}