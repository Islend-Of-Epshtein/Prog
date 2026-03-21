#nullable enable
using System;

namespace L3S4
{
    public class Element
    {
        public int TreeNumber { get; }
        public string Name { get; }
        public string? MethodName { get; }

        public Element(int treeNumber, string name, string? methodName = null)
        {
            TreeNumber = treeNumber;
            Name = name ?? string.Empty;
            MethodName = string.IsNullOrWhiteSpace(methodName) || methodName.Equals("Null", StringComparison.OrdinalIgnoreCase) ? null : methodName;
        }

        public override string ToString() => $"{TreeNumber} {Name} {MethodName}";
    }
}
#nullable restore