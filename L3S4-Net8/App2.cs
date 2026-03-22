using System.Collections;
using System.Reflection;
using System.Xml.Linq;

namespace L3S4
{
    /// <summary>
    /// Вариант позднего связывания
    /// </summary>\
    internal class App2 : App0
    {
        /*
         * Introduce
         * Как работает позднее связываниее?
         * В C# все просто. Существует класс Assembly который может загружать dll.
         * На выходе мы получаем что то что можно перебрать,
         * это что то содержит типы ( то есть все классы из библиотеки)
         * Выбирая конкретный тип с помощью GetTtpe() можно найти в нем соответствующий метод или свойство
         * с помощью ProprtyInfo и MethodInfo. Далее объект этих типов (наш метод или свойство) можно 
         * вызвать с помощью .Invoke(_Объект вызова, параметры);
         * Также можно создавать объекты соответствующих типов с помощью Activatot.CreateInstance
         */
        private Type? menu, auth, menuItem;
        private object? menuObj, authObj;
        private IEnumerable? result;
        private PropertyInfo titleProp, levelProp, accessProp, methodNameProp, childhave;
        public App2(string menuPath, string usersPath) {
            try
            {
                menu = Assembly.LoadFrom("MenuLibrary.dll").GetType("MenuLibrary.MenuModel");
                auth = Assembly.LoadFrom("AuthLibrary.dll").GetType("AuthLibrary.AuthService");
            }
            catch(Exception ex ) {
                throw new Exception(ex + " Ошибка загрузки dll в позднем связывании");
            }  

            this.menuObj = Activator.CreateInstance(menu, menuPath);
            this.authObj = Activator.CreateInstance(auth, usersPath);

            result = new List<object>();
        }
        public override bool CheckPassword(string Name, string Password) {
            MethodInfo? LoginAuth = auth.GetMethod("Login");
            MethodInfo? ApplyMenu = menu.GetMethod("ApplyPermissions");
            MethodInfo? GetRootMenu = menu.GetMethod("GetRootMenu");

            object? logincheck = LoginAuth?.Invoke(authObj, new string[] { Name, Password });
            if (logincheck is not null)
            {
                ApplyMenu?.Invoke(menuObj,new object[] { logincheck });
                object? RootMenu = GetRootMenu?.Invoke(menuObj, null);
                result = RootMenu as IEnumerable;
                Assembly menuitems = Assembly.LoadFrom("MenuLibrary.dll");
                menuItem = menuitems.GetType("MenuLibrary.MenuItem");
                List<Element> elements = new List<Element>();

                titleProp = menuItem.GetProperty("Title");
                levelProp = menuItem.GetProperty("Level");
                accessProp = menuItem.GetProperty("Status");
                methodNameProp = menuItem.GetProperty("MethodName");
                childhave = menuItem.GetProperty("Children");

                if (titleProp is not null && levelProp is not null &&
                     accessProp is not null &&  methodNameProp is not null &&  childhave is not null)
                {
                    ConvertMenuItemsAndAddToBase(elements, result);
                }
                else
                {
                    throw new InvalidOperationException("Не найдено одно из свойств в MenuItem(.dll)");
                }
                base.elements = elements;
                return true;
            }
            else { return false; }
        }
        private void ConvertMenuItemsAndAddToBase(List<Element> elements, IEnumerable? ListOfMenuItems) {
            if (ListOfMenuItems != null)
            {
                foreach (var item in ListOfMenuItems)
                {
                    int Level = (int?)levelProp.GetValue(item) ?? 0;
                    string Name = titleProp.GetValue(item) as string ?? string.Empty;
                    int Status = (int?)accessProp.GetValue(item) ?? 0;
                    string MethodName = methodNameProp.GetValue(item) as string ?? string.Empty;
                    IEnumerable? listOfMenuItem = childhave.GetValue(item) as IEnumerable ?? null;
                    Element element = new Element(Level, Name, Status, MethodName);
                    elements.Add(element);
                    if (listOfMenuItem != null)
                    {
                        ConvertMenuItemsAndAddToBase(elements, listOfMenuItem);
                    }
                }
            }
        }
    }
}
