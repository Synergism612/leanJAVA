# setProperty()

## 性质

    public static String
    公开的 静态的 返回值为字符串

## 参数

* String key  
  属性键名

* String value
  属性键值

## 官方释义

>Sets the system property indicated by the specified key.  
>First, if a security manager exists, its SecurityManager.checkPermission method is called with a PropertyPermission(key, "write") permission.  
>This may result in a SecurityException being thrown.  
>If no exception is thrown, the specified property is set to the given value.  

    该方法用于设置由指定键指示的系统属性。
    首先，如果存在安全管理器，那么SecurityManager.checkPermission()方法就会调用PropertyPermission(key, "write")方法获取权限。
    这可能导致抛出异常。若没有抛出异常，则删除指定的属性。  
  
## 返回

* ``系统属性上一次的字符串值``
  正常返回
* ``null``
  如果没有以前的值
* ``SecurityException``  
  如果安全管理器存在并且其
* ``checkPropertyAccess``  
  方法不允许访问指定的系统属性。
* ``NullPointerException``  
  如果 key 为 null。
* ``IllegalArgumentException``  
  如果 key 为空。  

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

    public static String setProperty(String key, String value) {
        checkKey(key);
        //检查key值
        SecurityManager sm = getSecurityManager();
        //获取安全管理器
        if (sm != null) {
          //若存在安全管理器
            sm.checkPermission(new PropertyPermission(key,
                SecurityConstants.PROPERTY_WRITE_ACTION));
                //检查许可，参数为key与操作手段 write
        }
        return (String) props.setProperty(key, value); 
        //调用小工具里的set函数来创建
    }

* ``checkKey(key);``  
    该函数用以检查key的合法性

      private static void checkKey(String key) {
        if (key == null) {
          //若key值为null，抛出异常，msg=key值不能为null
            throw new NullPointerException("key can't be null");
        }
        if (key.equals("")) {
          //若key值为字符串空值，抛出异常，msg=key值不能为空
            throw new IllegalArgumentException("key can't be empty");
        }
    }

* ``getSecurityManager()``
    该函数将会返回安全管理器，若不存在则返回null

      public static SecurityManager getSecurityManager() {
          return security;
      }

