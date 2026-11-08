package com.houjiang.commons.util;

/**
 * @author Amos
 */
public class UserThreadLocal {
  /** 线程变量隔离 */
  private static final ThreadLocal<Object> USER_LOCAL = new ThreadLocal<>();

  public static void put(Object user){
    USER_LOCAL.set(user);
  }

  public static Object get(){
    return USER_LOCAL.get();
  }

  public static void remove(){
    USER_LOCAL.remove();
  }
}
