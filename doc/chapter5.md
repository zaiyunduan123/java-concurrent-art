<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [5、Java中的锁](#5java%E4%B8%AD%E7%9A%84%E9%94%81)
  - [Lock接口](#lock%E6%8E%A5%E5%8F%A3)
    - [Lock接口提供的synchronized所不具备的主要特性](#lock%E6%8E%A5%E5%8F%A3%E6%8F%90%E4%BE%9B%E7%9A%84synchronized%E6%89%80%E4%B8%8D%E5%85%B7%E5%A4%87%E7%9A%84%E4%B8%BB%E8%A6%81%E7%89%B9%E6%80%A7)
  - [队列同步器](#%E9%98%9F%E5%88%97%E5%90%8C%E6%AD%A5%E5%99%A8)
    - [同步队列的结构](#%E5%90%8C%E6%AD%A5%E9%98%9F%E5%88%97%E7%9A%84%E7%BB%93%E6%9E%84)
    - [同步状态的获取](#%E5%90%8C%E6%AD%A5%E7%8A%B6%E6%80%81%E7%9A%84%E8%8E%B7%E5%8F%96)
  - [重入锁](#%E9%87%8D%E5%85%A5%E9%94%81)
    - [ReentrantLock的非公平锁](#reentrantlock%E7%9A%84%E9%9D%9E%E5%85%AC%E5%B9%B3%E9%94%81)
    - [ReentrantLock的公平锁](#reentrantlock%E7%9A%84%E5%85%AC%E5%B9%B3%E9%94%81)
  - [读写锁 ReentrantReadWriteLock](#%E8%AF%BB%E5%86%99%E9%94%81-reentrantreadwritelock)
  - [LockSupport工具](#locksupport%E5%B7%A5%E5%85%B7)
  - [Condition接口](#condition%E6%8E%A5%E5%8F%A3)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# 5、Java中的锁

## Lock接口
### Lock接口提供的synchronized所不具备的主要特性

| 特性 |	描述 | 
| ------ | ------ | 
| 尝试非阻塞地获取锁	|  当前线程尝试获取锁，如果这一时刻锁没有被其他线程获取到，则成功获取并持有锁 | 
| 能被中断地获取锁 | 	与synchronized不同，获取到的锁能够响应中断，当获取到锁的线程被中断时，中断异常将会被抛出，同时锁会被释放 | 
| 超时获取锁	|  在指定的时间截止之前获取锁，如果截止时间之前仍旧无法获取锁，则返回 | 


## 队列同步器

### 同步队列的结构
队列同步器的实现依赖内部的同步队列来完成同步状态的管理。它是一个FIFO的双向队列，当线程获取同步状态失败时，同步器会将当前线程和等待状态等信息包装成一个节点并将其加入同步队列，同时会阻塞当前线程。当同步状态释放时，会把首节点中的线程唤醒，使其再次尝试获取同步状态。

节点是构成同步队列的基础，同步器拥有首节点和尾节点，没有成功获取同步状态的线程会成为节点加入该队列的尾部，其结构如下图所示

![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/5-1.jpeg)

同步器包含了两个节点类型的引用，一个指向头节点，而另一个指向尾节点。

如果一个线程没有获得同步队列，那么包装它的节点将被加入到队尾，显然这个过程应该是线程安全的。因此同步器提供了一个基于CAS的设置尾节点的方法：compareAndSetTail(Node expect,Node update),它需要传递一个它认为的尾节点和当前节点，只有设置成功，当前节点才被加入队尾。这个过程如下所示

![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/5-2.jpeg)

同步队列遵循FIFO，首节点是获取同步状态成功的节点，首节点线程在释放同步状态时，将会唤醒后继节点，而后继节点将会在获取同步状态成功时将自己设置为首节点，这一过程如下：

![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/5-3.jpeg)

### 同步状态的获取
下图描述了节点自旋获取同步状态的情况
![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/5-4.jpeg)
![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/5-5.jpeg)

## 重入锁

公平锁与非公平锁的区别：公平锁的获取顺序符合请求的绝对时间顺序，即FIFO，非公平锁不用。

### ReentrantLock的非公平锁
```java
protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
     }
```
1. getState()获取锁数量

1. 如果锁数量为0，再基于CAS尝试将state（锁数量）从0设置为1一次，如果设置成功，设置当前线程为独占锁的线程；

2. 如果锁数量不为0或者上边的尝试又失败了，查看当前线程是不是已经是独占锁的线程了，如果是，则将当前的锁数量+1；


### ReentrantLock的公平锁

和非公平锁相比多了一个hasQueuedPredecessors方法
```java
public final boolean hasQueuedPredecessors() {
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }
```
如果锁数量不为0或者当前线程不是等待队列中的头节点或者上边的尝试又失败了，查看当前线程是不是已经是独占锁的线程了，如果是，则将当前的锁数量+1；如果不是，则将该线程封装在一个Node
内，并加入到等待队列中去。等待被其前一个线程节点唤醒,保证串行化。


公平锁实现了先进先出的公平性，但是由于来一个线程就加入队列中，往往都需要阻塞，再由阻塞变为运行，这种上下文切换是非常耗性能的。非公平锁由于允许插队所以，上下文切换少的多，性能比较好，保证的大的吞吐量，但是容易出现饥饿问题

## 读写锁 ReentrantReadWriteLock

JAVA的并发包提供了读写锁ReentrantReadWriteLock，它表示两个锁，一个是读操作相关的锁，称为共享锁；一个是写相关的锁，称为排他锁，描述如下：

线程进入读锁的前提条件：
1. 没有其他线程的写锁，
2. 没有写请求或者有写请求，但调用线程和持有锁的线程是同一个。

线程进入写锁的前提条件：
1. 没有其他线程的读锁
2. 没有其他线程的写锁

而读写锁有以下三个重要的特性：
1. 公平选择性：支持非公平（默认）和公平的锁获取方式，吞吐量还是非公平优于公平。
2. 重进入：读锁和写锁都支持线程重进入。
3. 锁降级：遵循获取写锁、获取读锁再释放写锁的次序，写锁能够降级成为读锁。


```java
protected final boolean tryAcquire(int acquires) {
    //当前线程
    Thread current = Thread.currentThread();
    //获取状态
    int c = getState();
    //写线程数量（即获取独占锁的重入数）
    int w = exclusiveCount(c);
    
    //当前同步状态state != 0，说明已经有其他线程获取了读锁或写锁
    if (c != 0) {
        // 当前state不为0，此时：如果写锁状态为0说明读锁此时被占用返回false；
        // 如果写锁状态不为0且写锁没有被当前线程持有返回false
        if (w == 0 || current != getExclusiveOwnerThread())
            return false;
        
        //判断同一线程获取写锁是否超过最大次数（65535），支持可重入
        if (w + exclusiveCount(acquires) > MAX_COUNT)
            throw new Error("Maximum lock count exceeded");
        //更新状态
        //此时当前线程已持有写锁，现在是重入，所以只需要修改锁的数量即可。
        setState(c + acquires);
        return true;
    }
    
    //到这里说明此时c=0,读锁和写锁都没有被获取
    //writerShouldBlock表示是否阻塞
    if (writerShouldBlock() ||
        !compareAndSetState(c, c + acquires))
        return false;
    
    //设置锁为当前线程所有
    setExclusiveOwnerThread(current);
    return true;
}

```

```java
protected final int tryAcquireShared(int unused) {
    // 获取当前线程
    Thread current = Thread.currentThread();
    // 获取状态
    int c = getState();
    
    //如果写锁线程数 != 0 ，且独占锁不是当前线程则返回失败，因为存在锁降级
    if (exclusiveCount(c) != 0 &&
        getExclusiveOwnerThread() != current)
        return -1;
    // 读锁数量
    int r = sharedCount(c);
    /*
     * readerShouldBlock():读锁是否需要等待（公平锁原则）
     * r < MAX_COUNT：持有线程小于最大数（65535）
     * compareAndSetState(c, c + SHARED_UNIT)：设置读取锁状态
     */
     // 读线程是否应该被阻塞、并且小于最大值、并且比较设置成功
    if (!readerShouldBlock() &&
        r < MAX_COUNT &&
        compareAndSetState(c, c + SHARED_UNIT)) {
        //r == 0，表示第一个读锁线程，第一个读锁firstRead是不会加入到readHolds中
        if (r == 0) { // 读锁数量为0
            // 设置第一个读线程
            firstReader = current;
            // 读线程占用的资源数为1
            firstReaderHoldCount = 1;
        } else if (firstReader == current) { // 当前线程为第一个读线程，表示第一个读锁线程重入
            // 占用资源数加1
            firstReaderHoldCount++;
        } else { // 读锁数量不为0并且不为当前线程
            // 获取计数器
            HoldCounter rh = cachedHoldCounter;
            // 计数器为空或者计数器的tid不为当前正在运行的线程的tid
            if (rh == null || rh.tid != getThreadId(current)) 
                // 获取当前线程对应的计数器
                cachedHoldCounter = rh = readHolds.get();
            else if (rh.count == 0) // 计数为0
                //加入到readHolds中
                readHolds.set(rh);
            //计数+1
            rh.count++;
        }
        return 1;
    }
    return fullTryAcquireShared(current);
}
```

## LockSupport工具
在没有LockSupport之前，线程的挂起和唤醒咱们都是通过Object的wait和notify/notifyAll方法实现。wait和notify/notifyAll方法只能在同步代码块里用

LockSupport比Object的wait/notify有两大优势：
1. LockSupport不需要在同步代码块里 。所以线程间也不需要维护一个共享的同步对象了，实现了线程间的解耦。
2. unpark函数可以先于park调用，所以不需要担心线程间的执行的先后顺序。

LockSupport 提供park()和unpark()方法实现阻塞线程和解除线程阻塞，LockSupport和每个使用它的线程都与一个许可(permit)
关联。permit相当于1，0的开关，默认是0，调用一次unpark就加1变成1，调用一次park会消费permit, 也就是将1变成0

```java
public static void park(Object blocker) {
        // 获取当前线程
        Thread t = Thread.currentThread();
        // 设置Blocker
        setBlocker(t, blocker);
        // 获取许可
        UNSAFE.park(false, 0L);
        // 重新可运行后再此设置Blocker
        setBlocker(t, null);
    }
```
调用park函数时，首先获取当前线程，然后设置当前线程的parkBlocker字段，即调用setBlocker函数，之后调用Unsafe类的park函数，之后再调用setBlocker函数。

至于为什么要调用setBlocker两次？因为当前线程首先设置好parkBlocker字段后再调用Unsafe的park方法,之后,当前线程已经被阻塞,等待unpark方法被调用, unpark方法被调用,该线程获得许可后,
可以继续进行下面的代码,第二个setBlocker参数parkBlocker字段设置为null,这样就完成了整个park方法的逻辑。如果没有第二个setBlocker,那么之后没有调用park(Object blocker),而直接调用getBlocker方法,得到的还是前一个park(Object blocker)设置的blocker,

## Condition接口
Condition的作用是对锁进行更精确的控制。Condition中的await()方法相当于Object的wait()方法，Condition中的signal()方法相当于Object的notify()方法，Condition中的signalAll()相当于Object的notifyAll()方法。不同的是，Object中的wait(),notify(),notifyAll()方法是和"同步锁"(synchronized关键字)捆绑使用的；而Condition是需要与"互斥锁"/"共享锁"捆绑使用的


