#nullable enable
using System;
using System.Linq;
using System.Windows;
using System.Windows.Controls;

namespace L3S4
{
    public partial class MenuMain : Window
    {
        private readonly App0 _app;

        public MenuMain(App0 app)
        {
            _app = app;
            InitializeComponent();

            Loaded += MenuMain_Loaded!;
            _app.ElementsUpdated += App_ElementsUpdated!;
        }

        private void MenuMain_Loaded(object? sender, RoutedEventArgs e) => BuildMenuFromElements();

        private void App_ElementsUpdated(object? sender, EventArgs e) => BuildMenuFromElements();

        protected override void OnClosed(EventArgs e)
        {
            _app.ElementsUpdated -= App_ElementsUpdated!;
            base.OnClosed(e);
        }

        private void BuildMenuFromElements()
        {
            MainMenu.Items.Clear();

            var all = _app.GetElements().ToList();
            if (all.Count == 0)
            {
                HeaderText.Text = "Разделы меню не найдены.";
                return;
            }

            int index = 0;
            while (index < all.Count)
            {
                if (all[index].TreeNumber != 0)
                {
                    index++;
                    continue;
                }

                var rootItem = BuildMenuItem(all, ref index, 0);
                MainMenu.Items.Add(rootItem);
            }

            HeaderText.Text = "Выбери раздел меню";
        }

        private MenuItem BuildMenuItem(System.Collections.Generic.IList<Element> nodes, ref int index, int currentLevel)
        {
            var current = nodes[index];
            var item = new MenuItem
            {
                Header = current.Name,
                Tag = current
            };

            index++;

            while (index < nodes.Count)
            {
                int nextLevel = nodes[index].TreeNumber;
                if (nextLevel <= currentLevel)
                    break;

                if (nextLevel == currentLevel + 1)
                {
                    var child = BuildMenuItem(nodes, ref index, currentLevel + 1);
                    item.Items.Add(child);
                }
                else
                {
                    index++;
                }
            }

            if (item.Items.Count == 0)
            {
                item.Click += (sender, e) =>
                {
                    HeaderText.Text = $"Выбран пункт: {current.Name}";
                    if (!string.IsNullOrWhiteSpace(current.MethodName))
                    {
                        _app.InvokeMethod(current.MethodName);
                    }
                };
            }

            return item;
        }
    }
}
#nullable restore