package org.springdot.forpan.cpanel.api;

public record CPanelDomain(
    String name
){
    @Override
    public String toString(){
        return name;
    }
}
