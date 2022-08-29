package org.synergism.test.system;

import org.junit.Test;

import java.util.Arrays;

public class arraycopyTest {

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
}
