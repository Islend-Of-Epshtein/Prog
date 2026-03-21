#nullable enable
using System;

namespace L3S4
{
    public class Element
    {
        public int TreeNumber { get; }
        public string Name { get; }
        public string? MethodName { get; }
        public int Access { get; }  // 0 - полный доступ, 1 - виден но недоступен, 2 - не виден. Неочевидно, но факт

        public Element(int treeNumber, string name, string? methodName = null, int access = 0)
        {
            TreeNumber = treeNumber;
            Name = name ?? string.Empty;
            MethodName = string.IsNullOrWhiteSpace(methodName) || methodName.Equals("Null", StringComparison.OrdinalIgnoreCase) ? null : methodName;
            Access = access;
        }

        public override string ToString() => $"{TreeNumber} {Name} {MethodName} (Access: {Access})";
    }
}
#nullable restore