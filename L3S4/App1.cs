using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using AuthLibrary;
using MenuLibrary;

namespace L3S4
{
    /// <summary>
    /// Вариант раннего связывания
    /// </summary>
    internal class App1 : App0
    {
        private AuthLibrary.AuthService authLibrary;
        private MenuLibrary.MenuModel menuModel;    
        public App1(string menuPath,string usersPath)
        { 
            this.authLibrary = new AuthService(usersPath);
            this.menuModel = new MenuModel(menuPath);
        }
        private void Init()
        {
            
        }

    }
}
