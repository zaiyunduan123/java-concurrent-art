package com.concurrent.java.chapter3;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/6 下午11:49
 *
 * 锁内存
 */
public class ReentrantLockExample {

    int a = 0;
    ReentrantLock lock = new ReentrantLock();

    public void writer() {
        lock.lock(); //获取锁
        try {
            a++;
        } finally {
            lock.unlock();//释放锁
        }
    }

    public void reader() {
        lock.lock(); //获取锁
        try {
            int i = a;
        } finally {
            lock.unlock();//释放锁
        }
    }
}
