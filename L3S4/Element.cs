using System;

struct Element
{
    int TreeNumber;
    string Name;
    string MethodName = string.Empty;

    public  Element (int TreeNumber, string Name, String MethodName = null)
    {
        this.TreeNumber = TreeNumber;
        this.Name = Name;
        if (MethodName != null) this.MethodName = MethodName;
    }
}