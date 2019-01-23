

# 第6章 Java并发容器和框架

## Java中的阻塞队列
阻塞队列（BlockingQueue）是一个支持两个附加操作的队列。这两个附加的操作支持阻塞的插入和移除方法


| 方法\处理方式 | 抛出异常  | 返回特殊值 | 一直阻塞 | 超时退出           |
| ------------- | --------- | ---------- | -------- | ------------------ |
| 插入方法      | add(e)    | offer(e)   | put(e)   | offer(e,time,unit) |
| 移除方法      | remove()  | poll()     | take()   | poll(time,unit)    |
| 检查方法      | element() | peek()     | 不可用   | 不可用             |

- 抛出异常：是指当阻塞队列满时候，再往队列里插入元素，会抛出 IllegalStateException("Queue full") 异常。当队列为空时，从队列里获取元素时会抛出 NoSuchElementException 异常 。
- 返回特殊值：插入方法会返回是否成功，成功则返回 true。移除方法，则是从队列里拿出一个元素，如果没有则返回 null
- 一直阻塞：当阻塞队列满时，如果生产者线程往队列里 put 元素，队列会一直阻塞生产者线程，直到拿到数据，或者响应中断退出。当队列空时，消费者线程试图从队列里 take 元素，队列也会阻塞消费者线程，直到队列可用。
- 超时退出：当阻塞队列满时，队列会阻塞生产者线程一段时间，如果超过一定的时间，生产者线程就会退出。

### 7个阻塞队列

JDK7 提供了 7 个阻塞队列。分别是

- ArrayBlockingQueue ：一个由数组结构组成的有界阻塞队列。
- LinkedBlockingQueue ：一个由链表结构组成的有界阻塞队列。
- PriorityBlockingQueue ：一个支持优先级排序的无界阻塞队列。
- DelayQueue：一个使用优先级队列实现的无界阻塞队列。
- SynchronousQueue：一个不存储元素的阻塞队列。
- LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。
- LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。


#### ArrayBlockingQueue

ArrayBlockingQueue 是一个用数组实现的有界阻塞队列。此队列按照先进先出（FIFO）的原则对元素进行排序。默认情况下不保证访问者公平的访问队列，所谓公平访问队列是指阻塞的所有生产者线程或消费者线程，当队列可用时，可以按照阻塞的先后顺序访问队列，即先阻塞的生产者线程，可以先往队列里插入元素，先阻塞的消费者线程，可以先从队列里获取元素。通常情况下为了保证公平性会降低吞吐量。我们可以使用以下代码创建一个公平的阻塞队列：

```
ArrayBlockingQueue fairQueue = new  ArrayBlockingQueue(1000,true);
```

访问者的公平性是使用可重入锁实现的，代码如下：

```
public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.items = new Object[capacity];
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
}
```
####  LinkedBlockingQueue
LinkedBlockingQueue 是一个用链表实现的有界阻塞队列。此队列的默认和最大长度为 Integer.MAX_VALUE。此队列按照先进先出的原则对元素进行排序。

#### PriorityBlockingQueue
PriorityBlockingQueue 是一个支持优先级的无界队列。默认情况下元素采取自然顺序排列，也可以通过比较器 comparator 来指定元素的排序规则。元素按照升序排列。
#### DelayQueue
DelayQueue 是一个支持延时获取元素的无界阻塞队列。队列使用 PriorityQueue 来实现。队列中的元素必须实现 Delayed 接口，在创建元素时可以指定多久才能从队列中获取当前元素。只有在延迟期满时才能从队列中提取元素。我们可以将 DelayQueue 运用在以下应用场景：
- 缓存系统的设计：可以用 DelayQueue 保存缓存元素的有效期，使用一个线程循环查询 DelayQueue，一旦能从 DelayQueue 中获取元素时，表示缓存有效期到了。
- 定时任务调度：使用 DelayQueue 保存当天将会执行的任务和执行时间，一旦从 DelayQueue 中获取到任务就开始执行，从比如 TimerQueue 就是使用 DelayQueue 实现的。

队列中的 Delayed 必须实现 compareTo 来指定元素的顺序。比如让延时时间最长的放在队列的末尾。实现代码如下：

```java
public int compareTo(Delayed other) {
           if (other == this) // compare zero ONLY if same object
                return 0;
            if (other instanceof ScheduledFutureTask) {
                ScheduledFutureTask x = (ScheduledFutureTask)other;
                long diff = time - x.time;
                if (diff < 0)
                    return -1;
                else if (diff > 0)
                    return 1;
	   else if (sequenceNumber < x.sequenceNumber)
                    return -1;
                else
                    return 1;
            }
            long d = (getDelay(TimeUnit.NANOSECONDS) -
                      other.getDelay(TimeUnit.NANOSECONDS));
            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }
```

