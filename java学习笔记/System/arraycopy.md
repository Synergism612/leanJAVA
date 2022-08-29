# arraycopy()

## 性质

    public static native void
    公开的、静态的、本地方法、返回为空

## 参数

* Object src  
  来源数组  
* int  srcPos  
  需要复制的开始位置
* Object dest  
  去向数组
* int destPos  
  需要写入的开始位置
* int length  
  需要复制的长度

## 官方释义

>Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination array. A subsequence of array components are copied from the source array referenced by src to the destination array referenced by dest. The number of components copied is equal to the length argument. The components at positions srcPos through srcPos+length-1 in the source array are copied into positions destPos through destPos+length-1, respectively, of the destination array.

    从指定源数组中复制一个数组，复制从指定的位置开始，到目标数组的指定位置结束。从 src 引用的源数组到 dest 引用的目标数组，数组组件的一个子序列被复制下来。被复制的组件的编号等于 length 参数。源数组中位置在 srcPos 到 srcPos+length-1 之间的组件被分别复制到目标数组中的 destPos 到 destPos+length-1 位置。

## 用法

### 全部复制

    @Test
    //数组a到数组b全部复制
    public void arraycopy01(){
        //声明 a,b
        int[]a = new int[10];
        int[] b = new int[10];
        //为a赋值
        for (int i=0;i<a.length;i++) {
            a[i] = i*i;
        }
        //开始复制
        System.arraycopy(a,0,b,0,a.length);
        //输出结果
        System.out.println("a:"+Arrays.toString(a));
        System.out.println("b:"+Arrays.toString(b));
    }

>a:[0, 1, 4, 9, 16, 25, 36, 49, 64, 81]  
>b:[0, 1, 4, 9, 16, 25, 36, 49, 64, 81]  

### 数组后推两位

    @Test
    //数组后推两位
    public void arraycopy02(){
        //声明 a
        int[]a = new int[10];
        //为a赋值
        for (int i=0;i<a.length;i++) {
            a[i] = i*i;
        }
        //数组a未复制前输出
        System.out.println("a:"+Arrays.toString(a));
        //开始复制
        System.arraycopy(a,0,a,2,a.length-2);
        //数组a复制hou输出
        System.out.println("a:"+Arrays.toString(a));
    }

>a:[0, 1, 4, 9, 16, 25, 36, 49, 64, 81]  
>a:[0, 1, 0, 1, 4, 9, 16, 25, 36, 49]  

## 根本实现

[System.arraycopy函数底层源码][1]  
[System.arraycopy为什么快][15]  
[compareAndSwapInt函数底层源码案例][5]

    该方法为native，使用c/c++语言实现。

### 实现过程

* [System.c](../../../java\source\jdk\jdk-7fcf35286d52\src\share\native\java\lang\System.c)

    >静态方法数组

        static JNINativeMethod methods[] = {
        {"currentTimeMillis", "()J",              (void *)&JVM_CurrentTimeMillis},
        {"nanoTime",          "()J",              (void *)&JVM_NanoTime},
        {"arraycopy",     "(" OBJ "I" OBJ "II)V", (void *)&JVM_ArrayCopy},
        };  

* [jni.h](../../../java\source\jdk\jdk-7fcf35286d52\src\share\javavm\export\jni.h)

    >used in RegisterNatives to describe native method name, signature,and function pointer.  
    >用于描述本机方法名称、签名和函数指针。

        typedef struct {
            char *name; //名字
            char *signature; //签名
            void *fnPtr; //函数指针
        } JNINativeMethod;

* [jvm.h](../../../java\source\jdk\jdk-7fcf35286d52\src\share\javavm\export\jvm.h)

    >函数声明

        JNIEXPORT void JNICALL
        JVM_ArrayCopy(JNIEnv *env, jclass ignored, jobject src, jint src_pos,jobject dst, jint dst_pos, jint length);

