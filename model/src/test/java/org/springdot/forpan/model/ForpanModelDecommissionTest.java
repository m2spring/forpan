package org.springdot.forpan.model;

import org.junit.Test;
import org.springdot.forpan.cpanel.api.CPanelDomain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springdot.forpan.model.RecordState.COMMISSIONED;
import static org.springdot.forpan.model.RecordState.DECOMMISSIONED;
import static org.springdot.forpan.util.TestUtil.getMethodName;
import static org.springdot.forpan.util.TestUtil.showMethod;

public class ForpanModelDecommissionTest extends ModelTestBase{

    @Test
    public void testDecommissionForwarder() throws Exception{
        showMethod();
        setForpanHome(getMethodName());

        List<FwRecord> removed = new ArrayList<>();
        ForpanModel model = new ForpanModel(new ModelSource(){
            @Override public List<CPanelDomain> readDomains(){ return List.of(); }
            @Override public List<FwRecord> readRecords(){ return List.of(); }
            @Override public void createForwarder(FwRecord rec){ throw new UnsupportedOperationException(); }
            @Override public void removeForwarder(FwRecord rec){ removed.add(rec); }
        });

        FwRecord rec = new FwRecord("fwdr@example.org","trgt@example.org");
        rec.setStates(new ArrayList<>(List.of(new RecordStateEntry(new Date(),COMMISSIONED))));
        model.addForwarder(rec);

        model.decommissionForwarder(rec);

        assertEquals(List.of(rec),removed);
        assertTrue("record should stay in the model, not be removed",model.getRecords().contains(rec));
        assertEquals(DECOMMISSIONED,rec.getLastState());

        ForpanModel reloaded = ForpanModel.load("");
        FwRecord reloadedRec = reloaded.findForwarder("fwdr@example.org");
        assertEquals(DECOMMISSIONED,reloadedRec.getLastState());
    }
}
