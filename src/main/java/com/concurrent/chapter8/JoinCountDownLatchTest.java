package com.concurrent.chapter8;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/20 下午9:13
 *
 * Join方法
 *
 * join用于让当前执行线程等待join线程执行结束，其实现原理是不停检查join线程是否存活，如果join线程存活则让当前线程永远等待。
 */
public class JoinCountDownLatchTest {

    public static void main(String[] args) throws InterruptedException {
        Thread parser1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("parser1 finish");
            }
        });
        Thread parser2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("parser2 finish");
            }
        });
        parser1.start();
        parser2.start();

        parser1.join();
        parser2.join();
        System.out.println("all parser finish");
    }
}
