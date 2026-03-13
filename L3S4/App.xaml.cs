
using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;

namespace L3S4
{
    /// <summary>
    /// Логика взаимодействия для App.xaml и реализация меню
    /// </summary>
    public partial class App : Application
    {
        private void Application_Startup(object sender, StartupEventArgs e)
        {
            new App0();
        }
    }
}
