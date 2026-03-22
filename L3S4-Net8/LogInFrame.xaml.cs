#nullable enable
using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Effects;
using WpfBrushes = System.Windows.Media.Brushes;
using WpfColor = System.Windows.Media.Color;
using WpfColorConverter = System.Windows.Media.ColorConverter;
using WpfFontFamily = System.Windows.Media.FontFamily;
using WpfSolidColorBrush = System.Windows.Media.SolidColorBrush;

namespace L3S4
{
    public partial class LogInFrame : Window
    {
        private string _password = string.Empty;
        private readonly App0 obj;
        private bool _isUpdatingPassword;

        public LogInFrame(App0 app)
        {
            obj = app ?? throw new ArgumentNullException(nameof(app));
            DataContext = app;
            InitializeComponent();
            Visibility = System.Windows.Visibility.Visible;
            Show();
        }

        private void buttonEnter_Click(object sender, RoutedEventArgs e)
        {
            string login = FieldName.Text.Trim();
            string password = _password.Trim();

            if (obj.CheckPassword(login, password))
            {
                MenuMain menuMain = new(obj);
                menuMain.Show();
                Close();
            }
            else
            {
                ShowErrorToast("Неверный логин или пароль!", 500);
            }
        }

        private void FieldPaswword_TextChanged(object sender, TextChangedEventArgs e)
        {
            if (_isUpdatingPassword) return;

            _isUpdatingPassword = true;

            string currentText = FieldPaswword.Text;
            if (currentText.Length > _password.Length)
                _password += currentText[^1];
            else if (currentText.Length < _password.Length && _password.Length > 0)
                _password = _password[..currentText.Length];

            FieldPaswword.Text = new string('*', _password.Length);
            FieldPaswword.SelectionStart = FieldPaswword.Text.Length;

            _isUpdatingPassword = false;
        }

        private void buttonCanel_Click(object sender, RoutedEventArgs e) => Close();

        private void FieldName_TextChanged(object sender, TextChangedEventArgs e) { }

        public static void ShowErrorToast(string message, int duration = 3000)
        {
            Window toast = new()
            {
                Width = 350,
                Height = 80,
                WindowStyle = WindowStyle.None,
                AllowsTransparency = true,
                Background = WpfBrushes.Transparent,
                WindowStartupLocation = WindowStartupLocation.CenterScreen,
                Topmost = true,
                ShowInTaskbar = false
            };

            Border border = new()
            {
                Background = new WpfSolidColorBrush((WpfColor)WpfColorConverter.ConvertFromString("#FEE2E2")!),
                BorderBrush = new WpfSolidColorBrush((WpfColor)WpfColorConverter.ConvertFromString("#FECACA")!),
                BorderThickness = new Thickness(1),
                CornerRadius = new CornerRadius(10),
                Margin = new Thickness(10),
                Effect = new DropShadowEffect { BlurRadius = 15, ShadowDepth = 2, Opacity = 0.3 }
            };

            Grid grid = new() { Margin = new Thickness(15) };
            grid.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            grid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });

            TextBlock icon = new()
            {
                Text = "⚠️",
                FontSize = 24,
                FontFamily = new WpfFontFamily("Segoe UI Emoji"),
                VerticalAlignment = VerticalAlignment.Center,
                Margin = new Thickness(0, 0, 10, 0)
            };
            Grid.SetColumn(icon, 0);

            TextBlock textBlock = new()
            {
                Text = message,
                Foreground = new WpfSolidColorBrush((WpfColor)WpfColorConverter.ConvertFromString("#991B1B")!),
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
                timer.Tick += (_, _) =>
                {
                    timer.Stop();
                    toast.Close();
                };
                timer.Start();
            };

            toast.Show();
        }
    }
}