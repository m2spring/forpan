package org.springdot.forpan.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Util{
    public static String helper(){
        return "helping";
    }

    public static void mkRequest(){
        try{
            System.out.println("requesting...");
            var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
            var request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.springdot.org/"))
                .build();
            HttpResponse<String> rsp = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(rsp.statusCode());
            System.out.println(rsp.body());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
