# clearProperty()

## 性质

    public static String
    公开的 静态的 返回值为字符串

## 参数

* String key  
  需要改变的键

## 返回

* ``该系统属性前一个字符串值``  
  正常返回
* ``null``  
  如果该键没有属性。
* ``NullPointerException``  
  如果 key是 null 。
* ``IllegalArgumentException``  
  如果 key为空。
* ``SecurityException``  
  如果存在安全管理员，并且其 checkPropertyAccess方法不允许访问指定的系统属性。

## 官方释义

>Removes the system property indicated by the specified key.  
>First, if a security manager exists, its SecurityManager.checkPermission method is called with a PropertyPermission(key, "write") permission.
>This may result in a SecurityException being thrown. If no exception is thrown, the specified property is removed.

    该方法用于删除键指定的系统属性。
    首先，如果存在安全管理器，那么SecurityManager.checkPermission()方法就会调用PropertyPermission(key, "write")方法获取权限。
    这可能导致抛出异常。若没有抛出异常，则删除指定的属性。

## 用法
