using System;

struct Element
{
    int TreeNumber;
    string Name;
    string MethodName;

    public  Element (int TreeNumber, string Name, String MethodName = null)
    {
        this.TreeNumber = TreeNumber;
        this.Name = Name;
        this.MethodName = MethodName;
    }
}