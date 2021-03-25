package com.zcj.nettyServer.tools;

public interface IQueue<T> {
    /**
     * 初始化队列 构造一个空队列
     */
    IQueue initQueue();

    /**
     * 销毁队列
     */
    IQueue destroy();

    /**
     * 清空队列
     */
    IQueue clear();

    /**
     * 队列判空
     */
    Boolean isEmpty();

    /**
     * 返回队列长度
     */
    Integer getLength();

    /**
     * 返回队列头元素
     */
    T getHead();

    /**
     * 插入队尾元素
     */
    Boolean push(T e);

    /**
     * 删除队头元素  即出队
     */
    T pull();
}