* ``sm.checkPermission(new PropertyPermission(key,SecurityConstants.PROPERTY_WRITE_ACTION));``  
  该函数将会检查许可，是否可以对key对应属性进行操作  
  SecurityConstants是一个常量库  
  PROPERTY_WRITE_ACTION的值为"write"

      public void checkPermission(Permission perm) {
        java.security.AccessController.checkPermission(perm);
      }  
  
  * ``new PropertyPermission(key,SecurityConstants.PROPERTY_WRITE_ACTION)``  
    * ``PropertyPermission``  
      构造函数如下

          public PropertyPermission(String name, String actions) {
          super(name,actions); //将参数赋值到私有变量
          init(getMask(actions)); //调用初始化方法
          }

      * ``getMask(actions)``  
        该函数用于将原本为字符串的actions转为对应的16进制数

            private static int getMask(String actions) {

                int mask = NONE; //重置mask值为空的数值化值

                if (actions == null) {
                    return mask; //若没有操作则直接返回数值化后的空
                }

                // Use object identity comparison against known-interned strings for
                // performance benefit (these values are used heavily within the JDK).
                if (actions == SecurityConstants.PROPERTY_READ_ACTION) { //若操作为read
                    return READ; //返回数值化后的读取
                } if (actions == SecurityConstants.PROPERTY_WRITE_ACTION) { //若操作为write
                    return WRITE; //返回数值化后的写入
                } else if (actions == SecurityConstants.PROPERTY_RW_ACTION) { //若操作为read,write
                    return READ|WRITE; //返回数值化后的读写
                }

                //以上if都没有通过

                char[] a = actions.toCharArray(); //将actions字符串转为字符数组

                int i = a.length - 1; //得到长度减1的值

                if (i < 0) //若i值为-1，说明a.length为0，长度判空
                    return mask; //返回数值化后的空

                while (i != -1) { //开启循环
                    char c; //声明字符变量c

                    // skip whitespace
                    //筛选掉空格，换行，缩进
                    while ((i!=-1) && ((c = a[i]) == ' ' ||
                            c == '\r' ||
                            c == '\n' ||
                            c == '\f' ||
                            c == '\t'))
                        i--;

                    // check for the known strings
                    //筛选是否存在已知字符
                    //一定要注意，还有一种可能是两个都存在，比如"readwrite"
                    //ALL的值为READ和WRITE的按位或
                    int matchlen; //声明一个变量，存放长度

                    if (i >= 3 && (a[i-3] == 'r' || a[i-3] == 'R') &&
                            (a[i-2] == 'e' || a[i-2] == 'E') &&
                            (a[i-1] == 'a' || a[i-1] == 'A') &&
                            (a[i] == 'd' || a[i] == 'D'))
                    //监测到read或者READ
                    {
                        matchlen = 4; //赋值
                        mask |= READ; //按位或 赋予mask 按位或保证是否为ALL

                    } else if (i >= 4 && (a[i-4] == 'w' || a[i-4] == 'W') &&
                            (a[i-3] == 'r' || a[i-3] == 'R') &&
                            (a[i-2] == 'i' || a[i-2] == 'I') &&
                            (a[i-1] == 't' || a[i-1] == 'T') &&
                            (a[i] == 'e' || a[i] == 'E'))
                    //监测到write或者WRITE
                    {
                        matchlen = 5; //赋值
                        mask |= WRITE; //按位或 赋予mask 按位或保证是否为ALL

                    } else {
                        // parse error
                        //抛出解析错误异常
                        throw new IllegalArgumentException(
                                "invalid permission: " + actions);
                        //无效的许可：***
                    }

                    // make sure we didn't just match the tail of a word
                    // like "ackbarfaccept".  Also, skip to the comma.
                    //现在知道末尾存在read或writh的大或小写，需要检查前面的字符
                    boolean seencomma = false; //声明布尔类变量
                    while (i >= matchlen && !seencomma) { //循环检查之前的每个字符
                        switch(a[i-matchlen]) {
                            case ',':
                                seencomma = true; //更改循环条件值，停止循环
                                break; //若为逗号说明这段结束，跳出循环
                            case ' ': case '\r': case '\n':
                            case '\f': case '\t':
                                break; //若存在以上符号，说明需要继续向前判断
                            default:
                                throw new IllegalArgumentException(
                                        "invalid permission: " + actions);
                                //出现了其它的字符说明参数不合法，抛出解析错误异常
                        }
                        i--; //自减，循环条件
                    }

                    // point i at the location of the comma minus one (or -1).
                    i -= matchlen; //减去获得的字符长度，循环直到i=-1
                }
                return mask; //返回mask值
            }

      * ``init(getMask(actions));``  

            private void init(int mask) {
                //初始化方法
                if ((mask & ALL) != mask) //按位与，0&3 = 0，1&3 = 1，2&3 = 2，3&3 = 3
                    // 故若不等于抛出异常
                    throw new IllegalArgumentException("invalid actions mask");

                if (mask == NONE) //mask等于0
                    //抛出异常
                    throw new IllegalArgumentException("invalid actions mask");

                //两次if足以说明mask的值合法
                if (getName() == null) //获取名字
                    //否则抛出异常
                    throw new NullPointerException("name can't be null");

                this.mask = mask; //将mask赋值
            }

  * ``java.security.AccessController.checkPermission(perm);``  
    * ``security``  

          Provides the classes and interfaces for the security framework. This includes classes that implement an easily configurable, fine-grained access control security architecture. This package also supports the generation and storage of cryptographic public key pairs, as well as a number of exportable cryptographic operations including those for message digest and signature generation. Finally, this package provides classes that support signed/guarded objects and secure random number generation. Many of the classes provided in this package (the cryptographic and secure random number generator classes in particular) are provider-based. The class itself defines a programming interface to which applications may write. The implementations themselves may then be written by independent third-party vendors and plugged in seamlessly as needed. Therefore application developers may take advantage of any number of provider-based implementations without having to add or rewrite code.

          提供安全框架的类和接口。这包括实现容易配置的细粒度访问控制安全体系结构的类。此包还支持加密公钥对的生成和存储，以及许多可导出的加密操作，包括消息摘要和签名生成操作。最后，这个包提供了支持签名/保护对象和安全随机数生成的类。此包中提供的许多类(特别是加密和安全随机数生成器类)都是基于提供程序的。类本身定义了应用程序可以向其编写的编程接口。然后，实现本身可以由独立的第三方供应商编写，并根据需要无缝插入。因此，应用程序开发人员可以利用任何数量的基于提供者的实现，而不必添加或重写代码。  

    * ``AccessController``  

          The AccessController class is used for access control operations and decisions.
          More specifically, the AccessController class is used for three purposes:
          1.to decide whether an access to a critical system resource is to be allowed or denied, based on the security policy currently in effect,
          2.to mark code as being "privileged", thus affecting subsequent access determinations, and
          3.to obtain a "snapshot" of the current calling context so access-control decisions from a different context can be made with respect to the saved context.

          AccessController 类用于与访问控制相关的操作和决定。
          更确切地说，AccessController 类用于以下三个目的：
          1.基于当前生效的安全策略决定是允许还是拒绝对关键系统资源的访问
          2.将代码标记为享有“特权”，从而影响后续访问决定，以及
          3.获取当前调用上下文的“快照”，这样便可以相对于已保存的上下文作出其他上下文的访问控制决定。  

      * ``AccessController.checkPermission(perm);``  
        基于当前 AccessControlContext 和安全策略确定是否允许指定权限所指示的访问请求。  
        允许则默默返回。  
        不允许则抛出 SecurityException。

  * ``(String) props.setProperty(key, value);``  
    该函数是核心函数  
    synchronized 关键字，线程上唯一，检查线程是否存在调用该函数，若是调用则需要等待该方法处理结束

        public synchronized Object setProperty(String key, String value) {
          return put(key, value);
        }

    * ``put(key, value);``  
        该函数使用哈希方法进行put  
        [哈希表原理分析][1]

            public synchronized V put(K key, V value) {
                // Make sure the value is not null
                //判空
                if (value == null) {
                    throw new NullPointerException();
                }

                // Makes sure the key is not already in the hashtable.
                //确保键不存在于哈希表中
                Entry<?,?> tab[] = table; //获取目前所有的键值对
                int hash = key.hashCode(); //获取key对应的哈希码
                int index = (hash & 0x7FFFFFFF) % tab.length; //哈希槽定位，获得插入位置
                //关闭未信任约束的警告
                @SuppressWarnings("unchecked")
                Entry<K,V> entry = (Entry<K,V>)tab[index]; //定位到指定哈希表的指定位置
                for(; entry != null ; entry = entry.next) { //for遍历哈希表
                    if ((entry.hash == hash) && entry.key.equals(key)) { //如果键存在于哈希表中。哈希值相等且字符相等
                        V old = entry.value; //将旧值保存到old
                        entry.value = value; //写入新的值
                        return old; //返回旧值
                    }
                }
                // 若是表中不存在键值，说明要添加新的键值对
                addEntry(hash, key, value, index); //调用添加方法
                return null; //返回空值
            }

      * ``@SuppressWarnings``  
        [@SuppressWarnings注解详情][0]  
        用于关闭某个异常的报错信息

      * ``addEntry(hash, key, value, index);``  
          若原本表中不存在键名，则需要新添加进去

            private void addEntry(int hash, K key, V value, int index) {
                modCount++; //用于计数哈希表的修改次数

                Entry<?,?> tab[] = table; //获取全部键值对
                if (count >= threshold) { //count 哈希表的总条目数，threshold 记录哈希表的大小阈值
                    // Rehash the table if the threshold is exceeded
                    //超过阈值，重新散列该表，也就是扩大大小并重组哈希表
                    rehash(); //进行重组哈希表

                    tab = table; //获取重组后的哈希表
                    hash = key.hashCode(); //获得键名的哈希值
                    index = (hash & 0x7FFFFFFF) % tab.length; //哈希槽定位，获取位置
                }

                // Creates the new entry.
                @SuppressWarnings("unchecked")
                Entry<K,V> e = (Entry<K,V>) tab[index]; //获取原本位置的值
                tab[index] = new Entry<>(hash, key, value, e); //构建新的对象
                count++; //哈希表总条目数加一
            }

        * ``modCount``  
          [transient关键字作用][2]
          该关键字所修饰的变量或方法不会被序列化存储到硬盘中，生命周期只为调用者的缓存中。  
          注释中提到``ConcurrentModificationException``异常，该异常会在不同线程对同一哈希表进行操作或改变内部结构时、或在哈希表自身迭代中对自身进行操作或改变内部结构时抛出，变量modCount是该异常的重要依据  

              /**
              * The number of times this Hashtable has been structurally modified
              * Structural modifications are those that change the number of entries in
              * the Hashtable or otherwise modify its internal structure (e.g.,
              * rehash).  This field is used to make iterators on Collection-views of
              * the Hashtable fail-fast.  (See ConcurrentModificationException).
              */
              private transient int modCount = 0; //哈希表操作次数



[0]:https://cloud.tencent.com/developer/article/1353329
[1]:https://www.jianshu.com/p/5a2a5f6de440
[2]:https://www.runoob.com/w3cnote/java-transient-keywords.html