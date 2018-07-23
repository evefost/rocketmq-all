package org.apache.rocketmq.remoting;


import java.util.Random;

public class MyTest {

    public static void main(String[] args) {
        Random r = new Random();

        int i = Math.abs(r.nextInt() % 999) % 999;
        System.out.println(i);
    }
}
