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
                // Пропускаем элементы, у которых TreeNumber != 0 или Access == 2
                if (all[index].TreeNumber != 0 || all[index].Access == 2)
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
                Tag = current,
                IsEnabled = current.Access != 1  // Если Access == 1, пункт недоступен
            };

            index++;

            while (index < nodes.Count)
            {
                int nextLevel = nodes[index].TreeNumber;
                // Если следующий элемент имеет Access == 2, его и всех его потомков пропускаем
                if (nodes[index].Access == 2)
                {
                    // Пропускаем этот элемент и все элементы на его уровне и выше, пока не встретим нужный уровень
                    int skipLevel = nodes[index].TreeNumber;
                    while (index < nodes.Count && nodes[index].TreeNumber > skipLevel)
                    {
                        index++;
                    }
                    continue;
                }

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

            // Если после добавления детей элемент всё ещё пуст и он недоступен (IsEnabled = false), его можно не показывать
            if (item.Items.Count == 0 && !item.IsEnabled)
            {
                // Такие элементы не добавляем вообще (например, родитель с Access=1 и без детей)
                // В текущей реализации мы всё равно возвращаем item, но он будет добавлен в родительский Items.
                // Если нужно полностью скрыть такие пункты, нужно дополнительно проверять в BuildMenuFromElements.
                // Оставляем как есть, пусть будет серый недоступный пункт.
            }

            if (item.Items.Count == 0)
            {
                item.Click += (sender, e) =>
                {
                    HeaderText.Text = $"Выбран пункт: {current.Name}";
                    if (!string.IsNullOrWhiteSpace(current.MethodName) && current.Access == 0)
                    {
                        _app.InvokeMethod(current.MethodName);
                    }
                    else if (current.Access == 1)
                    {
                        HeaderText.Text = $"Пункт '{current.Name}' недоступен.";
                    }
                };
            }

            return item;
        }
    }
}
#nullable restore