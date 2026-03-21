#nullable enable
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Windows.Forms;
using System.Windows.Threading;

using WpfMessageBox = System.Windows.MessageBox;
using WpfMessageBoxButton = System.Windows.MessageBoxButton;
using WpfMessageBoxImage = System.Windows.MessageBoxImage;

namespace L3S4
{
    public class App0 : INotifyPropertyChanged
    {
        private bool _capsLook;
        private InputLanguage _selectLanguage = null!;
        private string _language = string.Empty;
        private DispatcherTimer? _timer;
        private string _version = "1.1.1.1";

        internal List<Element> elements = new();

        public event EventHandler? ElementsUpdated;
        public event PropertyChangedEventHandler? PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string? propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

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
            Init();
        }

        public App0(string version)
        {
            Version = version;
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
            _timer.Tick += Timer_Tick!;
            _timer.Start();
        }

        private void Timer_Tick(object? sender, EventArgs e)
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

        private static string GetDisplayLanguageName(InputLanguage language)
        {
            return language.LayoutName switch
            {
                "Русская" or "Russian" => "Русский",
                "США" or "US" or "English" or "Английский" => "Английский",
                _ => language.LayoutName
            };
        }

        private static bool IsCapsLockOn() => Control.IsKeyLocked(Keys.CapsLock);

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
                _timer.Tick -= Timer_Tick!;
                _timer = null;
            }
        }

        private void InitLogInFrame()
        {
            _ = new LogInFrame(this);
        }

        public virtual bool CheckPassword(string Name, string Password)
        {
            throw new Exception("Вызван базовый метод CheckPassword. Dll не загружен.");
        }

        public string Version
        {
            get => "Версия " + _version;
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
                if (string.IsNullOrWhiteSpace(raw)) continue;

                var line = raw.Trim();
                if (line.StartsWith('#') || line.StartsWith("//")) continue;

                var tokens = line.Split(new[] { ' ', '\t' }, StringSplitOptions.RemoveEmptyEntries);
                if (tokens.Length < 2) continue;

                if (!int.TryParse(tokens[0], out int tree)) continue;

                string? method = null;
                string name;

                if (tokens.Length == 2)
                {
                    name = tokens[1];
                }
                else
                {
                    var last = tokens[^1];
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

        public IReadOnlyList<Element> GetElements() => elements.AsReadOnly();

        public int GetCountInHierarchy(int treeNumber) => elements.Count(e => e.TreeNumber == treeNumber);

        public string? GetMethodNameByIndex(int index) => index >= 0 && index < elements.Count ? elements[index].MethodName : null;

        public string? GetNameByIndex(int index) => index >= 0 && index < elements.Count ? elements[index].Name : null;

        public int GetTreeNumberByIndex(int index) => index >= 0 && index < elements.Count ? elements[index].TreeNumber : -1;

        public void InvokeMethod(string methodName)
        {
            if (string.IsNullOrWhiteSpace(methodName))
            {
                WpfMessageBox.Show("Метод не задан", "Информация", WpfMessageBoxButton.OK, WpfMessageBoxImage.Information);
                return;
            }

            var mi = GetType().GetMethod(methodName, BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic, null, Type.EmptyTypes, null);
            if (mi != null)
            {
                try
                {
                    mi.Invoke(this, null);
                }
                catch (Exception ex)
                {
                    WpfMessageBox.Show($"Ошибка при вызове метода {methodName}: {ex.Message}", "Ошибка", WpfMessageBoxButton.OK, WpfMessageBoxImage.Error);
                }
                return;
            }
        }

        public static void OpenDepartments()
        {
            WpfMessageBox.Show("Открыть: Отделы", "Действие", WpfMessageBoxButton.OK, WpfMessageBoxImage.Information);
        }
    }
}
#nullable restore