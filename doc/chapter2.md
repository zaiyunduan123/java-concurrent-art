<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [第2章 Java并发机制的底层实现原理](#%E7%AC%AC2%E7%AB%A0-java%E5%B9%B6%E5%8F%91%E6%9C%BA%E5%88%B6%E7%9A%84%E5%BA%95%E5%B1%82%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86)
  - [volatile](#volatile)
    - [实现原理](#%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86)
  - [synchronized](#synchronized)
    - [实现原理](#%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86-1)
    - [Java对象头](#java%E5%AF%B9%E8%B1%A1%E5%A4%B4)
    - [锁的升级](#%E9%94%81%E7%9A%84%E5%8D%87%E7%BA%A7)
  - [原子操作](#%E5%8E%9F%E5%AD%90%E6%93%8D%E4%BD%9C)
    - [处理器如何实现原子操作](#%E5%A4%84%E7%90%86%E5%99%A8%E5%A6%82%E4%BD%95%E5%AE%9E%E7%8E%B0%E5%8E%9F%E5%AD%90%E6%93%8D%E4%BD%9C)
    - [JAVA如何实现原子操作](#java%E5%A6%82%E4%BD%95%E5%AE%9E%E7%8E%B0%E5%8E%9F%E5%AD%90%E6%93%8D%E4%BD%9C)
      - [循环CAS](#%E5%BE%AA%E7%8E%AFcas)
      - [锁](#%E9%94%81)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


# 第2章 Java并发机制的底层实现原理

## volatile
一旦一个共享变量（类的成员变量、类的静态成员变量）被volatile修饰之后，那么就具备了两层语义：
1. 保证了不同线程对这个变量进行操作时的可见性，即一个线程修改了某个变量的值，这新值对其他线程来说是立即可见的。
2. 禁止进行指令重排序。

### 实现原理
加入volatile关键字和没有加入volatile关键字时所生成的汇编代码发现，加入volatile关键字时，会多出一个lock前缀指令

lock前缀指令实际上相当于一个内存屏障（也成内存栅栏），内存屏障会提供3个功能：
1. 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成；
2. 它会强制将对缓存的修改操作立即写入主存；
3. 如果是写操作，它会导致其他CPU中对应的缓存行无效。

## synchronized
synchronized可以保证方法或者代码块在运行时，同一时刻只有一个方法可以进入到临界区，同时它还可以保证共享变量的内存可见性


利用synchronized实现同步的基础，Java中的每一个对象都可以作为锁，有以下3种形式
- 对于普通同步方法，锁是当前实例对象
- 对于静态同步方法，锁是当前类的Class对象
- 对于同步方法块，锁锁Synchonized括号里配置的对象

### 实现原理
Java 虚拟机中的同步(Synchronization)是基于进入和退出Monitor对象实现， 无论是显式同步(有明确的 monitorenter 和 monitorexit 指令,即同步代码块)还是隐式同步都是如此。在 Java 语言中，同步用的最多的地方可能是被 synchronized 修饰的同步方法。同步方法并不是由monitorenter 和 monitorexit 指令来实现同步的，而是由方法调用指令读取运行时常量池中方法的 ACC_SYNCHRONIZED 标志来隐式实现的

我们对如下同步代码块进行javap反编译：
```java
1    public void add(Object obj){
2        synchronized (obj){
3            //do something
4        }
5    }
```
反编译后的代码如下：
```java
1public class com.wuzy.thread.SynchronizedDemo {
 2  public com.wuzy.thread.SynchronizedDemo();
 3    Code:
 4       0: aload_0
 5       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
 6       4: return
 7
 8  public void add(java.lang.Object);
 9    Code:
10       0: aload_1
11       1: dup
12       2: astore_2
13       3: monitorenter //注意此处，进入同步方法
14       4: aload_2
15       5: monitorexit //注意此处，退出同步方法
16       6: goto          14
17       9: astore_3
18      10: aload_2
19      11: monitorexit //注意此处，退出同步方法
20      12: aload_3
21      13: athrow
22      14: return
23    Exception table:
24       from    to  target type
25           4     6     9   any
26           9    12     9   any
27}
```
我们看下第13行~15行代码，发现同步代码块是使用monitorenter和monitorexit指令来进行代码同步的,注意看第19行代码，为什么会多出一个monitorexit指令，主要是JVM为了防止代码出现异常，也能正确退出同步方法。

接下来我们将同步整个方法进行反编译一下：
```java
1  public synchronized void update(){
2
3    }
```
反编译后的代码如下：
```java
 1public class com.wuzy.thread.SynchronizedDemo {
 2  public com.wuzy.thread.SynchronizedDemo();
 3    Code:
 4       0: aload_0
 5       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
 6       4: return
 7
 8  public synchronized void update();
 9    Code:
10       0: return
11}
```

从反编译的代码看，同步方法并不是用monitorenter和monitorexit指令来进行同步的，实际上同步方法会被翻译成普通的方法调用和返回指令如:invokevirtual、areturn指令，在VM字节码层面并没有任何特别的指令来实现被synchronized修饰的方法，而是在Class文件的方法表中将该方法的access_flags字段中的synchronized标志位置设为1，表示该方法是同步方法并使用调用该方法的对象或该方法所属的Class在JVM的内部对象表示做为锁对象。

### Java对象头

在JVM中，对象在内存中的布局分为3块：对象头、实例数据和对齐填充。

synchronized使用的锁信息都放在对象头里，JVM中用2个字节来储存对象头(如果对象是数组则分配3个字节，多的一个字节用于存储数组的长度)。而对象头包含两部分信息，分别为Mark Word和类型指针。Mark Word主要用于储存对象自身的运行时数据，例如对象的hashCode、GC分代年龄、锁状态标志、线程持有的锁、偏向线程的ID、偏向时间戳等。而类型指针用于标识JVM通过这个指针来确定这个对象是哪个类的实例。

由于对象需要储存的运行时数据过多，Mark Word被设计成一个非固定的数据结构以便在极小的空间内存储更多的信息。对象在不同的状态下，Mark Word会存储不同的内容(只放32位虚拟机的图表)。

![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/2-1.png)


锁标志位的表示意义
- 锁标识 lock=00 表示轻量级锁
- 锁标识 lock=10 表示重量级锁
- 偏向锁标识 biased_lock=1表示偏向锁
- 偏向锁标识 biased_lock=0且锁标识=01表示无锁状态
### 锁的升级
锁的状态：无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态

所谓锁的升级、降级，就是 JVM 优化 synchronized 运行的机制，当 JVM 检测到不同的竞争状况时，会自动切换到适合的锁实现，这种切换就是锁的升级、降级。

当没有竞争出现时，默认会使用偏斜锁。JVM 会利用 CAS 操作（compare and swap），在对象头上的 Mark Word 部分设置线程 ID，以表示这个对象偏向于当前线程，所以并不涉及真正的互斥锁。这样做的假设是基于在很多应用场景中，大部分对象生命周期中最多会被一个线程锁定，使用偏斜锁可以降低无竞争开销。

如果有另外的线程试图锁定某个已经被偏斜过的对象，JVM 就需要撤销（revoke）偏斜锁，并切换到轻量级锁实现。轻量级锁依赖 CAS 操作 Mark Word 来试图获取锁，如果重试成功，就使用普通的轻量级锁；否则，进一步升级为重量级锁。

通俗来讲就是：
- 偏向锁：仅有一个线程进入临界区
- 轻量级锁：多个线程交替进入临界区
- 重量级锁：多个线程同时进入临界区


我注意到有的观点认为 Java 不会进行锁降级。实际上据我所知，锁降级确实是会发生的，当 JVM 进入安全点（SafePoint）的时候，会检查是否有闲置的 Monitor，然后试图进行降级。

## 原子操作
原子操作（atomic operation）意为"不可被中断的一个或一系列操作" 

### 处理器如何实现原子操作
1、使用总线锁保证原子性

所谓总线锁就是使用处理器提供的一个LOCK＃信号，当一个处理器在总线上输出此信号时，其他处理器的请求将被阻塞住,那么该处理器可以独占使用共享内存。

2、使用缓存锁保证原子性

所谓“缓存锁定”就是如果缓存在处理器缓存行中内存区域在LOCK操作期间被锁定，当它执行锁操作回写内存时，处理器不在总线上声言LOCK＃信号，而是修改内部的内存地址，并允许它的缓存一致性机制来保证操作的原子性


### JAVA如何实现原子操作

Java中主要通过下面两种方式来实现原子操作：锁和循环CAS

#### 循环CAS
CAS全称Compare-and-Swap（比较并交换），JVM中的CAS操作是依赖处理器提供的cmpxchg指令完成的，CAS指令中有3个操作数，分别是内存位置V、旧的预期值A和新值B

当CAS指令执行时，当且仅当内存位置V符合旧预期值时A时，处理器才会用新值B去更新V的值，否则就不执行更新，但是无论是否更新V，都会返回V的旧值，该操作过程就是一个原子操作

JDK1.5之后才可以使用CAS，由sun.misc.Unsafe类里面的compareAndSwapInt()和compareAndSwapLong()等方法包装实现，虚拟机在即时编译时，对这些方法做了特殊处理，会编译出一条相关的处理器CAS指令

**CAS实现原子操作的三大问题**
 
**1、ABA问题**：初次读取内存旧值时是A，再次检查之前这段期间，如果内存位置的值发生过从A变成B再变回A的过程，我们就会错误的检查到旧值还是A，认为没有发生变化，其实已经发生过A-B-A得变化，这就是CAS操作的ABA问题

解决方法：使用版本号，即1A-2B-3A，这样就会发现1A到3A的变化，不存在ABA变化无感知问题，JDK的atomic包中提供一个带有标记的原子引用类AtomicStampedReference来解决ABA问题

**2、循环时间长开销大**：自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销

**3、只能保证一个共享变量的原子操作**：当对一个共享变量执行操作时，可以使用循环CAS来保证原子操作，但是多个共享变量操作时，就无法保证了

解决方法：
- 将多个变量组合成一个共享变量，jdk提供了AtomicReference类来保证引用对象之间的原子性，那么就可以把多个变量放在一个对象里来进行CAS操作
- 使用锁

#### 锁

锁机制保证了只有获得锁的线程才能够操作锁定的内存区域。JVM内部实现了很多锁机制，有偏向锁、轻量级锁和互斥锁。除了偏向锁，JVM实现锁的方式都用了循环CAS，即当一个线程想进入同步块的时候使用循环CAS的方式来获取锁，当它退出同步块的时候使用循环CAS释放锁。


