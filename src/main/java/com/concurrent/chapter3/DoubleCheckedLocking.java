package com.concurrent.chapter3;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/7 上午12:04
 *
 * 有问题的双重校验锁定
 */
public class DoubleCheckedLocking {
    private static Instance instance;

    public static Instance getInstance() {
        if (instance == null) {//第一次检查
            synchronized (DoubleCheckedLocking.class) {//加锁
                if (instance == null) {//第二次检查
                    instance = new Instance();//问题的根源，instance不为null不代表已经初始化了，分配内存地址和初始化可能会发生重排序
                }
            }
        }
        return instance;
    }


   static class Instance {}
}
