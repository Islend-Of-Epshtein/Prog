using System;
using System.Linq;
using System.Windows;

using WpfButton = System.Windows.Controls.Button;
using WpfWrapPanel = System.Windows.Controls.WrapPanel;
using WpfMessageBox = System.Windows.MessageBox;
using WpfMessageBoxButton = System.Windows.MessageBoxButton;
using WpfMessageBoxImage = System.Windows.MessageBoxImage;
using WpfHorizontalAlignment = System.Windows.HorizontalAlignment;

namespace L3S4
{
    public partial class MenuMain : Window
    {
        private readonly App0 _app;

        public MenuMain(App0 app)
        {
            _app = app;
            InitializeComponent();

            Loaded += MenuMain_Loaded;
        }

        private void MenuMain_Loaded(object sender, RoutedEventArgs e)
        {
            BuildButtonsFromElements();
        }

        private void BuildButtonsFromElements()
        {
            ButtonsPanel.Children.Clear();
            ItemsPanel.Children.Clear();

            var roots = new List<string> { };

            foreach (var root in roots)
            {
                WpfButton button = new WpfButton
                {
                    
                    Tag = root,
                    Margin = new Thickness(5),
                    Padding = new Thickness(12, 6, 12, 6),
                    MinWidth = 110
                };

                button.Click += RootButton_Click;
                ButtonsPanel.Children.Add(button);
            }

            HeaderText.Text = roots.Count == 0
                ? "Разделы меню не найдены в menu.txt"
                : "Выбери раздел сверху";
        }

        private void RootButton_Click(object sender, RoutedEventArgs e)
        {
            if (sender is not WpfButton button || button.Tag is not Element root)
            {
                return;
            }

            ItemsPanel.Children.Clear();

            var all = new List<string> { };
            int rootIndex = all.FindIndex(x => ReferenceEquals(x, root));

            if (rootIndex < 0)
            {
                HeaderText.Text = $"Не удалось найти раздел: {root.Name}";
                return;
            }

            int childCount = 0;

            
        }

        private void ChildButton_Click(object sender, RoutedEventArgs e)
        {
            if (sender is not WpfButton button || button.Tag is not Element child)
            {
                return;
            }

            HeaderText.Text = $"Выбран пункт: {child.Name}";

            if (!string.IsNullOrWhiteSpace(child.MethodName))
            {
                
            }
            else
            {
                WpfMessageBox.Show(
                    $"Пункт: {child.Name}",
                    "Информация",
                    WpfMessageBoxButton.OK,
                    WpfMessageBoxImage.Information);
            }
        }
    }
}