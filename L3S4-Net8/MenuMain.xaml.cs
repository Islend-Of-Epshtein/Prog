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

            var roots = _app.GetElements()
                .Where(x => x.TreeNumber == 0)
                .ToList();

            foreach (var root in roots)
            {
                WpfButton button = new WpfButton
                {
                    Content = root.Name,
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

            var all = _app.GetElements().ToList();
            int rootIndex = all.FindIndex(x => ReferenceEquals(x, root));

            if (rootIndex < 0)
            {
                HeaderText.Text = $"Не удалось найти раздел: {root.Name}";
                return;
            }

            int childCount = 0;

            for (int i = rootIndex + 1; i < all.Count; i++)
            {
                if (all[i].TreeNumber == 0)
                {
                    break;
                }

                if (all[i].TreeNumber == 1)
                {
                    Element child = all[i];

                    WpfButton childButton = new WpfButton
                    {
                        Content = child.Name,
                        Tag = child,
                        Margin = new Thickness(4),
                        Padding = new Thickness(10, 6, 10, 6),
                        HorizontalContentAlignment = WpfHorizontalAlignment.Left,
                        MinWidth = 120
                    };

                    childButton.Click += ChildButton_Click;
                    ItemsPanel.Children.Add(childButton);
                    childCount++;
                }
            }

            HeaderText.Text = childCount == 0
                ? $"Раздел: {root.Name}. Подпунктов пока нет."
                : $"Раздел: {root.Name}. Выбери подпункт.";
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
                _app.InvokeMethod(child.MethodName);
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