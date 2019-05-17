package com.joe.utils.concurrent;

/**
 * 自定义线程，其中保存有父线程
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月09日 19:54 JoeKerouac Exp $
 */
public class CustomThread extends Thread {

    private Thread parent;

    public CustomThread() {
        super();
    }

    public CustomThread(Runnable target) {
        super(target);
    }

    public CustomThread(ThreadGroup group, Runnable target) {
        super(group, target);
    }

    public CustomThread(String name) {
        super(name);
    }

    public CustomThread(ThreadGroup group, String name) {
        super(group, name);
    }

    public CustomThread(Runnable target, String name) {
        super(target, name);
    }

    public CustomThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
    }

    public CustomThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    @Override
    public synchronized void start() {
        this.parent = Thread.currentThread();
        super.start();
    }

    public Thread getParent() {
        return parent;
    }
}
