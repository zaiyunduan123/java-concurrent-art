package com.concurrent.chapter1;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/6 下午9:17
 *
 * 并发pk串行
 */
public class ConcurrentTest {

    private static final long count = 10000L;

    public static void main(String[] args) throws InterruptedException {
        concurrency();
        serial();

    }

    private static void concurrency() throws InterruptedException {
        long start = System.currentTimeMillis();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int a = 0;
                for (long i = 0; i < count; i++) {
                    a += 5;
                }
            }
        });
        thread.start();
        int b = 0;
        for (int i = 0; i < count; i++) {
            b--;
        }
        thread.join();
        long t = System.currentTimeMillis() - start;
        System.out.println("concurrency:" + t + "ms,b=" + b);
    }

    private static void serial() {
        long start = System.currentTimeMillis();

        int a = 0;
        for (long i = 0; i < count; i++) {
            a += 5;
        }
        int b = 0;
        for (int i = 0; i < count; i++) {
            b--;
        }
        long t = System.currentTimeMillis() - start;
        System.out.println("concurrency:" + t + "ms,b=" + b + "a=" + a);
    }
}
