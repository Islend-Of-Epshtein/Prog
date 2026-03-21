using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace L3S4
{
    
    /// <summary>
    /// Вариант позднего связывания
    /// </summary>\
    
    internal class App2 : App0
    {
        Assembly assemblyMenuLibrary;
        Assembly assemblyAuthLibrary;
      
        public App2() {
            this.assemblyMenuLibrary = Assembly.LoadFrom("MenuLibrary.dll");
            this.assemblyAuthLibrary = Assembly.LoadFrom("AuthLibrary.dll");
            Type? AS = assemblyMenuLibrary.GetType("AuthService");
            if (AS is not null)
            {
                MethodInfo? constructAuth = AS.GetMethod("AuthService");
                // вызываем метод, передаем ему значения для параметров и получаем результат
                object? result = constructAuth?.Invoke(null, new string[] { "users.txt" });
            }
            Type? MM = assemblyMenuLibrary.GetType("MenuModel");
            if (MM is not null)
            {
                MethodInfo? constructAuth = MM.GetMethod("MenuModel");
                // вызываем метод, передаем ему значения для параметров и получаем результат
                object? result = constructAuth?.Invoke(null, new string[] { "menu.txt" });
            }
        }
    }
}
