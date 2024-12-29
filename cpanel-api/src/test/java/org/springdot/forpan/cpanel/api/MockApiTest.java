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

public class MockApiTest{

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
    public void testGetDomains() throws Exception{
        showMethod();

        class Tst{
            Tst run(String addonDomains, String expected){
                server.enqueue(new MockResponse().setBody("""
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
                """.formatted(addonDomains)));

                List<CPanelDomain> domains = api.get().getDomains();
                String dmns = domains.stream().map(d -> d.name()).collect(Collectors.joining(","));
                System.out.println("expected: "+expected);
                System.out.println("dmns    : "+dmns);
                assertEquals(expected,dmns);
                return this;
            }
        }

        new Tst()
            .run("\"example.info\",\"other-example.org\"","example.info,example.org,other-example.org")
            .run("","example.org");
    }

    @Test
    public void testAddForwarder() throws Exception{
        showMethod();

        String fdmn = "fdomain.org";
        String fwdr = "faddr@"+fdmn;
        String trgt = "taddr@tdomain.org";

        server.enqueue(new MockResponse().setBody("""
            {
              "metadata":{"transformed":1},
              "messages":null,
              "status":1,
              "warnings":null,
              "errors":null,
              "data":[
                {
                  "forward":"%s",
                  "domain":"%s",
                  "email":"%s"
                }
              ]
            }
        """.formatted(trgt,fdmn,fwdr)));

        CPanelForwarder cpf = api.get().addForwarder(new CPanelForwarder(fwdr,trgt));

        assertEquals(fwdr,cpf.forwarder());
        assertEquals(trgt,cpf.target());
    }

    private Lazy<CPanelAPI> api = Lazy.of(() ->
        CPanelAPI.mkImpl(
            new CPanelAccessDetails().setEndpoint("http://localhost:"+server.getPort())
        )
    );
}
