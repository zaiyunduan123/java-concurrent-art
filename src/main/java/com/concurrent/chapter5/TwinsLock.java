package com.concurrent.chapter5;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/9 下午9:08
 * 自定义同步器-共享锁
 * <p>
 * 在同一时刻，只允许至多两个线程同时访问，超过两个线程的访问被阻塞
 */
public class TwinsLock {

    private final Sync sync = new Sync(2);

    private static final class Sync extends AbstractQueuedSynchronizer {
        Sync(int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("count must large than zero");
            }
            setState(count);
        }

        // 自旋 + CAS
        public int tryAcquireShared(int reduceCount) {
            for (; ; ) {
                int current = getState();
                int newCount = current - reduceCount;
                if (newCount < 0 || compareAndSetState(current, newCount)) {
                    return newCount;
                }
            }
        }

        public boolean tryReleaseShare(int returnCount) {
            for (; ; ) {
                int current = getState();
                int newCount = current - returnCount;
                if (compareAndSetState(current, newCount)){
                    return true;
                }
            }
        }
    }

    public void Lock(){
        sync.tryAcquireShared(1);
    }

    public void unlock(){
        sync.tryReleaseShare(1);
    }
}