##### 如何实现 Delayed 接口

我们可以参考 ScheduledThreadPoolExecutor 里 ScheduledFutureTask 类。这个类实现了 Delayed 接口。首先：在对象创建的时候，使用 time 记录前对象什么时候可以使用，代码如下：

```java
ScheduledFutureTask(Runnable r, V result, long ns, long period) {
            super(r, result);
            this.time = ns;
            this.period = period;
            this.sequenceNumber = sequencer.getAndIncrement();
}
```

然后使用 getDelay 可以查询当前元素还需要延时多久，代码如下：

```java
public long getDelay(TimeUnit unit) {
            return unit.convert(time - now(), TimeUnit.NANOSECONDS);
        }
```

通过构造函数可以看出延迟时间参数 ns 的单位是纳秒，自己设计的时候最好使用纳秒，因为 getDelay 时可以指定任意单位，一旦以纳秒作为单位，而延时的时间又精确不到纳秒就麻烦了。使用时请注意当 time 小于当前时间时，getDelay 会返回负数。

##### 如何实现延时队列

延时队列的实现很简单，当消费者从队列里获取元素时，如果元素没有达到延时时间，就阻塞当前线程。

```java
long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay <= 0)
                        return q.poll();
                    else if (leader != null)
                        available.await();
```
#### SynchronousQueue
SynchronousQueue 是一个不存储元素的阻塞队列。每一个 put 操作必须等待一个 take 操作，否则不能继续添加元素。SynchronousQueue 可以看成是一个传球手，负责把生产者线程处理的数据直接传递给消费者线程。队列本身并不存储任何元素，非常适合于传递性场景, 比如在一个线程中使用的数据，传递给另外一个线程使用，SynchronousQueue 的吞吐量高于 LinkedBlockingQueue 和 ArrayBlockingQueue。
#### LinkedTransferQueue
LinkedTransferQueue 是一个由链表结构组成的无界阻塞 TransferQueue 队列。相对于其他阻塞队列，LinkedTransferQueue 多了 tryTransfer 和 transfer 方法。

transfer 方法。如果当前有消费者正在等待接收元素（消费者使用 take() 方法或带时间限制的 poll() 方法时），transfer 方法可以把生产者传入的元素立刻 transfer（传输）给消费者。如果没有消费者在等待接收元素，transfer 方法会将元素存放在队列的 tail 节点，并等到该元素被消费者消费了才返回。transfer 方法的关键代码如下：

```java
Node pred = tryAppend(s, haveData);
return awaitMatch(s, pred, e, (how == TIMED), nanos);
```

第一行代码是试图把存放当前元素的 s 节点作为 tail 节点。第二行代码是让 CPU 自旋等待消费者消费元素。因为自旋会消耗 CPU，所以自旋一定的次数后使用 Thread.yield() 方法来暂停当前正在执行的线程，并执行其他线程。

tryTransfer 方法。则是用来试探下生产者传入的元素是否能直接传给消费者。如果没有消费者等待接收元素，则返回 false。和 transfer 方法的区别是 tryTransfer 方法无论消费者是否接收，方法立即返回。而 transfer 方法是必须等到消费者消费了才返回。

对于带有时间限制的 tryTransfer(E e, long timeout, TimeUnit unit) 方法，则是试图把生产者传入的元素直接传给消费者，但是如果没有消费者消费该元素则等待指定的时间再返回，如果超时还没消费元素，则返回 false，如果在超时时间内消费了元素，则返回 true。

#### LinkedBlockingDeque

LinkedBlockingDeque 是一个由链表结构组成的双向阻塞队列。所谓双向队列指的你可以从队列的两端插入和移出元素。双端队列因为多了一个操作队列的入口，在多线程同时入队时，也就减少了一半的竞争。相比其他的阻塞队列，LinkedBlockingDeque 多了 addFirst，addLast，offerFirst，offerLast，peekFirst，peekLast 等方法，以 First 单词结尾的方法，表示插入，获取（peek）或移除双端队列的第一个元素。以 Last 单词结尾的方法，表示插入，获取或移除双端队列的最后一个元素。另外插入方法 add 等同于 addLast，移除方法 remove 等效于 removeFirst。但是 take 方法却等同于 takeFirst，不知道是不是 Jdk 的 bug，使用时还是用带有 First 和 Last 后缀的方法更清楚。

在初始化 LinkedBlockingDeque 时可以设置容量防止其过渡膨胀。另外双向阻塞队列可以运用在“工作窃取”模式中。

### 实现原理

