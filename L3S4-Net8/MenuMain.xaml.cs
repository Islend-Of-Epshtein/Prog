using L3S4;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Windows;
using System.Windows.Controls;

namespace L3S4;

public partial class MenuMain : Window
{
    private static readonly Type[] EmptyTypes = Type.EmptyTypes;
    private readonly App0 _app;

    public MenuMain(App0 app)
    {
        _app = app;
        InitializeComponent();

        Loaded += MenuMain_Loaded;
        _app.ElementsUpdated += App_ElementsUpdated;
    }

    private void MenuMain_Loaded(object sender, RoutedEventArgs e) => BuildMenuFromElements();

    private void App_ElementsUpdated(object? sender, EventArgs e) => BuildMenuFromElements();

    protected override void OnClosed(EventArgs e)
    {
        _app.ElementsUpdated -= App_ElementsUpdated;
        base.OnClosed(e);
    }

    private void BuildMenuFromElements()
    {
        MainMenu.Items.Clear();

        List<Element> all = _app
            .GetElements()
            .Where(x => x.Access != 2)
            .ToList();

        if (all.Count == 0)
        {
            HeaderText.Text = "Нет доступных пунктов меню.";
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

            MenuItem item = BuildMenuItemRecursive(all, ref index, 0);
            MainMenu.Items.Add(item);
        }

        HeaderText.Text = "Выбери раздел меню";
    }

    private MenuItem BuildMenuItemRecursive(List<Element> all, ref int index, int currentLevel)
    {
        Element current = all[index];
        var item = new MenuItem
        {
            Header = current.Name,
            Tag = current,
            IsEnabled = current.Access == 0
        };

        index++;

        if (current.Access != 0)
        {
            while (index < all.Count && all[index].TreeNumber > currentLevel)
                index++;
            return item;
        }

        while (index < all.Count)
        {
            int nextLevel = all[index].TreeNumber;
            if (nextLevel <= currentLevel) break;

            if (nextLevel == currentLevel + 1)
            {
                MenuItem child = BuildMenuItemRecursive(all, ref index, currentLevel + 1);
                item.Items.Add(child);
            }
            else
            {
                index++;
            }
        }

        if (item.Items.Count == 0)
            item.Click += FinalItem_Click;

        return item;
    }

    private void FinalItem_Click(object sender, RoutedEventArgs e)
    {
        if (sender is not MenuItem { Tag: Element element })
            return;

        HeaderText.Text = $"Выбран пункт: {element.Name}";

        if (HasMethod(element.MethodName))
            _app.InvokeMethod(element.MethodName);

        e.Handled = true;
    }

    private bool HasMethod(string? methodName) =>
        !string.IsNullOrWhiteSpace(methodName) &&
        _app.GetType().GetMethod(methodName, BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic, null, EmptyTypes, null) != null;
}