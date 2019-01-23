

# 第10章 Executor框架

## 介绍

从代码上看，Executor 是一个简单的接口，但它却是整个异步任务执行框架的基础。他将任务的提交和执行解耦开来，任务用 Runnable 来表示。Executor 基于生产者-消费者模式，提交任务的线程相当于生产者，执行任务的线程相当于消费者。同时，Executor 的实现还提供了对任务执行的生命周期管理的支持。

![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/10-1.png)


### Executor框架结构
Executor框架有3部分组成
1. 任务：包括被执行任务需要实现的接口：Runnable接口、Callable接口
2. 任务的执行：包括任务执行机制的核心接口Executor，以及继承自己Executor的ExecutorService接口。ThreadPoolExecutor和ScheduledThreadPoolExecutor是实现ExecutorService接口两个关键类
3. 异步计算的结果：包括接口Future和实现Future接口的FutureTask类

![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/10-2.png)

## ThreadPoolExecutor

Executor框架最核心的类是ThreadPoolExecutor，它是线程池的实现类，主要由下列4个组件构成
1. corePool：核心线程池的大小。
2. maximumPool：最大线程池的大小。
3. BlockingQueue：用来暂时保存任务的工作队列。
4. RejectedExecutionHandler：当ThreadPoolExecutor已经关闭或ThreadPoolExecutor已经饱和时（达到了最大线程池大小且工作队列已满），execute()方法将要调用的Handler。

通过Executor框架的工具类Executors，可以创建3种类型的ThreadPoolExecutor：
1. FixedThreadPool
2. SingleThreadExecutor
3. CachedThreadPool

### FixedThreadPool
FixedThreadPool被称为可重用固定线程数的线程池
- 可控制线程最大并发数（同时执行的线程数）
- 超出的线程会在队列中等待

![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/10-3.png)

### SingleThreadExecutor
- 有且仅有一个工作线程执行任务
- 所有任务按照指定顺序执行，即遵循队列的入队出队规则

![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/10-4.png)

### CachedThreadPool

CachedThreadPool是一个会根据需要创建新线程的线程池
- 线程数无限制
- 有空闲线程则复用空闲线程，若无空闲线程则新建线程 一定程序减少频繁创建/销毁线程，减少系统开销
![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/10-5.png)