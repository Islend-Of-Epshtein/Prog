using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace L3S4
{
    public class App0 : INotifyPropertyChanged
    {
        
        private string _version;
        List<Element> elements; public event PropertyChangedEventHandler PropertyChanged;
        public App0()
        {
            Version = "1.1.1.1";
            elements = new List<Element>();
            InitLogInFrame("1.1.0.0");
        }
        private void InitLogInFrame(string version)
        {
            Version = version;
            LogInFrame logInFrame = new LogInFrame(this);
            
        }
        public string Version
        {
            get { return _version; }
            set
            {
                if (_version != value)
                {
                    _version = value;
                    OnPropertyChanged(nameof(Version));
                }
            }
        }
        protected void OnPropertyChanged(string propertyName)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

    }
}

