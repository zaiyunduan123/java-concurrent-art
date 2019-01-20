<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [8、Java中的并发工具类](#8java%E4%B8%AD%E7%9A%84%E5%B9%B6%E5%8F%91%E5%B7%A5%E5%85%B7%E7%B1%BB)
  - [等待多线程完成的CountDownLatch](#%E7%AD%89%E5%BE%85%E5%A4%9A%E7%BA%BF%E7%A8%8B%E5%AE%8C%E6%88%90%E7%9A%84countdownlatch)
    - [用法](#%E7%94%A8%E6%B3%95)
    - [原理](#%E5%8E%9F%E7%90%86)
      - [初始化](#%E5%88%9D%E5%A7%8B%E5%8C%96)
      - [获得锁](#%E8%8E%B7%E5%BE%97%E9%94%81)
      - [释放锁](#%E9%87%8A%E6%94%BE%E9%94%81)
      - [总结](#%E6%80%BB%E7%BB%93)
  - [同步屏障CyclicBarrier](#%E5%90%8C%E6%AD%A5%E5%B1%8F%E9%9A%9Ccyclicbarrier)
    - [原理](#%E5%8E%9F%E7%90%86-1)
    - [CyclicBarrier和CountDownLatch的区别](#cyclicbarrier%E5%92%8Ccountdownlatch%E7%9A%84%E5%8C%BA%E5%88%AB)
  - [控制并发线程数的Semaphore](#%E6%8E%A7%E5%88%B6%E5%B9%B6%E5%8F%91%E7%BA%BF%E7%A8%8B%E6%95%B0%E7%9A%84semaphore)
  - [线程间交换数据的Exchanger](#%E7%BA%BF%E7%A8%8B%E9%97%B4%E4%BA%A4%E6%8D%A2%E6%95%B0%E6%8D%AE%E7%9A%84exchanger)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# 8、Java中的并发工具类

## 等待多线程完成的CountDownLatch
CountDownLatch这个类，作用感觉和join很像，首先来看一下join，join用于让当前执行线程等待join线程执行结束，其实现原理是不停检查join线程是否存活，如果join线程存活则让当前线程永远等待。

CountDownLatch是一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。 

### 用法 
CountDownLatch的构造函数接收一个int类型的参数作为计数器，如果你想等待N个点完成，这里就传入N。

当我们调用CountDownLatch的countDown方法时，N就会减1，CountDownLatch的await方法会阻塞当前线程，直到N变成零。由于countDown方法可以用在任何地方，所以这里说的N个点，可以是N个线程，也可以是1个线程里的N个执行步骤。用在多个线程时，只需要把这个CountDownLatch的引用传递到线程里即可。

### 原理

#### 初始化
CountDownLatch内部使用了共享锁，new CountDownLatch(int )的构造函数传入n为计数器,其实设置state值为n，表示n个锁资源占用
```java
 public CountDownLatch(int var1) {
        if (var1 < 0) {
            throw new IllegalArgumentException("count < 0");
        } else {
            this.sync = new CountDownLatch.Sync(var1);
        }
    }
    
    
Sync(int var1) {
            this.setState(var1);
        }
```
#### 获得锁
```java
        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }
```
共享锁有个约定，返回有三种情况。
- 0为获取锁且没有其他资源
- 正数 获取锁并且还有其他资源
- 负数 获取锁资源失败

共享锁在tryAcquireShared返回大于0的值的时候，会唤醒其他停顿状态加锁线程。由于没有对state的增加操作，所以当state变成0的时候，所有尝试加锁的线程都会被唤醒。

#### 释放锁

```java
protected boolean tryReleaseShared(int var1) {
            int var2;
            int var3;
            do {
                var2 = this.getState();
                if (var2 == 0) {
                    return false;
                }

                var3 = var2 - 1;
            } while(!this.compareAndSetState(var2, var3));

            return var3 == 0;
        }
```
释放锁的操作，获取state，如果等于0，说明当前没有锁资源也就无法释放，返回false；否则执行正常操作 state - 1，当只有state变成0的时候，才返回true，tryReleaseShared返回true的时候会触发唤醒其他加锁线程的操作。

countDown是释放锁，最终会调用到tryReleaseShared。
```java
public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }
```

#### 总结
CountDownLatch就是一个通过构造方法初始化锁资源占用数后，然后通过countDown方法不断释放锁的过程。




## 同步屏障CyclicBarrier
CyclicBarrier要做的事情是，让一组线程到达一个屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续运行。

说白了，就是等全部人都到齐了才出发，比如

如果把new CyclicBarrier(2)修改成new CyclicBarrier(3)，则主线程和子线程会永远等待，因为没有第三个线程执行await方法，即没有第三个线程到达屏障，所以之前到达屏障的两个线程都不会继续执行。

因为三个线程不到齐，所以两个线程是无法执行的



### 原理
在CyclicBarrier类的内部有一个计数器，每个线程在到达屏障点的时候都会调用await方法将自己阻塞，此时计数器会减1，当计数器减为0的时候所有因调用await方法而被阻塞的线程将被唤醒。

CyclicBarrier内部是通过条件队列trip来对线程进行阻塞的，并且其内部维护了两个int型的变量parties和count

parties表示每次拦截的线程数，该值在构造时进行赋值。

count是内部计数器，它的初始值和parties相同，以后随着每次await方法的调用而减1，直到减为0就将所有线程唤醒。

```java
private int dowait(boolean timed, long nanos) 
throws InterruptedException, BrokenBarrierException, TimeoutException {
   final ReentrantLock lock = this.lock;
   lock.lock();
   try {
       final Generation g = generation;
       //检查当前栅栏是否被打翻
       if (g.broken) {
           throw new BrokenBarrierException();
       }
       //检查当前线程是否被中断
       if (Thread.interrupted()) {
           //如果当前线程被中断会做以下三件事
           //1.打翻当前栅栏
           //2.唤醒拦截的所有线程
           //3.抛出中断异常
           breakBarrier();
           throw new InterruptedException();
       }
      //每次都将计数器的值减1
      int index = --count;
      //计数器的值减为0则需唤醒所有线程并转换到下一代
      if (index == 0) {
          boolean ranAction = false;
          try {
              //唤醒所有线程前先执行指定的任务
              final Runnable command = barrierCommand;
              if (command != null) {
                  command.run();
              }
              ranAction = true;
              //唤醒所有线程并转到下一代
              nextGeneration();
              return 0;
          } finally {
              //确保在任务未成功执行时能将所有线程唤醒
              if (!ranAction) {
                  breakBarrier();
              }
          }
      }

      //如果计数器不为0则执行此循环
      for (;;) {
          try {
              //根据传入的参数来决定是定时等待还是非定时等待
              if (!timed) {
                  trip.await();
              }else if (nanos > 0L) {
                  nanos = trip.awaitNanos(nanos);
              }
          } catch (InterruptedException ie) {
              //若当前线程在等待期间被中断则打翻栅栏唤醒其他线程
              if (g == generation && ! g.broken) {
                  breakBarrier();
                  throw ie;
              } else {
                  //若在捕获中断异常前已经完成在栅栏上的等待, 
                  //则直接调用中断操作
                  Thread.currentThread().interrupt();
              }
          }
          //如果线程因为打翻栅栏操作而被唤醒则抛出异常
          if (g.broken) {
              throw new BrokenBarrierException();
          }
          //如果线程因为换代操作而被唤醒则返回计数器的值
          if (g != generation) {
              return index;
          }
          //如果线程因为时间到了而被唤醒则打翻栅栏并抛出异常
          if (timed && nanos <= 0L) {
              breakBarrier();
              throw new TimeoutException();
          }
      }
   } finally {
       lock.unlock();
   }
}
```

dowait方法中每次都将count减1，减完后立马进行判断看看是否等于0
1. 如果等于0的话就会先去执行之前指定好的任务，执行完之后再调用nextGeneration方法将栅栏转到下一代，在该方法中会将所有线程唤醒，将计数器的值重新设为parties
，最后会重新设置栅栏代次。
2. 如果计数器此时还不等于0的话就进入for循环，根据参数来决定是调用trip.awaitNanos(nanos)还是trip.await()
方法，这两方法对应着定时和非定时等待。如果在等待过程中当前线程被中断就会执行breakBarrier方法，该方法叫做打破栅栏，意味着游戏在中途被掐断，设置generation的broken状态为true并唤醒所有线程。同时这也说明在等待过程中有一个线程被中断整盘游戏就结束，所有之前被阻塞的线程都会被唤醒。






### CyclicBarrier和CountDownLatch的区别
1. CyclicBarrier的计数器由自己控制，而CountDownLatch的计数器则由使用者来控制，在CyclicBarrier中线程调用await方法不仅会将自己阻塞还会将计数器减1，而在CountDownLatch
中线程调用await方法只是将自己阻塞而不会减少计数器的值。
2. CountDownLatch的计数器只能使用一次，而CyclicBarrier的计数器可以使用reset()
方法重置。所以CyclicBarrier能处理更为复杂的业务场景。例如，如果计算发生错误，可以重置计数器，并让线程重新执行一次。
3. CyclicBarrier还提供其他有用的方法，比如getNumberWaiting方法可以获得Cyclic-Barrier阻塞的线程数量。isBroken()方法用来了解阻塞的线程是否被中断

## 控制并发线程数的Semaphore

Semaphore（信号量）是用来控制同时访问特定资源的线程数量，它通过协调各个线程，以保证合理的使用公共资源。

Semaphore可以用于做流量控制，特别是公用资源有限的应用场景，比如数据库连接。假如有一个需求，要读取几万个文件的数据，因为都是IO密集型任务，我们可以启动几十个线程并发地读取，但是如果读到内存后，还需要存储到数据库中，而数据库的连接数只有10个，这时我们必须控制只有10个线程同时获取数据库连接保存数据，否则会报错无法获取数据库连接。这个时候，就可以使用Semaphore来做流量控制
```java

public class SemaphoreTest { 
	private static final int THREAD_COUNT = 30;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
	private static Semaphore s = new Semaphore(10);
	
	public static void main(String[] args) {
		for (int i = 0; i < THREAD_COUNT; i++) {
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					try { 
						s.acquire();
						System.out.println("save data");
						s.release();
					} catch (InterruptedException e) {
					}
				}
			});
		}
		threadPool.shutdown();
	}
}
```

虽然有30个线程在执行，但是只允许10个并发执行。Semaphore的构造方法Semaphore（int permits）接受一个整型的数字，表示可用的许可证数量。Semaphore（10）表示允许10个线程获取许可证，也就是最大并发数是10。Semaphore的用法也很简单，首先线程使用Semaphore的acquire()方法获取一个许可证，使用完之后调用release()方法归还许可证。

## 线程间交换数据的Exchanger

Exchanger（交换者）是一个用于线程间协作的工具类。Exchanger用于进行线程间的数据交换。它提供一个同步点，在这个同步点，两个线程可以交换彼此的数据。这两个线程通过exchange方法交换数据，如果第一个线程先执行exchange()方法，它会一直等待第二个线程也执行exchange方法，当两个线程都到达同步点时，这两个线程就可以交换数据，将本线程生产出来的数据传递给对方。


