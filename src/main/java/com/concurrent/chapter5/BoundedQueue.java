package com.concurrent.chapter5;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/9 下午11:58
 * <p>
 * Condition使用示例
 * <p>
 * 实现一个简单的有界队列，队列为空时，队列的删除操作将会阻塞直到队列中有新的元素，队列已满时，队列的插入操作将会阻塞直到队列出现空位。
 */
public class BoundedQueue<T> {

    private Object[] items;

    //添加的下标，删除的下标和数组当前数量
    private int addIndex, removeIndex, count;

    private Lock lock = new ReentrantLock();
    private Condition notEmpty = lock.newCondition();
    private Condition notFull = lock.newCondition();

    public BoundedQueue(int size) {
        items = new Object[size];
    }

    //队列已满时，队列的插入操作将会阻塞直到队列出现空位
    public void add(T t) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length) {
                notFull.await();
            }
            items[addIndex] = t;
            if (++addIndex == items.length) {
                addIndex = 0;
            }
            ++count;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    //队列为空时，队列的删除操作将会阻塞直到队列中有新的元素
    public T remove() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            Object x = items[removeIndex];
            if (++removeIndex == items.length) {
                removeIndex = 0;
            }
            --count;
            notFull.signal();
            return (T) x;
        } finally {
            lock.unlock();
        }
    }

}
