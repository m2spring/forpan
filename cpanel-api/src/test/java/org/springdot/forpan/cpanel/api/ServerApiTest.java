package org.springdot.forpan.cpanel.api;

import org.junit.Test;
import org.springdot.forpan.util.Lazy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springdot.forpan.util.TestUtil.showMethod;

public class ServerApiTest{

    @Test
    public void testGetDomains() throws Exception{
        exec(api -> {
            List<CPanelDomain> dmns = domains.get();
            System.out.println("domains: "+dmns.stream().map(d -> d.name()).collect(Collectors.joining(",")));
        });
    }

    @Test
    public void testGetForwarders() throws Exception{
        exec(api -> {
            List<CPanelDomain> dmns = domains.get();
            for (CPanelDomain dmn : dmns){
                System.out.println("\n"+dmn);
                List<CPanelForwarder> fwds = api.getForwarders(dmn);
                for (CPanelForwarder fwd : fwds){
                    System.out.println("  "+fwd.forwarder()+" -> "+fwd.target());
                }
            }
        });
    }

    private Lazy<List<CPanelDomain>> domains = Lazy.of(() -> getConfiguredAPI().getDomains());

    @Test
    public void testAddForwarder() throws Exception{
        exec(api -> {
            List<CPanelDomain> dmns = domains.get();
            assertTrue("no configured domains",dmns.size() > 0);

            String fwdr = "forpan-"+new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date())+"@"+dmns.get(0);
            String trgt = "user@example.org";
            CPanelForwarder cpf = api.addForwarder(new CPanelForwarder(fwdr,trgt));
            assertEquals(fwdr,cpf.forwarder());
            assertEquals(trgt,cpf.target());

            testDeleteForwarder(cpf);
        });
    }

    private void testDeleteForwarder(CPanelForwarder cpf){
        exec(api -> {
            api.delForwarder(cpf);
        });
    }

    private void exec(Consumer<CPanelAPI> r){
        CPanelAPI api = getConfiguredAPI();
        if (!api.isConfigured()) return;
        showMethod(1);
        r.accept(api);
    }

    private CPanelAPI getConfiguredAPI(){
        return CPanelAPI.mkImpl(accessDetails.get());
    }

    private static Lazy<CPanelAccessDetails> accessDetails = Lazy.of(() -> {
        CPanelAccessDetails ad = new CPanelAccessDetails();
        if (!ad.isConfigured()){
            System.out.println(ad.getStatus());
        }
        return ad;
    });
}
