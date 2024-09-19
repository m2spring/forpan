package org.springdot.forpan.cpanel.api;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springdot.forpan.util.Lazy;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.springdot.forpan.util.TestUtil.showMethod;

public class ApiTest{

    private static MockWebServer server;

    @BeforeClass
    public static void setUp() throws Exception{
        showMethod();
        server = new MockWebServer();
        server.start();
        server.getPort();
        System.out.println("setUp server on "+server.getPort());
    }

    @AfterClass
    public static void tearDown() throws Exception{
        showMethod();
        server.shutdown();
        server = null;
    }

    @Test
    public void testPing() throws Exception{
        showMethod();
        server.enqueue(new MockResponse().setBody("ping"));
        String rsp = api.get().ping();
        System.out.println(rsp);
        assertEquals("200\nping",rsp);
    }

    @Test
    public void testListDomains() throws Exception{
        showMethod();

        class Tst{
            Tst run(String addonDomains, String expected){
                var json = """
                    {
                     "warnings":"test warning",
                     "errors":null,
                     "data":{
                       "main_domain":"example.org",
                       "sub_domains":[
                         "sub.example.org",
                         "sub2.example.org"
                       ],
                       "parked_domains":[],
                       "addon_domains":[%s]
                     },
                     "metadata":{},
                     "messages":null,
                     "status":1
                    }
                """.formatted(addonDomains);

                server.enqueue(new MockResponse().setBody(json));
                List<CPanelDomain> domains = api.get().getDomains();
                String dmns = domains.stream().map(d -> d.name()).collect(Collectors.joining(","));
                assertEquals(expected,dmns);
                System.out.println("domains: "+dmns);
                return this;
            }
        }

        new Tst()
            .run("\"example.info\",\"other-example.org\"","example.org,example.info,other-example.org")
            .run("","example.org");
    }

    @Test
    public void testListDomainsFromServer() throws Exception{
        CPanelAPI api = getConfiguredAPI();
        if (!api.isConfigured()) return;
        showMethod();

        List<CPanelDomain> domains = api.getDomains();
        System.out.println("domains: "+domains.stream().map(d -> d.name()).collect(Collectors.joining(",")));
    }

    @Test
    public void testListForwardersFromServer() throws Exception{
        CPanelAPI api = getConfiguredAPI();
        if (!api.isConfigured()) return;
        showMethod();

        List<CPanelDomain> domains = api.getDomains();
        for (CPanelDomain dmn : domains){
            System.out.println("\n"+dmn);
            List<CPanelForwarder> fwds = api.getForwarders(dmn);
            for (CPanelForwarder fwd : fwds){
                System.out.println("  "+fwd.forwarder()+" -> "+fwd.target());
            }
        }
    }

    private Lazy<CPanelAPI> api = Lazy.of(() ->
        CPanelAPI.mkImpl(
            new CPanelAccessDetails().setEndpoint("http://localhost:"+server.getPort())
        )
    );

    private CPanelAPI getConfiguredAPI(){
        CPanelAccessDetails ad = new CPanelAccessDetails();
        if (!ad.isConfigured()){
            System.out.println(ad.getStatus());
        }
        return CPanelAPI.mkImpl(ad);
    }
}
