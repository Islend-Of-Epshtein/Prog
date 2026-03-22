
using System;
using System.IO;
using System.Windows;

namespace L3S4
{
    partial class App : System.Windows.Application
    {
        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            string basePath = AppDomain.CurrentDomain.BaseDirectory;
            string menuPath = Path.Combine(basePath, "menu.txt");
            string usersPath = Path.Combine(basePath, "users.txt");

            MessageBoxResult result = System.Windows.MessageBox.Show(
                "Связывание dll:\n\nДа - Раннее(App1)\nНет - Позднее(App2)",
                "Выбор приложения",
                MessageBoxButton.YesNo,
                MessageBoxImage.Question);

            if (result == MessageBoxResult.Yes)
            {
                new App1(menuPath, usersPath);
            }
            else
            {
                new App2(menuPath, usersPath);
            }
        }
    }
}