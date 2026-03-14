using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Effects;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;


namespace L3S4
{
    
    /// <summary>
    /// Логика взаимодействия для LogInFrame.xaml
    /// </summary>
    public partial class LogInFrame : Window
    {
        private string _password;
        private App0 obj;
        public LogInFrame(Object obj)
        {
            this.obj = (App0)obj;
            this.DataContext = obj;
            InitializeComponent();
            Visibility = Visibility.Visible;
            Show();
        }

        private void buttonEnter_Click(object sender, RoutedEventArgs e)
        {
            if (obj.CheckPassword(FieldName.Text.Trim(), FieldPaswword.Text.Trim()))
            {
                this.Close();

            }
            else
            {
                ShowErrorToast("Неверный логин или пароль!",500);
            }
        }

        private void FieldPaswword_TextChanged(object sender, TextChangedEventArgs e)
        {
            _password = FieldPaswword.Text;
            string arr = new string('*',FieldPaswword.Text.Length);
            FieldPaswword.Text = arr;
            FieldPaswword.Select(arr.Length, arr.Length);
        }

       
        public static void ShowErrorToast(string message, int duration = 3000)
        {
            var toast = new Window
            {
                Width = 350,
                Height = 80,
                WindowStyle = WindowStyle.None,
                AllowsTransparency = true,
                Background = System.Windows.Media.Brushes.Transparent,
                WindowStartupLocation = WindowStartupLocation.CenterScreen,
                Topmost = true,
                ShowInTaskbar = false
            };

            var border = new Border
            {
                Background = new SolidColorBrush((System.Windows.Media.Color)System.Windows.Media.ColorConverter.ConvertFromString("#FEE2E2")),
                BorderBrush = new SolidColorBrush((System.Windows.Media.Color)System.Windows.Media.ColorConverter.ConvertFromString("#FECACA")),
                BorderThickness = new Thickness(1),
                CornerRadius = new CornerRadius(10),
                Margin = new Thickness(10),
                Effect = new DropShadowEffect
                {
                    BlurRadius = 15,
                    ShadowDepth = 2,
                    Opacity = 0.3
                }
            };

            var grid = new Grid { Margin = new Thickness(15) };
            grid.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            grid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });

            var icon = new TextBlock
            {
                Text = "⚠️",
                FontSize = 24,
                FontFamily = new System.Windows.Media.FontFamily("Segoe UI Emoji"),
                VerticalAlignment = VerticalAlignment.Center,
                Margin = new Thickness(0, 0, 10, 0)
            };
            Grid.SetColumn(icon, 0);

            var textBlock = new TextBlock
            {
                Text = message,
                Foreground = new SolidColorBrush((System.Windows.Media.Color)System.Windows.Media.ColorConverter.ConvertFromString("#991B1B")),
                FontSize = 14,
                FontWeight = FontWeights.Medium,
                VerticalAlignment = VerticalAlignment.Center,
                TextWrapping = TextWrapping.Wrap
            };
            Grid.SetColumn(textBlock, 1);

            grid.Children.Add(icon);
            grid.Children.Add(textBlock);
            border.Child = grid;
            toast.Content = border;

            toast.Loaded += (s, e) =>
            {
                var timer = new System.Windows.Threading.DispatcherTimer { Interval = TimeSpan.FromMilliseconds(duration) };
                timer.Tick += (sender, args) =>
                {
                    timer.Stop();
                    toast.Close();
                };
                timer.Start();
            };

            toast.Show();
        }

        private void buttonCanel_Click(object sender, RoutedEventArgs e)
        {
            this.Close();
        } 
        private void FieldName_TextChanged(object sender, TextChangedEventArgs e)
        {

        }
    }
}
