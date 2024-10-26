# SysPro.TM.Library for JVM

## Building

You need to add a number of paths to your `gradle.properties` file (e.g. in `C:\Users\YourProfileName\.gradle\gradle.properties` on Windows).
See [Gradle Docs](https://docs.gradle.org/current/userguide/build_environment.html) for details on configuring build.

If you have native library files as files on your computer:

```properties
syspro.tm.library.public.win-x64=X:/SysPro.TM.Library/win-x64/SysPro.TM.Library.dll
syspro.tm.library.public.linux-x64=X:/SysPro.TM.Library/linux-x64/SysPro.TM.Library.so
syspro.tm.library.public.linux-arm64=X:/SysPro.TM.Library/linux-arm64/SysPro.TM.Library.so
```

If you have a build of `SysPro.TM.JVM.jar` that you want to reuse the native libraries from:

```properties
syspro.tm.library.public.jar=X:/SysPro.TM.JVM.jar
```
