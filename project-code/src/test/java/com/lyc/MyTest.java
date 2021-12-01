package com.lyc;

import com.lyc.utils.MD5Util;
import org.junit.Test;

public class MyTest {
    @Test
    public void testMD5(){
        String mi = MD5Util.getMD5("000000");
        System.out.println(mi);
    }

    @Test
    public void test(){
        String[] id = {"abc","def","ghi"};
        String []ic = {"a,b,c"};
        System.out.println("id长度="+id.length);
        System.out.println("ic长度="+ic.length);
    }
}
