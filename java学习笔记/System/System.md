# System

## 所属包

    java.long.Object  
    -->java.long.System

## 性质

    public final class
    公开的、最终的、类

## 官方解释

> The <code>System</code> class contains several useful class fields and methods. It cannot be instantiated.  

    System类包含几个有用的类字段和方法。它不能被实例化。

> <p>Among the facilities provided by the <code>System</code> class are standard input, standard output, and error output streams;access to externally defined properties and environment variables; a means of loading files and libraries; and a utility method for quickly copying a portion of an array.

    System类提供的功能包括:标准输入、标准输出和错误输出流;对外部定义的属性和环境变量的访问;加载文件和库的一种方法;以及一种用于快速复制数组一部分的实用方法。

## 字段

> staticPrintStream err

    “标准”错误输出流。

> static InputStream in  

    “标准”输入流。

> static PrintStream out  

    “标准”输出流。

## 根本实现

[转到源文件](../../source/jdk/jdk-7fcf35286d52/src/share/native/java/lang/System.c)