阻塞队列使用通知模式实现。所谓通知模式，就是当生产者往满的队列里添加元素时会阻塞住生产者，当消费者消费了一个队列中的元素后，会通知生产者当前队列可用。通过查看 JDK 源码发现 ArrayBlockingQueue 使用了 Condition 来实现，代码如下：

```java
private final Condition notFull;
private final Condition notEmpty;

public ArrayBlockingQueue(int capacity, boolean fair) {
        // 省略其他代码 
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
    }

public void put(E e) throws InterruptedException {
        checkNotNull(e);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == items.length)
                notFull.await();
            insert(e);
        } finally {
            lock.unlock();
        }
}

public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0)
                notEmpty.await();
            return extract();
  } finally {
            lock.unlock();
        }
}

private void insert(E x) {
        items[putIndex] = x;
        putIndex = inc(putIndex);
        ++count;
        notEmpty.signal();
    }
```

当我们往队列里插入一个元素时，如果队列不可用，阻塞生产者主要通过 LockSupport.park(this); 来实现

```java
public final void await() throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // clean up if cancelled
                unlinkCancelledWaiters();
            if (interruptMode != 0)

reportInterruptAfterWait(interruptMode);
        }
```

继续进入源码，发现调用 setBlocker 先保存下将要阻塞的线程，然后调用 unsafe.park 阻塞当前线程。

```java
public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        unsafe.park(false, 0L);
        setBlocker(t, null);
    }
```

unsafe.park 是个 native 方法，代码如下：

```java
public native void park(boolean isAbsolute, long time);
```

park 这个方法会阻塞当前线程，只有以下四种情况中的一种发生时，该方法才会返回。

- 与 park 对应的 unpark 执行或已经执行时。注意：已经执行是指 unpark 先执行，然后再执行的 park。
- 线程被中断时。
- 如果参数中的 time 不是零，等待了指定的毫秒数时。
- 发生异常现象时。这些异常事先无法确定。

我们继续看一下 JVM 是如何实现 park 方法的，park 在不同的操作系统使用不同的方式实现，在 linux 下是使用的是系统方法 pthread_cond_wait 实现。实现代码在 JVM 源码路径 src/os/linux/vm/os_linux.cpp 里的 os::PlatformEvent::park 方法，代码如下：

```java
void os::PlatformEvent::park() {      
     	     int v ;
	     for (;;) {
		v = _Event ;
	     if (Atomic::cmpxchg (v-1, &_Event, v) == v) break ;
	     }
	     guarantee (v >= 0, "invariant") ;
	     if (v == 0) {
	     // Do this the hard way by blocking ...
	     int status = pthread_mutex_lock(_mutex);
	     assert_status(status == 0, status, "mutex_lock");
	     guarantee (_nParked == 0, "invariant") ;
	     ++ _nParked ;
	     while (_Event < 0) {
	     status = pthread_cond_wait(_cond, _mutex);
	     // for some reason, under 2.7 lwp_cond_wait() may return ETIME ...
	     // Treat this the same as if the wait was interrupted
	     if (status == ETIME) { status = EINTR; }
	     assert_status(status == 0 || status == EINTR, status, "cond_wait");
	     }
	     -- _nParked ;
	     
	     // In theory we could move the ST of 0 into _Event past the unlock(),
	     // but then we'd need a MEMBAR after the ST.
	     _Event = 0 ;
	     status = pthread_mutex_unlock(_mutex);
	     assert_status(status == 0, status, "mutex_unlock");
	     }
	     guarantee (_Event >= 0, "invariant") ;
	     }

     }
```

pthread_cond_wait 是一个多线程的条件变量函数，cond 是 condition 的缩写，字面意思可以理解为线程在等待一个条件发生，这个条件是一个全局变量。这个方法接收两个参数，一个共享变量 _cond，一个互斥量 _mutex。而 unpark 方法在 linux 下是使用 pthread_cond_signal 实现的。park 在 windows 下则是使用 WaitForSingleObject 实现的。

当队列满时，生产者往阻塞队列里插入一个元素，生产者线程会进入 WAITING (parking) 状态。我们可以使用 jstack dump 阻塞的生产者线程看到这点：

```java
"main" prio=5 tid=0x00007fc83c000000 nid=0x10164e000 waiting on condition [0x000000010164d000]
   java.lang.Thread.State: WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x0000000140559fe8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
        at java.util.concurrent.locks.LockSupport.park(LockSupport.java:186)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2043)
        at java.util.concurrent.ArrayBlockingQueue.put(ArrayBlockingQueue.java:324)
        at blockingqueue.ArrayBlockingQueueTest.main(ArrayBlockingQueueTest.java:11)
```

