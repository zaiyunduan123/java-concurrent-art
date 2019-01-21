<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [锁的内存语义](#%E9%94%81%E7%9A%84%E5%86%85%E5%AD%98%E8%AF%AD%E4%B9%89)
  - [锁的释放-获取建立的happens before关系](#%E9%94%81%E7%9A%84%E9%87%8A%E6%94%BE-%E8%8E%B7%E5%8F%96%E5%BB%BA%E7%AB%8B%E7%9A%84happens-before%E5%85%B3%E7%B3%BB)
  - [锁释放和获取的内存语义](#%E9%94%81%E9%87%8A%E6%94%BE%E5%92%8C%E8%8E%B7%E5%8F%96%E7%9A%84%E5%86%85%E5%AD%98%E8%AF%AD%E4%B9%89)
    - [锁释放](#%E9%94%81%E9%87%8A%E6%94%BE)
    - [锁获取](#%E9%94%81%E8%8E%B7%E5%8F%96)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# 锁的内存语义

## 锁的释放-获取建立的happens before关系
锁是Java并发编程中最重要的同步机制，锁除了了让临界区互斥执行外，还可以让释放锁的线程向获取同一个锁的线程发送消息

```java
public class MonitorExample {
	int a = 0;

	public synchronized void writer() {// 1
		a++;// 2
	}// 3

	public synchronized void reader() {// 4
		int i = a;// 5
	}// 6

}

```
1. 根据程序次序规则，1 happens before 2，2 happens before 3，4 happens before 5，5 happens before 6
2. 根据监视器锁规则，3 happens before 4
3. 感觉传递性，2 happens before 5

![](http://img.blog.csdn.net/20170809091730250)

上图表示线程A释放锁之后，随后线程B获取同一个锁，也就2 happens before 5 。

## 锁释放和获取的内存语义

当线程释放锁时，JMM会把该线程对应的本地内存中的共享变量刷新到主内存中

### 锁释放
![](http://img.blog.csdn.net/20170809092235458)

而线程B获取锁，JMM会把其对应内存置为无效，从而使被监视器保护的临界区代码必须要从主内存去读取共享变量

### 锁获取
![](http://img.blog.csdn.net/20170809092536470)

锁的内存语义总结：
1. 线程A释放一个锁，实质上是线程A向接下来将要获取这个锁的某个线程发送了（线程A对共享变量做了修改的）消息
2. 线程B获取一个锁，实质上是线程B接收了之前线程A发的（在释放锁之前对共享变量做了修改的）消息
3. 线程A释放锁，随后线程B获取锁，这个过程实质上级是线程A通过主内存向线程B发送消息