package com.concurrent.chapter8;

import java.util.concurrent.CountDownLatch;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/20 下午9:11
 */
public class CountDownLatchTest {

    static CountDownLatch c = new CountDownLatch(2);

    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(1);
                c.countDown();
                System.out.println(2);
                c.countDown();
            }
        }).start();
        System.out.println(3);
    }
}
