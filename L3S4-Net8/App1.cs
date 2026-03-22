using AuthLibrary;
using L3S4;
using MenuLibrary;
using System;
using System.Collections.Generic;
using System.Linq;

namespace L3S4;

internal class App1 : App0
{
    private readonly AuthService authLibrary;
    private readonly string menuPath;

    public App1(string menuPath, string usersPath) : base()
    {
        this.menuPath = menuPath;
        authLibrary = new AuthService(usersPath);
    }

    public override bool CheckPassword(string Name, string Password)
    {
        Dictionary<string, int>? permissions = authLibrary.Login(Name, Password);
        if (permissions == null)
            return false;

        // Загружаем меню из файла
        LoadMenuFile(menuPath);

        // Применяем права к загруженным элементам
        var elementsWithAccess = new List<Element>();
        foreach (var element in elements)
        {
            int access = permissions.TryGetValue(element.Name, out int userAccess) ? userAccess : 0;
            elementsWithAccess.Add(new Element(element.TreeNumber, element.Name, element.MethodName, access));
        }
        SetElements(elementsWithAccess);

        return true;
    }
}