using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Windows.Forms;
using System.Windows.Threading;

namespace L3S4
{
    public class App0 : BaseView
    {
        private bool _capsLook;
        private InputLanguage _selectLanguage;
        private string _language;
        private DispatcherTimer _timer;
        private string _version = "1.1.1.1";

        internal List<Element> elements;

        public event EventHandler ElementsUpdated;

        public string CapsLookGet => _capsLook ? "Нажата" : "Не нажата";

        public bool CapsLookSet
        {
            set
            {
                _capsLook = value;
                OnPropertyChanged(nameof(CapsLookGet));
                OnPropertyChanged(nameof(CapsLookSet));
            }
        }

        public string SelectLanguage
        {
            get => _language;
            set
            {
                _language = value;
                OnPropertyChanged(nameof(SelectLanguage));
            }
        }

        public App0()
        {
            elements = new List<Element>();
            Init();
        }

        public App0(string version)
        {
            _version = version;
            elements = new List<Element>();
            Init();
        }

        private void Init()
        {
            _selectLanguage = InputLanguage.CurrentInputLanguage;
            SelectLanguage = GetDisplayLanguageName(_selectLanguage);
            InitializeTimer(100);
            InitLogInFrame();
        }

        private void InitializeTimer(int updateTime)
        {
            _timer = new DispatcherTimer();
            _timer.Interval = TimeSpan.FromMilliseconds(updateTime);
            _timer.Tick += Timer_Tick;
            _timer.Start();
        }

        private void Timer_Tick(object sender, EventArgs e)
        {
            bool currentCapsLock = IsCapsLockOn();
            if (currentCapsLock != _capsLook)
                CapsLookSet = currentCapsLock;

            InputLanguage currentLanguage = InputLanguage.CurrentInputLanguage;
            if (!currentLanguage.Equals(_selectLanguage))
            {
                _selectLanguage = currentLanguage;
                SelectLanguage = GetDisplayLanguageName(currentLanguage);
                OnPropertyChanged(nameof(SelectLanguage));
            }
        }

        private string GetDisplayLanguageName(InputLanguage language)
        {
            switch (language.LayoutName)
            {
                case "Русская":
                case "Russian":
                    return "Русский";
                case "США":
                case "US":
                case "English":
                case "Английский":
                    return "Английский";
                default:
                    return language.LayoutName;
            }
        }

        private bool IsCapsLockOn() => Control.IsKeyLocked(Keys.CapsLock);

        public void RefreshKeyboardState()
        {
            bool currentCapsLock = IsCapsLockOn();
            if (currentCapsLock != _capsLook)
                CapsLookSet = currentCapsLock;

            InputLanguage currentLanguage = InputLanguage.CurrentInputLanguage;
            if (!currentLanguage.Equals(_selectLanguage))
            {
                _selectLanguage = currentLanguage;
                SelectLanguage = GetDisplayLanguageName(currentLanguage);
            }
        }

        public void CleanupTimer()
        {
            if (_timer != null)
            {
                _timer.Stop();
                _timer.Tick -= Timer_Tick;
                _timer = null;
            }
        }

        private void InitLogInFrame()
        {
            _ = new LogInFrame(this);
        }

        public virtual bool CheckPassword(string Name, string Password) => false;

        public string Version => "Версия " + _version;

        // --- Новые методы для работы с элементами меню ---
        public IReadOnlyList<Element> GetElements() => elements.AsReadOnly();

        public void SetElements(IEnumerable<Element> newElements)
        {
            elements = newElements?.ToList() ?? new List<Element>();
            ElementsUpdated?.Invoke(this, EventArgs.Empty);
        }

        public void InvokeMethod(string methodName)
        {
            if (string.IsNullOrWhiteSpace(methodName))
            {
                System.Windows.MessageBox.Show("Метод не задан", "Информация",
                    System.Windows.MessageBoxButton.OK, System.Windows.MessageBoxImage.Information);
                return;
            }

            var mi = GetType().GetMethod(methodName,
                BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic,
                null, Type.EmptyTypes, null);
            if (mi != null)
            {
                try
                {
                    mi.Invoke(this, null);
                }
                catch (Exception ex)
                {
                    System.Windows.MessageBox.Show($"Ошибка при вызове метода {methodName}: {ex.Message}",
                        "Ошибка", System.Windows.MessageBoxButton.OK, System.Windows.MessageBoxImage.Error);
                }
            }
        }

        // Пример тестового метода
        public void OpenDepartments()
        {
            System.Windows.MessageBox.Show("Открыть: Отделы", "Действие",
                System.Windows.MessageBoxButton.OK, System.Windows.MessageBoxImage.Information);
        }
    }
}