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
            System.Windows.MessageBox.Show(
                $"menuPath = {menuPath}\nusersPath = {usersPath}",
                "Paths");
            if (System.IO.File.Exists(usersPath))
            {
                System.Windows.MessageBox.Show(
                    System.IO.File.ReadAllText(usersPath),
                    "users.txt content");
            }
            else
            {
                System.Windows.MessageBox.Show("users.txt not found", "users.txt");
            }

            new App2(menuPath, usersPath);
        }
    }
}