package org.springdot.forpan.cpanel.api;

import org.junit.Test;
import org.springdot.forpan.util.Lazy;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.springdot.forpan.util.TestUtil.showMethod;

// runs against the real, configured cPanel account - read-only calls only, see MockApiTest for write coverage
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
