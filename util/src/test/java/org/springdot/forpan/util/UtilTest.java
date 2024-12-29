package org.springdot.forpan.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilTest{

    @Test
    public void testEscapeJava() throws Exception{
        TestUtil.showMethod();

        assertEquals("null",Util.escapeJava(null));
        assertEquals("\"\"",Util.escapeJava(""));
        assertEquals("\"a\\\"b\"",Util.escapeJava("a\"b"));
    }
}
