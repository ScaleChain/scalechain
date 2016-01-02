#Introduction
We need to serialize the script operations in order to pass them to script parser and executor.
The input of the script parser is a byte array. So the serializer will write a list of ScriptOp(s) into Array[Byte].

# Input
```
List(OpIf(),
       OpNum(4),
       OpIf(),
         OpNum(8),
       OpEndIf(),
     OpEndIf())
```

#Output
```
0x63 0x54 0x63 0x58 0x68 0x68
```
