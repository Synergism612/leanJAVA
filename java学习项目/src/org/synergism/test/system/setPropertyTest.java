package org.synergism.test.system;

import org.junit.Test;

public class setPropertyTest {
    @Test
    public void setProperty01(){
        System.out.println(
                System.setProperty("hello","你好")
        );
        System.out.println(
                System.getProperty("hello")
        );
    }
}
