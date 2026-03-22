#nullable enable
namespace L3S4;

public class Element(int treeNumber, string name, string? methodName = null, int access = 0)
{
    public int TreeNumber { get; } = treeNumber;
    public string Name { get; } = name ?? string.Empty;
    public string? MethodName { get; } = string.IsNullOrWhiteSpace(methodName) || methodName == "Null" ? null : methodName;
    public int Access { get; } = access;

    public override string ToString() => $"{TreeNumber} {Name} {MethodName} (Access: {Access})";
}