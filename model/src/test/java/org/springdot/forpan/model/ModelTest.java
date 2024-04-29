package org.springdot.forpan.model;

import org.junit.Test;

import static org.springdot.forpan.util.TestUtil.showMethod;

public class ModelTest{

    @Test
    public void testReload() throws Exception{
        showMethod();

        FwModel model = FwModel.instance.get();
        model.reload();
    }
}
