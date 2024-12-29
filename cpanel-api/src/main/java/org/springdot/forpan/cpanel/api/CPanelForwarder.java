package org.springdot.forpan.cpanel.api;

public record CPanelForwarder(
    String forwarder,
    String target
){
    public CPanelForwarder(String jsonForward, String jsonDomain, String jsonEmail){
        this(validated(jsonDomain,jsonEmail),jsonForward);
    }

    public String getForwarderDomain(){
        if (forwarder != null){
            int p = forwarder.lastIndexOf('@');
            if (p > -1) return forwarder.substring(p+1);
        }
        throw new IllegalArgumentException("no forwarder domain in "+forwarder);
    }

    private static String validated(String jsonDomain, String jsonEmail){
        if (!jsonEmail.endsWith("@"+jsonDomain)){
            throw new IllegalArgumentException("email "+jsonEmail+" does not match domain "+jsonDomain);
        }
        return jsonEmail;
    }
}
