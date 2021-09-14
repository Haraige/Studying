package ua.org.code;

import java.sql.Date;

public class App {
    public static void main(String[] args) {
        Integer i = 1;
        Object d = 1.2;
        System.out.println(d.getClass().getSimpleName());
        Tets tets = new Tets();
        Object obj = tets;
        System.out.println(obj.getClass());
    }
}
