module org.springdot.forpan.gui {
    exports org.springdot.forpan.gui;
    requires java.net.http;
    requires jdk.crypto.ec;
    requires javafx.controls;
    requires atlantafx.base;
    requires org.springdot.forpan.core;
    requires org.springdot.forpan.cpanel.api;
    requires org.springdot.forpan.model;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.lang3;
}
