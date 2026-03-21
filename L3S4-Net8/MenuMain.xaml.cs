using System;
using System.Linq;
using System.Reflection;
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

            Loaded += MenuMain_Loaded;
            _app.ElementsUpdated += App_ElementsUpdated;
        }

        private void MenuMain_Loaded(object sender, RoutedEventArgs e)
        {
            BuildMenuFromElements();
        }

        private void App_ElementsUpdated(object sender, EventArgs e)
        {
            BuildMenuFromElements();
        }

        protected override void OnClosed(EventArgs e)
        {
            _app.ElementsUpdated -= App_ElementsUpdated;
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

            for (int rootIndex = 0; rootIndex < all.Count; rootIndex++)
            {
                var root = all[rootIndex];
                if (root.TreeNumber != 0)
                    continue;

                var rootMenuItem = new MenuItem
                {
                    Header = root.Name,
                    Tag = root
                };

                int childCount = 0;

                for (int i = rootIndex + 1; i < all.Count; i++)
                {
                    if (all[i].TreeNumber == 0)
                        break;

                    if (all[i].TreeNumber == 1)
                    {
                        var child = all[i];
                        var childMenuItem = new MenuItem
                        {
                            Header = child.Name,
                            Tag = child
                        };

                        childMenuItem.Click += LeafMenuItem_Click;
                        rootMenuItem.Items.Add(childMenuItem);
                        childCount++;
                    }
                }

                // Если детей нет — это обычный пункт, по нему можно кликнуть
                if (childCount == 0)
                {
                    rootMenuItem.Click += LeafMenuItem_Click;
                }

                MainMenu.Items.Add(rootMenuItem);
            }

            HeaderText.Text = "Выбери раздел меню";
        }

        private bool HasMethod(string methodName)
        {
            if (string.IsNullOrWhiteSpace(methodName))
                return false;

            var method = _app.GetType().GetMethod(
                methodName,
                BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic,
                null,
                Type.EmptyTypes,
                null);

            return method != null;
        }

        private void LeafMenuItem_Click(object sender, RoutedEventArgs e)
        {
            if (sender is not MenuItem menuItem || menuItem.Tag is not Element element)
                return;

            HeaderText.Text = $"Выбран пункт: {element.Name}";

            // Если метод реально есть — вызываем.
            // Если метода нет — просто показываем название пункта в окне.
            if (HasMethod(element.MethodName))
            {
                _app.InvokeMethod(element.MethodName);
            }
        }
    }
}