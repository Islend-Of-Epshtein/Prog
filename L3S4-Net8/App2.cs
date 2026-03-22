using L3S4;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;

namespace L3S4;

internal class App2 : App0
{
    private static readonly Type[] EmptyTypes = Type.EmptyTypes;

    private readonly Type? menu;
    private readonly Type? auth;
    private readonly object? menuObj;
    private readonly object? authObj;
    private IEnumerable result = new List<object>();

    public App2(string menuPath, string usersPath)
    {
        menu = Assembly.LoadFrom("MenuLibrary.dll").GetType("MenuLibrary.MenuModel");
        auth = Assembly.LoadFrom("AuthLibrary.dll").GetType("AuthLibrary.AuthService");

        if (menu == null || auth == null)
            throw new InvalidOperationException("Не удалось загрузить типы из сборок.");

        menuObj = Activator.CreateInstance(menu, menuPath);
        authObj = Activator.CreateInstance(auth, usersPath);
    }

    public override bool CheckPassword(string Name, string Password)
    {
        MethodInfo? loginAuth = auth?.GetMethod("Login");
        MethodInfo? applyMenu = menu?.GetMethod("ApplyPermissions");
        MethodInfo? getRootMenu = menu?.GetMethod("GetRootMenu");

        if (loginAuth == null || applyMenu == null || getRootMenu == null)
            return false;

        object? loginCheck = loginAuth.Invoke(authObj, new[] { Name, Password });
        if (loginCheck != null)
        {
            applyMenu.Invoke(menuObj, new[] { loginCheck });
            object? rootMenu = getRootMenu.Invoke(menuObj, null);
            result = rootMenu as IEnumerable ?? new List<object>();
            ConvertMenuItemsAndAddToBase();
            return true;
        }

        return false;
    }

    private void ConvertMenuItemsAndAddToBase()
    {
        var elements = new List<Element>();
        var menuItemType = Assembly.LoadFrom("MenuLibrary.dll").GetType("MenuLibrary.MenuItem");
        if (menuItemType == null) return;

        var titleProp = menuItemType.GetProperty("Title");
        var levelProp = menuItemType.GetProperty("Level");
        var methodNameProp = menuItemType.GetProperty("MethodName");

        foreach (var item in result)
        {
            int level = (int)(levelProp?.GetValue(item) ?? 0);
            string name = titleProp?.GetValue(item) as string ?? "";
            string methodName = methodNameProp?.GetValue(item) as string ?? "";

            elements.Add(new Element(level, name, methodName));
        }

        SetElements(elements);
    }
}