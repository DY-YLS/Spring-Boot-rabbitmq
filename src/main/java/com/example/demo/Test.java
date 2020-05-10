package com.example.demo;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;

public class Test {

    public static void main(String[] args) {


        LocalDateTime now = LocalDateTime.now();
        Date date = new Date();
        System.out.println(now);
        System.out.println(date.getTime());

        System.out.println(StringUtils.isEmpty("  "));


        System.out.println(test());
    }

    public static int test() {
        try {
            int a = 1 / 0;
            return test1(1);
        } catch (Exception e) {

            return test1(2);
        } finally {
            return 3;
        }

    }

    public static int test1(int i) {
        System.out.println(i);
        return i;
    }
}
