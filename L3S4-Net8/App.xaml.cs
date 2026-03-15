using System;
using System.IO;
using System.Windows;

namespace L3S4
{
    public partial class App : System.Windows.Application
    {
        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            string basePath = AppDomain.CurrentDomain.BaseDirectory;
            string menuPath = Path.Combine(basePath, "menu.txt");
            string usersPath = Path.Combine(basePath, "users.txt");

            new App1(menuPath, usersPath);
        }
    }
}