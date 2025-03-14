package org.springdot.forpan.model;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springdot.forpan.cpanel.api.CPanelAccessDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.springdot.forpan.config.Formats.FORMAT_YMDHMSM;
import static org.springdot.forpan.util.TestUtil.getMethodName;
import static org.springdot.forpan.util.TestUtil.showMethod;
import static org.springdot.forpan.util.Util.escapeJava;

public class ModelTest extends ModelTestBase{

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
    public void testSyncAndSave() throws Exception{
        showMethod();

        ForpanModel model = mkModel(getMethodName());

        FwRecord R0 = new FwRecord("fwdr0@example.org","trgt0@example.org");
        FwRecord R1 = new FwRecord("fwdr1@example.org","trgt1@example.org");
        FwRecord R1a = new FwRecord("fwdr1@example.org","othr1@example.org");
        FwRecord R1b = new FwRecord("fwdr1@example.org","othr2@example.org");
        FwRecord R1c = new FwRecord("fwdr1@example.org","othr3@example.org");
        FwRecord[] recs = new FwRecord[]{R0,R1,R1a,R1b,R1c};

        List<Date> times = new ArrayList<>();

        class Tst{
            Tst testWith(FwRecord... recs){
                System.out.println("-----------");
                enqueueMockForwardersResponse(recs);
                times.add(model.syncFromServer());
                print(model);
                model.save();
                return this;
            }
            Tst expect(String expSpecs){
                final var indent = "    ";
                for (String expSpec : expSpecs.split(" ")){
                    var rec = model.findForwarder(recs[expSpec.charAt(0)-48].forwarder);

                    String actual = rec.getStates().stream()
                        .map(rse -> indent+FORMAT_YMDHMSM.format(rse.time)+" "+rse.state.toString().substring(0,3))
                        .collect(Collectors.joining("\n"));

                    String expected = Arrays.stream(expSpec.substring(2).split(","))
                        .map(s -> indent+FORMAT_YMDHMSM.format(times.get(s.charAt(0)-48))+" "+s.substring(1))
                        .collect(Collectors.joining("\n"));

                    assertEquals(expected,actual);
                }
                return this;
            }
        }


        new Tst()
            .testWith(R0)   .expect("0:0COM")
            .testWith(R0)   .expect("0:0COM,1COM")
            .testWith(R0)   .expect("0:0COM,2COM")
            .testWith(R0,R1).expect("0:0COM,3COM 1:3COM")
            .testWith(R1)   .expect("0:0COM,3COM,4DEC 1:3COM,4COM")
            .testWith(R1)   .expect("0:0COM,3COM,4DEC,5DEC 1:3COM,5COM")
            .testWith(R1)   .expect("0:0COM,3COM,4DEC,6DEC 1:3COM,6COM")

            .testWith(R1a)  .expect("1:3COM,6COM,7CHA")
            .testWith(R1b)  .expect("1:3COM,6COM,7CHA,8CHA")
            .testWith(R1c)  .expect("1:3COM,6COM,7CHA,8CHA,9CHA")
        ;
    }

    /**
     * cPanel allows to have the very same forwarder more than once with different targets.
     * Because Forpan relies on using the forwarder as a key, it has to deal with such
     * situation in a lenient way.
     */
    @Test
    public void testDuplicates() throws Exception{
        showMethod();

        ForpanModel model = mkModel(getMethodName());

        String firstTarget = "trgt0@example.org";

        enqueueMockForwardersResponse(
            new FwRecord("fwdr@example.org",firstTarget),
            new FwRecord("fwdr@example.org","trgt1@example.org")
        );

        model.syncFromServer();
        print(model);

        List<FwRecord> recs = model.getRecords();
        assertEquals(1,recs.size());
        assertEquals(firstTarget,recs.getFirst().target);
    }

    private ForpanModel mkModel(String method){
        setForpanHome(method);

        RemoteModelSource msrc = new RemoteModelSource(new CPanelAccessDetails("http://localhost:"+server.getPort()));

        enqueueMockDomainsResponse();

        return new ForpanModel(msrc);
    }

    private void enqueueMockDomainsResponse(){
        server.enqueue(new MockResponse().setBody("""
            {
              "warnings":"test warning",
              "errors":null,
              "data":{
                "main_domain":"example.org",
                "sub_domains":[],
                "parked_domains":[],
                "addon_domains":[]
              },
              "metadata":{},
              "messages":null,
              "status":1
            }
            """));
    }

    private void enqueueMockForwardersResponse(FwRecord... recs){
        try{
            Thread.sleep(1); // to ensure that new time stamp is at least one millisecond later
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
        server.enqueue(new MockResponse().setBody("""
            {
              "metadata": {
                "transformed": 1
              },
              "status": 1,
              "messages": null,
              "warnings": null,
              "data": [%s
              ],
              "errors": null
            }
            """.formatted(
            Arrays.stream(recs)
                .map(rec -> "\n    {\"forward\":"+escapeJava(rec.getTarget())+", \"dest\": "+escapeJava(rec.getForwarder())+"}")
                .collect(Collectors.joining(","))
        )));
    }

    private void print(ForpanModel model){
        model.getRecords().stream().forEach(rec -> {
            System.out.println("* "+rec.getForwarder()+" -> "+rec.getTarget());
            for (RecordStateEntry rse : rec.getStates()){
                System.out.println("  "+rse);
            }
        });
    }
}
