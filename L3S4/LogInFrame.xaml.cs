using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace L3S4
{
    /// <summary>
    /// Логика взаимодействия для LogInFrame.xaml
    /// </summary>
    public partial class LogInFrame : Window
    {

        public LogInFrame()
        {
            InitializeComponent();
            Binding binding = new Binding();
            Version = "версия 1.1.1.1";
            binding.ElementName = "LogInFrame"; // элемент-источник
            binding.Path = new PropertyPath("Version"); // свойство элемента-источника
            VersionBlock.SetBinding(TextBlock.TextProperty, binding); // установка привязки для элемента-приемника
        }
        public string Version { get; set; }
    }
}
