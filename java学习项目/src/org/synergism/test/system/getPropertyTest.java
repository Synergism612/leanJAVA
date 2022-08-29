package org.synergism.test.system;

import org.junit.Test;

public class getPropertyTest {
    @Test
    public void getProperty01(){
        System.out.println(
                System.getProperty("user.name")
        );
    }
}
