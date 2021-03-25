package com.zcj.nettyServer.tools;

public class LinkedQueue<T> implements IQueue{

    private LNode Header;//头指针 指向头节点  即队首元素的前一个位置 （作用 方便删除队首元素，方便判断队列是否满）
    private LNode TaillPoint;//尾指针
    private Integer size;

    @Override
    public IQueue initQueue() {
        if (Header == null) {
            Header = new LNode<T>(); //实例化头节点
            //头尾指针均指向头节点
            TaillPoint = null;
            TaillPoint = Header;
            size = 0;
        }
        return this;
    }

    @Override
    public IQueue destroy() {
        //销毁
        while (Header!=null){
            LNode e =  Header;
            Header = e.getNext();
            e.clearNode();
        }
        return this;
    }

    @Override
    public IQueue clear() {
        //头尾指针均指向头节点
        this.destroy();
        this.initQueue();
        return this;
    }

    @Override
    public Boolean isEmpty() {
        if (size == 0) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public Integer getLength() {
        return size;
    }

    @Override
    public T getHead() {
        if (isEmpty()){
            return null;
        }
        return (T) Header.getNext().getData();
    }

    @Override
    public Boolean push(Object e) {
        updateLQueue(1,e);
        return Boolean.TRUE;
    }

    @Override
    public T pull() {
        //删除的时候 如果是最后一个元素
        if (isEmpty()) {
            return null;
        }

        return updateLQueue(2,null);
    }

    private synchronized T updateLQueue(int type, Object e){
        T ret = null;
        if (type == 1){
            //入队 从队尾入
            LNode newNode = new LNode<T>((T) e, null);
            TaillPoint.setNext(newNode);
            TaillPoint = newNode;
            size++;
        }else if (type == 2){
            //出队列
            ret = (T) Header.getNext().getData();
            Header.setNext(Header.getNext().getNext());
            size--;
            if (size == 0){
                //已经空了的情况下，需要将尾指针重新指向头指针，以免再次插入之后无法关联
                TaillPoint = Header;
            }
        }
        return ret;
    }

}
