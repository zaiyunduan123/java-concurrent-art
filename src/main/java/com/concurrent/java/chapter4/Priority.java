package com.concurrent.java.chapter4;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/7 上午12:13
 * <p>
 * 线程优先级
 * 优先级的范围从1到10，默认是5，优先级高的线程分配的时间片的数量要多于优先级低的线程，优先级高的线程被执行的概率高，并不是优先执行。
 */
public class Priority {
    private static volatile boolean notStart = true;
    private static volatile boolean notEnd = true;

    public static void main(String[] args) throws InterruptedException {
        ArrayList<Job> jobs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int priority = i < 5 ? Thread.MIN_PRIORITY : Thread.MAX_PRIORITY;
            Job job = new Job(priority);
            jobs.add(job);
            Thread thread = new Thread(job, "Thread:" + i);
            thread.setPriority(priority);
            thread.start();
        }
        notStart = false;
        TimeUnit.SECONDS.sleep(10);
        notEnd = false;
        for (Job job : jobs) {
            System.out.println("Job Priority:" + job.priority + ",Count:"
                    + job.jobCount);
        }
    }

    static class Job implements Runnable {

        private int priority;
        private long jobCount;

        public Job(int priority) {
            this.priority = priority;
        }

        @Override
        public void run() {
            while (notStart) {
                Thread.yield();
            }
            while (notEnd) {
                Thread.yield();
                jobCount++;
            }
        }
    }
}
