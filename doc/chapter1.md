<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [1、Java并发机制的底层实现原理](#1java%E5%B9%B6%E5%8F%91%E6%9C%BA%E5%88%B6%E7%9A%84%E5%BA%95%E5%B1%82%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86)
  - [上下文切换](#%E4%B8%8A%E4%B8%8B%E6%96%87%E5%88%87%E6%8D%A2)
    - [如何减少上下文切换](#%E5%A6%82%E4%BD%95%E5%87%8F%E5%B0%91%E4%B8%8A%E4%B8%8B%E6%96%87%E5%88%87%E6%8D%A2)
  - [死锁](#%E6%AD%BB%E9%94%81)
    - [避免死锁的几种常见方法](#%E9%81%BF%E5%85%8D%E6%AD%BB%E9%94%81%E7%9A%84%E5%87%A0%E7%A7%8D%E5%B8%B8%E8%A7%81%E6%96%B9%E6%B3%95)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# 1、并发编程的挑战

## 上下文切换
### 如何减少上下文切换

1. 减少上下文切换的方法有无锁并发编程、CAS算法、使用最少的线程和使用协程
2. 无锁并发编程。多线程竞争时，会引起上下文切换，所以多线程处理数据时，可以用一些办法来避免使用锁，将数据Id按hash算法取模来分段，不同的线程处理不同时端的数据
3. CAS算法。Java的atomic包使用CAS算法来更新数据，面不需要加锁。Atomic变量的更新可以实现数据操作的原子性及可见性。这个是由volatile 原语及CPU的CAS指令来实现的。
4. 使用最少的线程。若任务少，但创建了很多线程来处理，这样会造成大量的线程处于等等状态。

## 死锁
### 避免死锁的几种常见方法

1. 避免一个线程同时获取多个锁 
2. 避免一个线程同时占用多个资源，尽量保证每个锁只占用一个资源 
3. 尝试使用定时锁，使用lock.tryLock(timeout)来替代使用内部锁机制 
4. 对于数据库锁，加锁和解锁必须在一个数据库连接里，否则会出现解锁失败的情况