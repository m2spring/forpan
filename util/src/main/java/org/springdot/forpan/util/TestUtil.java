package org.springdot.forpan.util;

public class TestUtil{

    public static void showMethod(){
        showMethod(1);
    }

    public static void showMethod(int index){
        showMethod(index+1,"");
    }

    private static void showMethod(int index, String msg){
        System.out.println("\n################# "+getMethodName(index+1)+msg);
    }

    public static String getMethodName(){
        return getMethodName(1);
    }

    public static String getMethodName(int index){
        return new Exception().getStackTrace()[index+1].getMethodName();
    }
}
