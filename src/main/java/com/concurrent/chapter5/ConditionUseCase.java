package com.concurrent.chapter5;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/9 下午11:49
 */
public class ConditionUseCase {

    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();

    public void conditionWait() throws  InterruptedException{
        lock.lock();
        try{
            condition.await();
        }finally {
            lock.unlock();
        }
    }
    public void conditionSignal() throws InterruptedException{
        lock.lock();
        try{
            condition.signal();
        }finally {
            lock.unlock();
        }
    }

}
