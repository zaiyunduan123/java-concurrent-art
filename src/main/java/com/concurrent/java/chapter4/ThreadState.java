package com.concurrent.java.chapter4;

import java.util.concurrent.TimeUnit;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/7 下午9:39
 * <p>
 * 线程的状态
 */
public class ThreadState {

    public static void main(String[] args) {

        new Thread(new TimeWaiting(), "TimeWaitingThread").start();
        new Thread(new Waiting(), "WaitingThread").start();
        //使用两个Blocked线程，一个获取锁成功，另一个被阻塞
        new Thread(new Blocked(), "BlockedThread-1").start();
        Thread thread = new Thread(new Blocked(), "BlockedThread-2");
        thread.start();
        thread.interrupt();
    }

    //该线程不断地进行睡眠
    static class TimeWaiting implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //该线程在Waiting.class实例上等待
    static class Waiting implements Runnable {

        @Override
        public void run() {
            while (true){
                synchronized (Waiting.class){
                    try {
                        Waiting.class.wait();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //该线程在Blocked.class实例上加锁后，不会释放该锁
    static class Blocked implements Runnable{
        @Override
        public void run() {
            synchronized (Blocked.class){
                while (true){
                    try {
                        TimeUnit.SECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
