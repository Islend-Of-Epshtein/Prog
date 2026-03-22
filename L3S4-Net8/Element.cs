using System;

namespace L3S4
{
    public class Element
    {
        public int TreeNumber { get; }
        public string Name { get; }
        public string MethodName { get; }
        public int Access { get; }
        public Element(int treeNumber, string name,int access=0,  string methodName = "")
        {
            TreeNumber = treeNumber;
            Name = name ?? string.Empty;
            Access = access;
            MethodName = methodName;
                        
        }

        public override string ToString()
        {
            return $"{TreeNumber} {Name} {Access} {MethodName}";
        }
    }
}   