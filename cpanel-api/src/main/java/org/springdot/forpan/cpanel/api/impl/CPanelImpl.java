package org.springdot.forpan.cpanel.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
            .map(s -> new CPanelDomain(s))
            .toList();
    }

    public static class ForwarderResult{
        public static class Data{
            public String forward;
            public String dest;

            public CPanelForwarder mkForwarder(){
                return new CPanelForwarder(dest,forward);
            }
        }
        public Data[] data;
    }

    @Override
    public List<CPanelForwarder> getForwarders(CPanelDomain domain){
        HttpResponse<String> rsp = doGet("execute/Email/list_forwarders?domain="+domain.name());
        return Arrays.stream(map(rsp.body(),ForwarderResult.class).data)
            .map(ForwarderResult.Data::mkForwarder)
            .toList();
    }

    private HttpResponse<String> doGet(String path){
        var client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
        var req = HttpRequest.newBuilder()
            .uri(URI.create(accessDetails.getEndpoint()+"/"+path).normalize())
            .header("Authorization","cpanel "+accessDetails.getUser()+":"+accessDetails.getPass())
            .GET()
            .build();
        try{
            return checkStatus(req,client.send(req,HttpResponse.BodyHandlers.ofString()));
        }catch (Exception e){
            throw new RuntimeException(e);
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
