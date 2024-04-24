package org.springdot.forpan.cli;

import org.springdot.forpan.core.Util;

public class App{
    public static void main(String[] args){
        System.out.println("hello multi modules: "+Util.helper());
        Util.mkRequest();
    }
}
