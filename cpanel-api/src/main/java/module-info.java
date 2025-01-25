module org.springdot.forpan.cpanel.api {
    requires java.net.http;
    requires jdk.crypto.ec;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.databind;
    requires org.springdot.forpan.util;
    requires org.springdot.forpan.config;
    exports org.springdot.forpan.cpanel.api;
    exports org.springdot.forpan.cpanel.api.impl to com.fasterxml.jackson.databind;
}
