using System;
using System.Collections.Generic;
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
        //!!! Т.к обновление списка происходит в наследниках
        //    то необходимо создать событие в set или метод для открытия окна.
        //    окно должно открыться когда в elements появятся значения.
        internal List<Element> elements;
                                                                

        public string CapsLookGet
        {
            get
            {
                return this._capsLook ? "Нажата" : "Не нажата";
            }
        }
        public bool CapsLookSet
        {
            set
            {
                this._capsLook = value;
                OnPropertyChanged(nameof(CapsLookGet));
                OnPropertyChanged(nameof(CapsLookSet));
            }
        }
        public string SelectLanguage
        {
            get { return this._language; }
            set
            {
                this._language = value;
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
        private void Init() {
            _selectLanguage = InputLanguage.CurrentInputLanguage;
            SelectLanguage = GetDisplayLanguageName(_selectLanguage); //конверитируем название

            InitializeTimer(100);

            InitLogInFrame();
        }
        private void InitializeTimer(int updateTime)
        {
            _timer = new DispatcherTimer();
            _timer.Interval = TimeSpan.FromMilliseconds(updateTime); // Проверка каждые 100 мс
            _timer.Tick += Timer_Tick;
            _timer.Start();
        }

        private void Timer_Tick(object sender, EventArgs e)
        {
            // Проверка состояния CapsLock
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
            string cultureName = language.Culture.EnglishName;
           
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

        // Метод для проверки состояния CapsLock
        private bool IsCapsLockOn()
        {
            return Control.IsKeyLocked(Keys.CapsLock);
        }

        // Метод для ручного обновления
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

        // Метод для остановки таймера (при закрытии приложения)
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
        public virtual bool CheckPassword(string Name, string Password) { return false; }
        public string Version
        {
            get { 
                return "Версия "+ _version; 
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
    }
}