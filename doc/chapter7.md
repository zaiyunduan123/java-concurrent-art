# 第七章、Java中的13个原子操作类

## 原子更新基本类型

atomic包里面一共提供了13个类，分为4种类型，分别是：原子更新基本类型，原子更新数组，原子更新引用，原子更新属性，这13个类都是使用Unsafe实现的包装类。

![](https://github.com/zaiyunduan123/java-concurrent-art/blob/master/image/7-1.png)

AtomicInteger的常用方法有：
1.int addAndGet(int delta)：以原子的方式将输入的值与实例中的值相加，并把结果返回
2.boolean compareAndSet(int expect, int update)：如果输入值等于预期值，以原子的方式将该值设置为输入的值
3.final int getAndIncrement()：以原子的方式将当前值加1，并返回加1之前的值
4.void lazySet(int newValue)：最终会设置成newValue，使用lazySet设置值后，可能导致其他线程在之后的一小段时间内还是可以读到旧的值。
5.int getAndSet(int newValue)：以原子的方式将当前值设置为newValue,并返回设置之前的旧值



getAndIncremen的实现代码如下：　　
```java
public final int getAndIncrement() {
        return unsafe.getAndAddInt(this, valueOffset, 1);
    }
```
我们可以看到getAndIncrement调用了unsafe的getAndAddInt，getAndAddInt的实现：

```java
public final int getAndAddInt(Object var1, long var2, int var4) {
        int var5;
        do {
            var5 = this.getIntVolatile(var1, var2);
        } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));

        return var5;
    }
```
getAndAddInt调用了Unsafe的native方法：getIntVolatile和compareAndSwapInt，在do-while循环中先取得当前值，然后通过CAS判断当前值是否和current一致，如果一致意味着值没被其他线程修改过，把当前值设置为当前值+var4，，如果不相等程序进入信的CAS循环。

由于atomic只提供了int,long和boolean的原子操作类，那么其他的基本类型，如byte,char,float,double如何实现原子操作呢，原子操作都是通过Unsafe实现的，让我们看一下Unsafe的实现
```java
public final native boolean compareAndSwapObject(Object var1, long var2, Object var4, Object var5);

public final native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);

public final native boolean compareAndSwapLong(Object var1, long var2, long var4, long var6);
```
通过代码，我们发现Unsafe只提供了3种CAS方法：compareAndSwapObject、compareAndSwapInt和compareAndSwapLong，再看AtomicBoolean源码，发现它是先把Boolean转换成整型，再使用compareAndSwapInt进行CAS，所以原子更新char、float和double变量也可以用类似的思路来实现。

## 原子更新数组

AtomicIntegerArray主要提供了以原子方式更新数组里的整数，常见方法如下：
1. int addAndGet(int i, int delta)：以原子的方式将输入值与数组中索引为i的元素相加
2. boolean compareAndSet(int i, int expect, int update)：如果当前值等于预期值，则以原子方式将数组位置i的元素设置成update值。　　

```java
public class AtomicIntegerArrayTest {

    private static int[] value = new int[]{1,2,3};
    private static AtomicIntegerArray atomicInteger = new AtomicIntegerArray(value);

    public static void main(String[] args){
        atomicInteger.getAndSet(0,12);
        System.out.println(atomicInteger.get(0));
        System.out.println(value[0]);
    }
}
```
需要注意的是，数组value通过构造方法传递进去，然后AtomicIntegerArray会将当前数组复制一份，所以当AtomicIntegerArray对内部的数组元素进行修改时，不会影响传入的数组。