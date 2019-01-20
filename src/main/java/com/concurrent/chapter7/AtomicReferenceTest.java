package com.concurrent.chapter7;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author jiangyunxiong
 * @Date 2019/1/20 下午8:52
 */
public class AtomicReferenceTest {

    public static AtomicReference<User> atomicReference = new
            AtomicReference<>();

    public static void main(String[] args) {
        User user = new User("conan", 15);
        atomicReference.set(user);
        User updateUser = new User("Shinici", 17);
        atomicReference.compareAndSet(user, updateUser);
        System.out.println(atomicReference.get().getName());
        System.out.println(atomicReference.get().getName());
    }

    static class User{
        private String name;
        private int old;

        public User(String name, int old) {
            this.name = name;
            this.old = old;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getOld() {
            return old;
        }

        public void setOld(int old) {
            this.old = old;
        }
    }
}
