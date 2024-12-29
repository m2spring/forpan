package org.springdot.forpan.cpanel.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springdot.forpan.cpanel.api.CPanelAPI;
import org.springdot.forpan.cpanel.api.CPanelAccessDetails;
import org.springdot.forpan.cpanel.api.CPanelDomain;
import org.springdot.forpan.cpanel.api.CPanelForwarder;
import org.springdot.forpan.cpanel.api.CPanelServerException;
import org.springdot.forpan.util.Lazy;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class CPanelImpl implements CPanelAPI{

    private final static boolean VERBOSE = true;

    private CPanelAccessDetails accessDetails;

    public CPanelImpl(CPanelAccessDetails ad){
        this.accessDetails = ad;
    }

    @Override
    public boolean isConfigured(){
        return accessDetails.isConfigured();
    }

    @Override
    public String ping() throws Exception{
        var client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
        var request = HttpRequest.newBuilder()
            .uri(URI.create(accessDetails.getEndpoint()))
            .GET()
            .build();
        HttpResponse<String> rsp = client.send(request, HttpResponse.BodyHandlers.ofString());
        return rsp.statusCode()+"\n"+rsp.body();
    }

    public static class DomainResult{
        public static class Data{
            public String main_domain;
            public String[] addon_domains;
        }
        public Data data;
    }

    @Override
    public List<CPanelDomain> getDomains(){
        HttpResponse<String> rsp = doGet("execute/DomainInfo/list_domains");
        DomainResult dr = map(rsp.body(),DomainResult.class);
        return Stream.concat(
                Stream.of(dr.data.main_domain),
                Arrays.stream(dr.data.addon_domains)
            )
            .filter(s -> !StringUtils.isBlank(s))
            .sorted()
            .map(s -> new CPanelDomain(s))
            .toList();
    }

    public static class ForwarderResult{
        public static class Data{
            public String forward; // =^= CPanelForwarder.target
            public String dest;    // =^= CPanelForwarder.forwarder

            public CPanelForwarder mkForwarder(){
                return new CPanelForwarder(dest,forward);
            }
        }
        public Data[] data;
    }

    @Override
    public List<CPanelForwarder> getForwarders(CPanelDomain domain){
        System.out.println("CPanelImpl.Forwarders: "+System.getProperty("jdk.httpclient.HttpClient.log"));
        HttpResponse<String> rsp = doGet("execute/Email/list_forwarders?domain="+domain.name());
        return Arrays.stream(map(rsp.body(),ForwarderResult.class).data)
            .map(ForwarderResult.Data::mkForwarder)
            .toList();
    }

    public static class AddForwarderResult{
        public static class Data{
            public String forward; // =^= CPanelForwarder.target
            public String domain;  // =^= CPanelForwarder.forwarderDomain
            public String email;   // =^= CPanelForwarder.forwarder

            public CPanelForwarder mkForwarder(){
                return new CPanelForwarder(forward,domain,email);
            }
        }
        public Data[] data;

        CPanelForwarder getSingleData(){
            if (data == null || data.length == 0) return null;
            if (data.length == 1) return data[0].mkForwarder();
            throw new IllegalStateException("more than 1 data elements");
        }
    }

    @Override
    public CPanelForwarder addForwarder(CPanelForwarder forwarder){
        String url = "execute/Email/add_forwarder"+
            "?domain="+forwarder.getForwarderDomain()+
            "&email="+forwarder.forwarder()+
            "&fwdopt=fwd"+
            "&fwdemail="+forwarder.target();
        return map(doGet(url).body(),AddForwarderResult.class).getSingleData();

// TODO: handle errors and warnings, e.g.
//url: execute/Email/add_forwarder?domain=example.org&email=forpan-7@example.org&fwdopt=fwd&fwdemail=xyz
//<body>
//{"errors":["(Warning: “xyz” does not refer to a valid system user. This forwarder will point to the default address.)"],"data":null,"warnings":null,"messages":null,"status":0,"metadata":{}}
//</body>
//
//url: execute/Email/add_forwarder?domain=example.org&email=forpan-7@example.org&fwdopt=fwd&fwdemail=user@example.org
//<body>
//{"metadata":{"transformed":1},"status":1,"messages":null,"warnings":null,"data":[{"email":"forpan-7@example.org","domain":"example.org","forward":"user@example.org"}],"errors":null}
//</body>
    }

    public static class DelForwarderResult{
        public String warnings;
        public String[] errors;
    }

    @Override
    public void delForwarder(CPanelForwarder forwarder){
        // delete_forwarder?address=user%40example.com&forwarder=fwdtome%40example.com
        String url = "execute/Email/delete_forwarder"+
            "?address="+forwarder.forwarder()+
            "&forwarder="+forwarder.target();
        DelForwarderResult res = map(doGet(url).body(),DelForwarderResult.class);
        if (!ArrayUtils.isEmpty(res.errors)) throw new CPanelServerException(url+"\n"+res.errors[0]);

// TODO: handle errors and warnings, e.g.
//url: execute/Email/delete_forwarder?address=forpan-10@example.org&forwarder=user@example.org'
//{"warnings":null,"data":null,"errors":null,"metadata":{},"status":1,"messages":null}

//url: execute/Email/delete_forwarder?address=notexistent@example.org&forwarder=user@example.org'
//{"warnings":null,"data":null,"errors":["Unable to locate the forwarder “notexistent@example.org” for account “notexistent@example.org” on domain “example.org”."],"metadata":{},"status":0,"messages":null}
    }

    private HttpResponse<String> doGet(String path){
        if (VERBOSE) System.out.println("url: "+path);
        var client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
        var req = HttpRequest.newBuilder()
            .uri(URI.create(accessDetails.getEndpoint()+"/"+path).normalize())
            .header("Authorization","cpanel "+accessDetails.getUser()+":"+accessDetails.getPass())
            .GET()
            .build();
        try{
            HttpResponse rsp = checkStatus(req, client.send(req, HttpResponse.BodyHandlers.ofString()));
            if (VERBOSE) System.out.println("<body>\n"+StringUtils.trim(""+rsp.body())+"\n</body>");
            return rsp;
        }catch (Exception e){
            throw new RuntimeException(e+"\npath: "+path);
        }
    }

    private HttpRequest mkReq(String path){
        return HttpRequest.newBuilder()
            .uri(URI.create(accessDetails.getEndpoint()+"/cpsess"+accessDetails.getPass()+"/"+path).normalize())
            .build();
    }

    private HttpResponse checkStatus(HttpRequest req, HttpResponse rsp){
        int sc = rsp.statusCode();
        if (sc == HttpURLConnection.HTTP_OK) return rsp;
        throw new CPanelServerException("\n"+req.method()+" "+req.uri()+"\nstatus: "+sc);
    }

    private <T> T map(String json, Class<T> type){
        try{
            return mapper.get().readValue(json,type);
        }catch (JsonProcessingException e){
            throw new RuntimeException("while mapping "+json,e);
        }
    }

    private Lazy<ObjectMapper> mapper = Lazy.of(() ->
        new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
    );
}
