# getProperty()

## 性质

    public static String
    公开的 静态的 返回值为字符串

## 参数

* String key  
  需要获得的键

## 官方释义

>Gets the system property indicated by the specified key.
>First, if there is a security manager, its checkPropertyAccess method is called with the key as its argument. This may result in a SecurityException.
>If there is no current set of system properties, a set of system properties is first created and initialized in the same manner as for the getProperties method.

    获取指定键对应的系统属性。
    首先，如果存在安全管理器，它的checkPropertyAccess方法将以键作为参数调用。这可能会导致SecurityException。
    如果没有当前的系统属性集，则首先创建并初始化一组系统属性，其方式与getProperties方法相同。  

## 返回

* ``系统属性的值``  
  正常返回  
* ``null``  
  该键不存在系统属性中
* ``SecurityException``  
 安全管理器存在且未通过权限审查
* ``NullPointerException``  
  key值为null
* ``IllegalArgumentException``  
  key值为空

## 用法

创建一个系统上的全局属性，该属性设定后，项目下的所有类都可以通过getProperty()函数获取。

    @Test
    public void setProperty01(){
        System.out.println(
                System.setProperty("hello","你好")
        );
        System.out.println(
                System.getProperty("hello")
        );
    }

>null  
>你好

## 根本实现  

    public static String getProperty(String key) {
        checkKey(key); //检查键名合法性
        SecurityManager sm = getSecurityManager(); //获取安全管理器
        if (sm != null) { //若存在安全管理器
            sm.checkPropertyAccess(key); //检查权限
        }

        return props.getProperty(key); //获取对应键值
    }