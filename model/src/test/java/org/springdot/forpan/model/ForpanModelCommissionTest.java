package org.springdot.forpan.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springdot.forpan.model.RecordState.COMMISSIONED;
import static org.springdot.forpan.model.RecordState.DECOMMISSIONED;
import static org.springdot.forpan.util.TestUtil.getMethodName;
import static org.springdot.forpan.util.TestUtil.showMethod;

public class ForpanModelCommissionTest extends ModelTestBase{

    @Test
    public void testCommissionForwarderRoundTripsThroughDummyModelSource() throws Exception{
        showMethod();
        setForpanHome(getMethodName());

        DummyModelSource dummySource = new DummyModelSource();
        ForpanModel model = new ForpanModel(dummySource);

        FwRecord rec = new FwRecord("fwdr@example.org","trgt@example.org");
        rec.setStates(new ArrayList<>(List.of(new RecordStateEntry(new Date(),COMMISSIONED))));
        model.addForwarder(rec);
        dummySource.createForwarder(rec);

        model.decommissionForwarder(rec);
        assertFalse("dummy server should no longer have the forwarder",dummySource.readRecords().contains(rec));
        assertEquals(DECOMMISSIONED,rec.getLastState());

        model.commissionForwarder(rec);
        assertTrue("dummy server should have the forwarder again",dummySource.readRecords().contains(rec));
        assertEquals(COMMISSIONED,rec.getLastState());
        assertTrue("record should stay in the model",model.getRecords().contains(rec));

        ForpanModel reloaded = ForpanModel.load("");
        FwRecord reloadedRec = reloaded.findForwarder("fwdr@example.org");
        assertEquals(COMMISSIONED,reloadedRec.getLastState());
    }
}
