package com.concurrent.chapter3;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/6 下午11:54
 *
 * 对象引用逃逸
 */
public class FinalReferenceEscapeExample {
    final int i;
    static FinalReferenceEscapeExample obj;

    public FinalReferenceEscapeExample() {
        i = 1;//写final域
        obj = this;//this引用在这里"逸出"
    }

    public static void writer() {
        new FinalReferenceEscapeExample();
    }

    public static void reader() {
        if (obj != null) {//仍然可能无法看到final域被初始化后的值
            int temp = obj.i;//这里将读取到final域初始化之前的值
        }
    }

}
