using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Windows.Threading;
using System.Windows.Forms;

using WpfMessageBox = System.Windows.MessageBox;
using WpfMessageBoxButton = System.Windows.MessageBoxButton;
using WpfMessageBoxImage = System.Windows.MessageBoxImage;

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

        public string CapsLookGet
        {
            get
            {
                return _capsLook ? "Нажата" : "Не нажата";
            }
        }

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
            get
            {
                return _language;
            }
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
            Version = version;
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
            {
                CapsLookSet = currentCapsLock;
            }

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

        private bool IsCapsLockOn()
        {
            return Control.IsKeyLocked(Keys.CapsLock);
        }

        public void RefreshKeyboardState()
        {
            bool currentCapsLock = IsCapsLockOn();
            if (currentCapsLock != _capsLook)
            {
                CapsLookSet = currentCapsLock;
            }

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
            LogInFrame logInFrame = new LogInFrame(this);
        }

        public virtual bool CheckPassword(string Name, string Password)
        {
            throw new Exception("Вызван базовый метод CheckPasword. Dll не загружен.");
        }

        public string Version
        {
            get
            {
                return "Версия " + _version;
            }
            set
            {
                if (_version != value)
                {
                    _version = value;
                    OnPropertyChanged(nameof(Version));
                }
            }
        }

        protected void SetElements(IEnumerable<Element> newElements)
        {
            elements = newElements?.ToList() ?? new List<Element>();
            ElementsUpdated?.Invoke(this, EventArgs.Empty);
        }

        public void LoadMenuFile(string path)
        {
            if (!File.Exists(path))
            {
                throw new FileNotFoundException("menu file not found", path);
            }

            var list = new List<Element>();

            foreach (var raw in File.ReadAllLines(path))
            {
                if (string.IsNullOrWhiteSpace(raw))
                    continue;

                var line = raw.Trim();

                if (line.StartsWith("#") || line.StartsWith("//"))
                    continue;

                var tokens = line.Split(new[] { ' ', '\t' }, StringSplitOptions.RemoveEmptyEntries);
                if (tokens.Length < 2)
                    continue;

                if (!int.TryParse(tokens[0], out int tree))
                    continue;

                string method = null;
                string name;

                if (tokens.Length == 2)
                {
                    name = tokens[1];
                }
                else
                {
                    var last = tokens[tokens.Length - 1];

                    if (last.Equals("Null", StringComparison.OrdinalIgnoreCase))
                    {
                        method = null;
                        name = string.Join(" ", tokens.Skip(1));
                    }
                    else
                    {
                        method = last;
                        name = string.Join(" ", tokens.Skip(1).Take(tokens.Length - 2));
                    }
                }

                list.Add(new Element(tree, name, method));
            }

            SetElements(list);
        }

        public IReadOnlyList<Element> GetElements()
        {
            return elements.AsReadOnly();
        }

        public int GetCountInHierarchy(int treeNumber)
        {
            return elements.Count(e => e.TreeNumber == treeNumber);
        }

        public string GetMethodNameByIndex(int index)
        {
            if (index < 0 || index >= elements.Count)
                return null;

            return elements[index].MethodName;
        }

        public string GetNameByIndex(int index)
        {
            if (index < 0 || index >= elements.Count)
                return null;

            return elements[index].Name;
        }

        public int GetTreeNumberByIndex(int index)
        {
            if (index < 0 || index >= elements.Count)
                return -1;

            return elements[index].TreeNumber;
        }

        public void InvokeMethod(string methodName)
        {
            if (string.IsNullOrWhiteSpace(methodName))
            {
                WpfMessageBox.Show(
                    "Метод не задан",
                    "Информация",
                    WpfMessageBoxButton.OK,
                    WpfMessageBoxImage.Information);
                return;
            }

            var mi = GetType().GetMethod(
                methodName,
                BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic,
                null,
                Type.EmptyTypes,
                null);

            if (mi != null)
            {
                try
                {
                    mi.Invoke(this, null);
                }
                catch (Exception ex)
                {
                    WpfMessageBox.Show(
                        $"Ошибка при вызове метода {methodName}: {ex.Message}",
                        "Ошибка",
                        WpfMessageBoxButton.OK,
                        WpfMessageBoxImage.Error);
                }

                return;
            }

            WpfMessageBox.Show(
                $"Метод '{methodName}' не найден в App0",
                "Информация",
                WpfMessageBoxButton.OK,
                WpfMessageBoxImage.Information);
        }

        public void OpenDepartments()
        {
            WpfMessageBox.Show(
                "Открыть: Отделы",
                "Действие",
                WpfMessageBoxButton.OK,
                WpfMessageBoxImage.Information);
        }
    }
}