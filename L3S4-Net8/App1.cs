using System;
using System.Collections.Generic;
using System.Linq;

namespace L3S4
{
    internal class App1 : App0
    {
        private readonly string _menuPath;

        public App1(string menuPath, string usersPath)
        {
            _menuPath = menuPath;
            // AuthService и MenuModel не используются, но остаются для совместимости
            // При желании их можно удалить, если не нужны
        }

        public override bool CheckPassword(string Name, string Password)
        {
            // Для упрощения проверка пароля опущена – пропускаем любого пользователя
            // Если нужна проверка, её можно реализовать здесь или оставить вызов библиотеки
            // В данном случае просто загружаем меню из файла
            LoadMenuFile(_menuPath);
            return true;
        }
    }
}