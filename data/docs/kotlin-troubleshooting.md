
## IntelliJ - “Searching for Compilable Files” when running/building Kotlin files
Turning off 'Keep compiler process alive' option solves the problem.
https://discuss.kotlinlang.org/t/kotlin-build-error/1560/6

```
Could you please check daemon status, is it on? (Settings | Build,... | Compiler | Kotlin Compiler | Keep compiler process alive...)
If so, could you please try with the disabled daemon.
```