* [jvm.cpp](../../../java\source\hotspot\hotspot-69087d08d473\src\share\vm\prims\jvm.cpp)

    >函数实现  
    [JNIEnv、jobject与jclass详解][6]

        JVM_ENTRY(void, JVM_ArrayCopy(JNIEnv *env, jclass ignored, jobject src, jint src_pos,jobject dst, jint dst_pos, jint length))
        JVMWrapper("JVM_ArrayCopy");
        // Check if we have null pointers
        if (src == NULL || dst == NULL) {
        THROW(vmSymbols::java_lang_NullPointerException());
        }
        arrayOop s = arrayOop(JNIHandles::resolve_non_null(src));
        arrayOop d = arrayOop(JNIHandles::resolve_non_null(dst));
        assert(s->is_oop(), "JVM_ArrayCopy: src not an oop");
        assert(d->is_oop(), "JVM_ArrayCopy: dst not an oop");
        // Do copy
        s->klass()->copy_array(s, src_pos, d, dst_pos, length, thread);
        JVM_END

    >分步解析
    1. 判空``// Check if we have null pointers``  
        * ``if (src == NULL || dst == NULL) {THROW(vmSymbols::java_lang_NullPointerException());}``  

            如果src或者dst为NULL，则返回java_lang中封装好的空指针异常。  
        * ``arrayOop s = arrayOop(JNIHandles::resolve_non_null(src));``  
            将传入的jobject转为数组基类，resolve_non_null()该函数返回oop(指针)类型  

            * ``arrayOop``  
                对应类型 ``arrayOopDesc``  
                该类型为所有数组的抽象基类，将传入的oop转为arrayOop  
                在c语言中，指针指向实际就指的是java的实例对象  
                [关于klass与oop][9]  
                [jvm底层继承关系][14]  
                这一步相当于[java中object转array][8]

                    typedef class   instanceOopDesc*            instanceOop;

                官方解释

                    // arrayOopDesc is the abstract baseclass for all arrays.  It doesn't
                    // declare pure virtual to enforce this because that would allocate a vtbl
                    // in each instance, which we don't want.
                    // The layout of array Oops is:
                    //
                    //  markOop
                    //  Klass*    // 32 bits if compressed but declared 64 in LP64.
                    //  length    // shares klass memory or allocated after declared fields.

            * ``JNIHandles::resolve_non_null(src)``  

                    inline oop JNIHandles::resolve_non_null(jobject handle) {
                    assert(handle != NULL, "JNI handle should not be null"); //断言判断不为null，msg=jni句柄不应该为空
                    oop result = resolve_impl<false /* external_guard */ >(handle); //使用resolve_impl()函数将jobject转为oop(指针)类，<false>向external_guard传递参数
                    assert(result != NULL, "NULL read from jni handle"); //再次断言检查结果后返回，msg=读取到来自jni句柄为空值
                    return result;
                    }

                * ``assert(handle != NULL, "JNI handle should not be null");``  
                    断言判断是否为空，存在很多断言方法，后续几乎每步都会进行一次断言  
                    [assert断言的用法][2]

                        //Do not assert this condition if there's already another error reported.
                        #define assert_if_no_error(cond,msg) assert((cond) || is_error_reported(), msg)
                        #else // #ifdef ASSERT
                        //p=判断，cond=状况，status=状态，msg=报错
                        #define assert(p,msg) 
                        #define assert_status(p,status,msg)
                        #define assert_if_no_error(cond,msg)
                        #endif // #ifdef ASSERT

                * ``oop result = resolve_impl<false /*external_guard*/ >(handle);``  
                将jobject类转换为oop  
                因为不排除jweak存在的可能，故而先进行一次判断是否为jweak  
                [有关弱引用(weak) java的四种引用][6]

                        // external_guard is true if called from resolve_external_guard.
                        template<bool external_guard> //获得参数，值为false
                        inline oop JNIHandles::resolve_impl(jobject handle) {
                            assert(handle != NULL, "precondition"); //断言判null，msg=先决条件
                            oop result; //声明结果
                            if (is_jweak(handle)) {       // Unlikely(不太可能的)
                            //如果是,resolve_jweak()会判断是否有值存在并处理jweak返回oop类型，<external_guard>传递参数，值为false
                                result = resolve_jweak<external_guard>(handle);
                            } else {
                            //如果不是，jobject_ref()将jobject转为oop类型
                            result = jobject_ref(handle);
                            // Construction of jobjects canonicalize a null value into a null
                            // 对象的构造规范，若值为空则成为null jobject
                            // jobject, so for non-jweak the pointee should never be null.
                            // 所以对于非jweak情况，指针永远不会为空
                            assert(external_guard || result != NULL,"Invalid value read from jni handle");
                            // 断言result!=null ，msg=这是因为jni获得的句柄为无效值
                            result = guard_value<external_guard>(result);
                            }
                            return result;
                        }
                    * [is_jweak()](../../../java\source\hotspot\hotspot-69087d08d473\src\share\vm\runtime\jniHandles.hpp)的核心语句为  
                    ``return (reinterpret_cast<uintptr_t>(handle) & weak_tag_mask) != 0;``  
                        * reinterpret_cast()为[强行转换方法][3]<uintptr_t>是要转换为的类型，[什么是uintptr_t][4]，该类型为一个无符号int类型，一般用以运算或比较指针的值。  
                        * JNIHandles提供一些用于区别jweak与jobject的底标记位

                                // Low tag bit in jobject used to distinguish a jweak.  jweak is
                                // type equivalent to jobject, but there are places where we need to
                                // be able to distinguish jweak values from other jobjects, and
                                // is_weak_global_handle is unsuitable for performance reasons.  To
                                // provide such a test we add weak_tag_value to the (aligned) byte
                                // address designated by the jobject to produce the corresponding
                                // jweak.  Accessing the value of a jobject must account for it
                                // being a possibly offset jweak.
                                static const uintptr_t weak_tag_size = 1;
                                static const uintptr_t weak_tag_alignment = (1u << weak_tag_size);
                                static const uintptr_t weak_tag_mask = weak_tag_alignment - 1;
                                static const int weak_tag_value = 1;

                        * 与底标记位做按位与，jobject的低位用来确定是否为weak，该位由weak_tag_size与weak_tag_value提供
                        * 故若按位与运算结果不为0，则是弱引用的类
                    * [resolve_jweak()](../../../java\source\hotspot\hotspot-69087d08d473\src\share\vm\runtime\jniHandles.cpp)

                            template<bool external_guard> //获得参数，值为false
                            oop JNIHandles::resolve_jweak(jweak handle) {
                            assert(is_jweak(handle), "precondition"); //断言判断是否为jweak，msg=先决条件
                            oop result = jweak_ref(handle); //使用jweak_ref()获得对应的oop
                            result = guard_value<external_guard>(result); //使用guard_value()判断是否含有值
                            #if INCLUDE_ALL_GCS
                                if (result != NULL && UseG1GC) {
                                    G1SATBCardTableModRefBS::enqueue(result);
                                    //该函数官方释义为
                                    //Add "pre_val" to a set of objects that may have been disconnected from the pre-marking object graph.
                                    //将目标添加到pre-marking对象集，该集合用以存储已断开连接的对象
                                }
                            #endif // INCLUDE_ALL_GCS
                            return result;
                            }
                * [guard_value()](../../../java\source\hotspot\hotspot-69087d08d473\src\share\vm\runtime\jniHandles.hpp)

                                // external_guard is true if called from resolve_external_guard.
                                // Treat deleted (and possibly zapped) as NULL for external_guard,
                                // else as (asserted) error.
                                template<bool external_guard> //获得参数，值为false
                                inline oop JNIHandles::guard_value(oop value) {
                                    if (!external_guard) {
                                        assert(value != badJNIHandle, "Pointing to zapped jni handle area");
                                        //断言value != badJNIHandle，msg=该句柄为疲软的、崩溃的、坏的
                                        assert(value != deleted_handle(), "Used a deleted global handle");
                                        //断言value != deleted_handle()，msg=该句柄是已经被删除的句柄
                                    } else if ((value == badJNIHandle) || (value == deleted_handle())) {
                                        value = NULL;
                                        //只有从resolve_external_guard调用时会进行这步
                                        //使用if语句判断，返回null
                                    }
                                return value;
                                }

        * ``assert(s->is_oop(), "JVM_ArrayCopy: src not an oop");``
            断言s->is_oop()，s中的每个元素是否为oop类型，msg=src不是一个oop

    2. 开始复制
        * ``s->klass()->copy_array(s, src_pos, d, dst_pos, length, thread);``  
        获取arrayOop的元类ArrayKlass，调用其中的copy_array方法，从继承关系可知，调用的函数来源于TypeArrayKlass  
        [TypeArrayKlass](../../../java\source\hotspot\hotspot-69087d08d473\src\share\vm\oops\typeArrayKlass.cpp)基础类型数组类，类函数

                void TypeArrayKlass::copy_array(arrayOop s, int src_pos, arrayOop d, int dst_pos, int length, TRAPS) {
                assert(s->is_typeArray(), "must be type array");
                //断言来源s是否为数组类型，msg=必须为数组类型

                // Check destination
                if (!d->is_typeArray() || element_type() != TypeArrayKlass::cast(d->klass())->element_type()) { 
                //用来检查去向d是否为数组
                //还记得s->klass()->copy_array(***);吗，这里调用element_type就是获取了s的子元素类型
                //s与显式2转换为TypeArrayKlass的d的子元素类型是否匹配
                    THROW(vmSymbols::java_lang_ArrayStoreException());
                    //抛出数组类型不兼容异常
                }

                // Check is all offsets and lengths are non negative
                if (src_pos < 0 || dst_pos < 0 || length < 0) {
                //检查参数与长度的合法性
                    THROW(vmSymbols::java_lang_ArrayIndexOutOfBoundsException());
                    //抛出数组索引越界异常
                }
                // Check if the ranges are valid
                if  ( (((unsigned int) length + (unsigned int) src_pos) > (unsigned int) s->length()) || (((unsigned int) length + (unsigned int) dst_pos) > (unsigned int) d->length()) ) {
                //检查src_pos+length是否大于s的长度，与检查dst_pos+length是否大于d的长度
                    THROW(vmSymbols::java_lang_ArrayIndexOutOfBoundsException());
                    //抛出数组索引越界异常
                }
                // Check zero copy
                if (length == 0)
                //若是length为0，则不需要执行复制，直接返回
                return;

                //指向数组的指针每一次前进后退一位，实际上的内存地址则会前进后退内部类型的长度

                // This is an attempt to make the copy_array fast.
                int l2es = log2_element_size();
                //获得数组的每个子元素长度
                int ihs = array_header_in_bytes() / wordSize;
                //获得以字节为单位的数组头/字长，得到每个元素前面的数组头长度，也就是偏移量
                char* src = (char*) ((oop*)s + ihs) + ((size_t)src_pos << l2es);
                //为s的第src_pos位找到内存地址
                char* dst = (char*) ((oop*)d + ihs) + ((size_t)dst_pos << l2es);
                //为d的第dst_pos位找到内存地址
                Copy::conjoint_memory_atomic(src, dst, (size_t)length << l2es); //开始拷贝
                }

            * ``element_type() != TypeArrayKlass::cast(d->klass())->element_type()``  
                TypeArrayKlass::cast(d->klass())->element_type() 将d的类型投射(显式转换为)TypeArrayKlass  
                获取TypeArrayKlass原生的子元素类型对比转换后的子元素类型

            * ``char*src = (char*) ((oop*)s + ihs) + ((size_t)src_pos << l2es);``  
                * (oop*)s + ihs)  
                    s指向一个oop的指针，若为数组则指向数组第一个元素，加上每个元素前面的数组头，就可以寻到这个数组在内存中开始的地址
                * (size_t)src_pos << l2es  
                    src_pos是一个索引，12es则是得到的数组元素固定分配的内存长度，左移内存长度等于乘内存长度
                        比如索引为5，元素长度为12，则该索引实际对应的位置是从内存开始地址的向后第5*12=60位
                * ((oop*)s + ihs) + ((size_t)src_pos << l2es)  
                    二者之和就可以得到数组s索引为src_pos的元素的内存地址

            * ``conjoint_memory_atomic(src, dst, (size_t)length << l2es)``  
                该函数的参数(size_t)length << l2es同上理

                    // Copy bytes; larger units are filled atomically if everything is aligned.
                    void Copy::conjoint_memory_atomic(void* from, void* to, size_t size) {
                        address src = (address) from;
                        //转为地址类型
                        address dst = (address) to;
                        //转为地址类型
                        uintptr_t bits = (uintptr_t) src | (uintptr_t) dst | (uintptr_t) size;
                        //目的是为了对齐
                        //三个参数按位或运算，都为0才得0
                        //举例理解 from 4 | to 8 | size 4 = 12

                        // (Note:  We could improve performance by ignoring the low bits of size, and putting a short cleanup loop after each bulk copy loop. There are plenty of other ways to make this faster also, and it's a slippery slope.  For now, let's keep this code simple since the simplicity helps clarify the atomicity semantics of this operation.  There are also CPU-specific assembly versions which may or may not want to include such optimizations.)
                        //我们可以忽略一些小的字节数并在每个批量复制循环后进行一次简短的清理循环，但这样做存在滑坡效应，一些cpu的程序集也不希望包含这种优化。现在让我们保持代码的简单性，原子性。

                        //利用得到的二进制数，对可以使用哪种复制方法做判断
                        if (bits % sizeof(jlong) == 0) {
                            //12 % 8 != 0，说明可以以long类型长度做复制
                            Copy::conjoint_jlongs_atomic((jlong*) src, (jlong*) dst, size / sizeof(jlong));
                        } else if (bits % sizeof(jint) == 0) {
                            //12 % 4 = 0，说明可以以int类型长度做复制
                            Copy::conjoint_jints_atomic((jint*) src, (jint*) dst, size / sizeof(jint));
                        } else if (bits % sizeof(jshort) == 0) {
                            //12 % 2 = 0，说明可以以short类型长度做复制
                            Copy::conjoint_jshorts_atomic((jshort*) src, (jshort*) dst, size / sizeof(jshort));
                        } else {
                            //若是都不满足，说明无法对齐，不需要保持原子性
                            // Not aligned, so no need to be atomic.
                            Copy::conjoint_jbytes((void*) src, (void*) dst, size); 
                        }
                    }

                * ``address()``  
                    该类型为内存地址，指向一个内存中地址  
                    [指针与地址][17]

                * ``sizeof()``  
                    获得变量或者类型的大小，以字节为单位  
                    [关于sizeof()与strlen()][16]  
                    一般char为1字节、short为2字节、int为4字节，long为4字节  

                * ``Copy::conjoint_jints_atomic(src, dst, count);``
                这里用int类型做例子
                调用拷贝方法，注意这里的参数是提取到开始复制位置，写入目标位置的指针

                        // jints, conjoint, atomic on each jint
                         static void conjoint_jints_atomic(jint* from, jint* to, size_t count) {
                        assert_params_ok(from, to, LogBytesPerInt);
                        pd_conjoint_jints_atomic(from, to, count);
                        }

                * ``assert_params_ok(from, to, LogBytesPerInt);``  
                    mask_bits() 函数返回两参数的按位与  
                    (uintptr_t)from/to 转换为uintptr_t类型，指针  
                    right_n_bits(n) BitsPerWord值来源于数组内存长度32，OneBit值为1  
                        ``(n >= BitsPerWord ? 0 : OneBit << (n))-1``

                        static void assert_params_ok(void* from, void* to, intptr_t log_align) {
                        #ifdef ASSERT
                        if (mask_bits((uintptr_t)from, right_n_bits(log_align)) != 0)
                        //检查from后的第log_align位是否存在
                            basic_fatal("not aligned");
                            //返回不一致错误
                        if (mask_bits((uintptr_t)to, right_n_bits(log_align)) != 0)
                        //检查to后的第log_align位是否存在
                            basic_fatal("not aligned");
                            //返回不一致错误
                        #endif
                        }

                * ``Copy::conjoint_jlongs_atomic((jlong*) src, (jlong*) dst, size / sizeof(jlong));``  

                        static void pd_conjoint_jints_atomic(jint* from, jint* to, size_t count) {
                            //考虑到内存重叠，我们需要为from，to在同一个数组情况下分别做向左和向右的复制
                            //而若是不在同一数组，那么哪种复制方法都可以
                            if (from > to) {
                                //若是from大于to，需要执行向左复制
                                while (count-- > 0) {
                                    // Copy forwards
                                    *to++ = *from++;
                                }
                            } else {
                                //若是from小于等于to，则需要执行向右复制
                                from += count - 1;
                                to   += count - 1;
                                while (count-- > 0) {
                                    // Copy backwards
                                    *to-- = *from--;
                                }
                            }
                        }

    3. 关于ObjArrayKlass中的copy_array方法  
        [ObjArrayKlass](../../../java\source\hotspot\hotspot-69087d08d473\src\share\vm\oops\objArrayKlass.cpp)普通对象数组类，对象函数  

                void ObjArrayKlass::copy_array(arrayOop s, int src_pos, arrayOop d,
                           int dst_pos, int length, TRAPS) {
                assert(s->is_objArray(), "must be obj array");
                //断言s->is_objArray()，msg=必须为array类型

                if (!d->is_objArray()) {
                    //参数d不为array则抛出类型异常
                    THROW(vmSymbols::java_lang_ArrayStoreException());
                }

                // Check is all offsets and lengths are non negative
                if (src_pos < 0 || dst_pos < 0 || length < 0) {
                    //长度参数不能小于0，否则抛出索引越界异常
                    THROW(vmSymbols::java_lang_ArrayIndexOutOfBoundsException());
                }
                // Check if the ranges are valid
                if  ( (((unsigned int) length + (unsigned int) src_pos) > (unsigned int) s->length()) || (((unsigned int) length + (unsigned int) dst_pos) > (unsigned int) d->length()) ) {
                    //参数length与src_pos/dst_pos的和不能大于src/dst的长度，否则抛出索引越界异常
                    THROW(vmSymbols::java_lang_ArrayIndexOutOfBoundsException());
                }

                // Special case. Boundary cases must be checked first
                // This allows the following call: copy_array(s, s.length(), d.length(), 0).
                // This is correct, since the position is supposed to be an 'in between point', i.e., s.length(),
                // points to the right of the last element.
                if (length==0) {
                    //如果length为0，说明不需要执行拷贝，直接返回空
                    return;
                }
                if (UseCompressedOops) { //该值为false，用以判断是oop还是narrowOop
                    narrowOop* const src = objArrayOop(s)->obj_at_addr<narrowOop>(src_pos);
                    narrowOop* const dst = objArrayOop(d)->obj_at_addr<narrowOop>(dst_pos);
                    do_copy<narrowOop>(s, src, d, dst, length, CHECK);
                } else {
                    oop* const src = objArrayOop(s)->obj_at_addr<oop>(src_pos);
                    //对s的src_pos位取地址
                    oop* const dst = objArrayOop(d)->obj_at_addr<oop>(dst_pos);
                    //对d的dst_pos位取地址
                    do_copy<oop> (s, src, d, dst, length, CHECK);
                    //执行复制函数
                }
                }

        * ``do_copy<oop> (s, src, d, dst, length, CHECK);``  

                // Either oop or narrowOop depending on UseCompressedOops.
                template <class T> void ObjArrayKlass::do_copy(arrayOop s, T* src,arrayOop d, T* dst, int length, TRAPS) {

                    BarrierSet* bs = Universe::heap()->barrier_set();
                    //Universe类存储着所有的静态方法，创建一个BarrierSet类，该类表示一个数据读写动作的屏障

                    // For performance reasons, we assume we are that the write barrier
                    //出于性能原因，假设我们写屏障
                    //we are using has optimized modes for arrays of references.  At least one
                    // of the asserts below will fail if this is not the case.
                    //若不为引用数组的优化模式，则断言不通过
                    assert(bs->has_write_ref_array_opt(), "Barrier set must have ref array opt");
                    //断言has_write_ref_array_opt()，返回值为true，msg=屏障设置必须存在一个array类型的参数
                    assert(bs->has_write_ref_array_pre_opt(), "For pre-barrier as well.");
                    //断言has_write_ref_array_pre_opt()，返回值为true，msg=对于更优的屏障也是一样的

                    if (s == d) {
                        // since source and destination are equal we do not need conversion checks.
                        //s==d说明来源数组与去向数组一致
                        assert(length > 0, "sanity check");
                        //断言length > 0，msg=检查参数合理性
                        bs->write_ref_array_pre(dst, length);
                        //调用屏障中的write_ref_array_pre操作数组入队
                        Copy::conjoint_oops_atomic(src, dst, length);
                        //进行拷贝
                    } else {
                        // We have to make sure all elements conform to the destination array
                        //若不为同一数组，则我们需要保证两数组的每一个子元素类型相同
                        Klass* bound = ObjArrayKlass::cast(d->klass())->element_klass();
                        //获取子元素类型
                        Klass* stype = ObjArrayKlass::cast(s->klass())->element_klass();
                        //获取子元素类型

                        if (stype == bound || stype->is_subtype_of(bound)) { //若是子元素类型相同，或者目标子元素是来源子元素类的子类，则可以进行拷贝
                            // elements are guaranteed to be subtypes, so no check necessary
                            bs->write_ref_array_pre(dst, length);
                            //调用屏障中的write_ref_array_pre操作数组入队
                            Copy::conjoint_oops_atomic(src, dst, length);
                            //进行拷贝
                        } else {
                            //若是不为同一类型，则我们需要手动复制
                            // slow case: need individual subtype checks
                            // note: don't use obj_at_put below because it includes a redundant store check
                            T* from = src; //获取来源起始位置
                            T* end = from + length; //得到来源的结束位置
                            for (T* p = dst; from < end; from++, p++) { //对目标起始位置循环到结束位置，每次循环都进行到两个数组的下一位
                                // XXX this is going to be slow.
                                // 该实现方法是缓慢的
                                T element = *from; //获取目前from指针对应的值
                                // even slower now
                                // 更慢了
                                bool element_is_null = oopDesc::is_null(element); //判断值是否为null
                                oop new_val = element_is_null ? oop(NULL): oopDesc::decode_heap_oop_not_null(element); //三目运算符
                                //若判空为空赋予一个null指针对象，若不为空则使用压缩指针排除隐形空值后获得指针对象
                            if (element_is_null ||(new_val->klass())->is_subtype_of(bound)) {
                                //若是判空或者新值是目标子元素类型的子类
                                bs->write_ref_field_pre(p, new_val); //入队
                                *p = element; //将值赋给目标地址
                            } else {
                                //若不为，说明值不为空且类型完全不沾边
                                // We must do a barrier to cover the partial copy.
                                // 我们必须制作一个屏障来覆盖复制
                                const size_t pd = pointer_delta(p, dst, (size_t)heapOopSize);
                                // pointer delta is scaled to number of elements (length field in
                                // objArrayOop) which we assume is 32 bit.
                                // 指针δ(pointer delta)是指按比例缩放元素的数量，他的length字段取决于objArrayOop也就是heapOopSize，我们假设为32位。
                                assert(pd == (size_t)(int)pd, "length field overflow");
                                //断言起始位置与现在位置的长度不为heapOopSize，mas=length字段溢出
                                bs->write_ref_array((HeapWord*)dst, pd); //将制作的屏障入队
                                THROW(vmSymbols::java_lang_ArrayStoreException()); //抛出数组不兼容异常
                                return; //返回空
                                }
                            }
                        }
                    }
                bs->write_ref_array((HeapWord*)dst, length);
                // 入队
                }

        * [BarrierSet详解][10]，该类用于读写

        * [屏障的作用][11]，为何设置屏障

        * ``bs->write_ref_array_pre(dst, length);``  
                该函数为[内联函数](../../../java\source\hotspot\hotspot-69087d08d473\src\share\vm\memory\barrierSet.inline.hpp)，[重载地址](../../../java\source\hotspot\hotspot-69087d08d473\src\share\vm\gc_implementation\g1\g1SATBCardTableModRefBS.hpp)
                目的是将传入的参数入队

                    // This notes that we don't need to access any BarrierSet data
                    // structures, so this can be called from a static context.
                    template <class T> static void write_ref_field_pre_static(T* field, oop newVal) {
                    T heap_oop = oopDesc::load_heap_oop(field);
                        if (!oopDesc::is_null(heap_oop)) {
                            enqueue(oopDesc::decode_heap_oop(heap_oop));
                        }
                    }

                    // We export this to make it available in cases where the static
                    // type of the barrier set is known.  Note that it is non-virtual.
                    template <class T> inline void inline_write_ref_field_pre(T* field, oop newVal) {
                        write_ref_field_pre_static(field, newVal);
                    }

        * ``enqueue(oopDesc::decode_heap_oop(heap_oop));``  
                    [入队与出队][12]

        * ``bs->write_ref_array((HeapWord*)dst, length);``  
                该函数为[内联函数](../../../java\source\hotspot\hotspot-69087d08d473\src\share\vm\memory\barrierSet.inline.hpp)  
                目的是操作数组读写

                        // count is number of array elements being written
                        void BarrierSet::write_ref_array(HeapWord* start, size_t count) {
                        assert(count <= (size_t)max_intx, "count too large");
                        HeapWord* end = (HeapWord*)((char*)start + (count*heapOopSize));
                        // In the case of compressed oops, start and end may potentially be misaligned;
                        // so we need to conservatively align the first downward (this is not
                        // strictly necessary for current uses, but a case of good hygiene and,
                        // if you will, aesthetics) and the second upward (this is essential for
                        // current uses) to a HeapWord boundary, so we mark all cards overlapping
                        // this write. If this evolves in the future to calling a
                        // logging barrier of narrow oop granularity, like the pre-barrier for G1
                        // (mentioned here merely by way of example), we will need to change this
                        // interface, so it is "exactly precise" (if i may be allowed the adverbial
                        // redundancy for emphasis) and does not include narrow oop slots not
                        // included in the original write interval.
                        HeapWord* aligned_start = (HeapWord*)align_size_down((uintptr_t)start, HeapWordSize);
                        HeapWord* aligned_end   = (HeapWord*)align_size_up  ((uintptr_t)end,   HeapWordSize);
                        // If compressed oops were not being used, these should already be aligned
                        assert(UseCompressedOops || (aligned_start == start && aligned_end == end),"Expected heap word alignment of start and end");
                        #if 0
                        warning("Post:\t" INTPTR_FORMAT "[" SIZE_FORMAT "] : [" INTPTR_FORMAT "," INTPTR_FORMAT ")\t",start,            count,              aligned_start,   aligned_end);
                        #endif
                        write_ref_array_work(MemRegion(aligned_start, aligned_end));
                        }

        * ``Copy::conjoint_oops_atomic(src, dst, length);``  
                调用拷贝方法，注意这里的参数是提取到开始复制位置，写入目标位置的指针

                    // oops,                  conjoint, atomic on each oop
                    static void conjoint_oops_atomic(oop* from, oop* to, size_t count) {
                        assert_params_ok(from, to, LogBytesPerHeapOop);
                        //检查参数是否合法函数
                        pd_conjoint_oops_atomic(from, to, count);
                        //执行复制
                    }

        * ``assert_params_ok(from, to, LogBytesPerHeapOop);``  
                    mask_bits() 函数返回两参数的按位与
                    (uintptr_t)from/to 转换为uintptr_t类型，指针
                    right_n_bits(n) BitsPerWord值来源于数组内存长度32，OneBit值为1
                        ``(n >= BitsPerWord ? 0 : OneBit << (n))-1``

                        static void assert_params_ok(void* from, void* to, intptr_t log_align) {
                        #ifdef ASSERT
                        if (mask_bits((uintptr_t)from, right_n_bits(log_align)) != 0)
                        //检查from后的第log_align位是否存在
                            basic_fatal("not aligned");
                            //返回不一致错误
                        if (mask_bits((uintptr_t)to, right_n_bits(log_align)) != 0)
                        //检查to后的第log_align位是否存在
                            basic_fatal("not aligned");
                            //返回不一致错误
                        #endif
                        }

        * ``pd_conjoint_oops_atomic(from, to, count);``  
                    该函数在[copy_windows_x86](../../../java\source\hotspot\hotspot-69087d08d473\src\os_cpu\windows_x86\vm\copy_windows_x86.inline.hpp)等各种操作系统内联类中实现，该方法是数组复制的核心方法

                        static void pd_conjoint_oops_atomic(oop* from, oop* to, size_t count) {
                            // Do better than this: inline memmove body  NEEDS CLEANUP
                            //调用这个函数有两种可能
                            //其一，from和to指向同一个数组，该情况下指针对比有意义
                            //其二，from和to指向不同数组，该情况下无论指针哪个大都无所谓
                            if (from > to) { //用以避免内存重叠
                            //若读取位置大于写入位置，则使用从左到右复制
                                while (count-- > 0) {
                                    // Copy forwards
                                    *to++ = *from++;
                                }
                            } else {
                            //若读取位置小于或等于写入位置，为避免将复制后的数据再复制，使用从右向左复制
                            from += count - 1;
                            to   += count - 1;
                            while (count-- > 0) {
                                // Copy backwards
                                *to-- = *from--;
                                }
                            }
                        }

        * ``oop new_val = element_is_null ? oop(NULL): oopDesc::decode_heap_oop_not_null(element);``  
                [decode_heap_oop_not_null()](../../../java\source\hotspot\hotspot-69087d08d473\src\share\vm\oops\oop.inline.hpp)函数内联方法  
                [压缩对象指针(narrow-oop)][13]是基于某个地址的偏移量，这个基础地址（narrow-oop-base）是由Java堆内存基址减去一个内存页的大小得来的，从而支持隐式空值检测。

                    inline oop oopDesc::decode_heap_oop_not_null(narrowOop v) {
                    assert(!is_null(v), "narrow oop value can never be zero"); //断言参数不为空
                    address base = Universe::narrow_oop_base(); //获得基底值
                    int    shift = Universe::narrow_oop_shift(); //获得偏移值
                    oop result = (oop)(void*)((uintptr_t)base + ((uintptr_t)v << shift)); //获得偏移后的指针对象
                    assert(check_obj_alignment(result), err_msg("address not aligned: " INTPTR_FORMAT, p2i((void*) result)));
                    //断言check_obj_alignment(result)，msg=地址错误
                    return result; //返回结果
                    }

                    inline bool check_obj_alignment(oop obj) {
                    return cast_from_oop<intptr_t>(obj) % MinObjAlignmentInBytes == 0;
                    //与最小队列值取余，获得基底值是否为0
                    }

[1]:https://blog.csdn.net/fu_zhongyuan/article/details/88663818
[2]:https://www.runoob.com/w3cnote/c-assert.html
[3]:https://zhuanlan.zhihu.com/p/33040213
[4]:https://cloud.tencent.com/developer/ask/sof/28262
[5]:https://blog.csdn.net/DramaLifes/article/details/124803667
[6]:https://blog.csdn.net/CV_Jason/article/details/80026265
[7]:https://blog.csdn.net/linzhiqiang0316/article/details/88591907
[8]:https://www.jianshu.com/p/daf01442455a
[9]:https://zhuanlan.zhihu.com/p/51695160
[10]:https://blog.csdn.net/qq_31865983/article/details/103667290
[11]:https://blog.csdn.net/gzxb1995/article/details/104789187
[12]:https://www.cnblogs.com/longzhongren/p/4418263.html
[13]:https://wiki.openjdk.org/display/HotSpot/
[14]:https://zhuanlan.zhihu.com/p/104725313
[15]:http://ddrv.cn/a/29253
[16]:https://blog.csdn.net/weixin_59913110/article/details/125375747
[17]:https://blog.csdn.net/qq_27370437/article/details/109560352
