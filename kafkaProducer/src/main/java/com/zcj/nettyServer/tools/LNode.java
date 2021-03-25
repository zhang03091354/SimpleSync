package com.zcj.nettyServer.tools;

public class LNode<T> {
    private T data;
    private LNode next;

    public LNode() {

    }

    public void clearNode(){
        this.next = null;
        this.data = null;
    }

    public LNode(T data, LNode next) {
        this.data = data;
        this.next = next;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LNode getNext() {
        return next;
    }

    public void setNext(LNode next) {
        this.next = next;
    }
}
