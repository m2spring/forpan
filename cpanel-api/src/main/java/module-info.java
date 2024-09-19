module org.springdot.forpan.cpanel.api {
    requires java.base;
    requires java.net.http;
    requires jdk.crypto.ec;
    requires org.apache.commons.lang3;
    requires commons.httpclient;
    requires com.fasterxml.jackson.databind;
    requires transitive org.springdot.forpan.util;
    exports org.springdot.forpan.cpanel.api;
    exports org.springdot.forpan.cpanel.api.impl to com.fasterxml.jackson.databind;
}
