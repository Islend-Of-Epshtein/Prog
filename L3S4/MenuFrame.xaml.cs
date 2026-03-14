using System;
using System.Windows;
using System.Windows.Controls;

namespace L3S4
{
    /// <summary>
    /// Логика взаимодействия для MenuFrame.xaml
    /// </summary>
    public partial class MenuFrame : Window
    {
        private readonly App0 _app;

        public MenuFrame(App0 app)
        {
            InitializeComponent();
            _app = app ?? throw new ArgumentNullException(nameof(app));
            BuildStaticTopMenu();
            BuildReferenceMenu();

            _app.ElementsUpdated += (s, e) => Dispatcher.Invoke(BuildReferenceMenu);
        }

        private void BuildStaticTopMenu()
        {
            MainMenu.Items.Clear();

            // статические пункты сверху, как на примере
            var titles = new[] { "Разное", "Сотрудники", "Приказы", "Документы", "Справочники", "Окно", "Справка" };
            foreach (var t in titles)
            {
                var mi = new MenuItem { Header = t };
                MainMenu.Items.Add(mi);
            }
        }

        private void BuildReferenceMenu()
        {
            // Найдём пункт "Справочники" и заполним его
            MenuItem refs = null;
            foreach (var item in MainMenu.Items)
            {
                if (item is MenuItem mi && mi.Header.ToString() == "Справочники")
                {
                    refs = mi;
                    break;
                }
            }

            if (refs == null) return;

            refs.Items.Clear();

            var elems = _app.GetElements();
            if (elems.Count == 0)
            {
                var empty = new MenuItem { Header = "(пусто)" , IsEnabled = false };
                refs.Items.Add(empty);
                return;
            }

            foreach (var el in elems)
            {
                var child = new MenuItem { Header = el.Name, Tag = el.MethodName };
                child.Click += MenuItem_Click;
                refs.Items.Add(child);
            }
        }

        private void MenuItem_Click(object sender, RoutedEventArgs e)
        {
            if (sender is MenuItem mi)
            {
                var method = mi.Tag as string;
                if (!string.IsNullOrWhiteSpace(method))
                {
                    _app.InvokeMethod(method);
                }
                else
                {
                    MessageBox.Show($"Выбрана: {mi.Header}", "Информация", MessageBoxButton.OK, MessageBoxImage.Information);
                }
            }
        }
    }
}