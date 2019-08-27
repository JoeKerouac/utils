package com.joe.utils.telnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.joe.utils.common.Assert;
import com.joe.utils.concurrent.ThreadUtil;
import com.joe.utils.exception.IOExceptionWrapper;
import com.joe.utils.exception.TelnetException;

import lombok.extern.slf4j.Slf4j;

/**
 * telnet服务端，用于构建telnet服务器（参照蚂蚁金服sofa-ark内部的telnet服务端）
 *
 * @author joe
 * @version 2018.07.19 18:27
 */
@Slf4j
public class TelnetServer {
    private static final int     SELECT_TIME_GAP = 1000;

    private AtomicBoolean        shutdown        = new AtomicBoolean(true);

    private int                  port            = 1234;

    private Selector             selector        = null;

    private ServerSocketChannel  acceptorSvr     = null;

    private String               host            = null;
    /**
     * kill命令hook
     */
    private Thread               shutdownHook;

    /**
     * 命令处理器
     */
    private final CommandHandler handler;

    public TelnetServer(CommandHandler handler) {
        this(1234, handler);
    }

    public TelnetServer(int port, CommandHandler handler) {
        this(null, port, handler);
    }

    public TelnetServer(String host, int port, CommandHandler handler) {
        Assert.isTrue(port > 0, "port must be positive number");
        Assert.notNull(handler, "commondHandler must not be null");
        this.host = host;
        this.port = port;
        this.handler = handler;
        this.shutdownHook = new Thread(this::shutdown);
    }

    /**
     * 启动telnet服务端
     */
    public void start() {
        if (shutdown.compareAndSet(true, false)) {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            try {
                selector = Selector.open();
                acceptorSvr = ServerSocketChannel.open();
                if (host == null) {
                    acceptorSvr.socket().bind(new InetSocketAddress(port));
                } else {
                    acceptorSvr.socket()
                        .bind(new InetSocketAddress(InetAddress.getByName(host), port));
                }
                acceptorSvr.configureBlocking(false);
                acceptorSvr.register(selector, SelectionKey.OP_ACCEPT);
                log.info(
                    "Listening on port: " + Integer.toString(acceptorSvr.socket().getLocalPort()));

                Runnable action = () -> {
                    while (!shutdown.get()) {
                        try {
                            selector.select(SELECT_TIME_GAP);
                            Set<SelectionKey> selectionKeys = selector.selectedKeys();
                            Iterator<SelectionKey> it = selectionKeys.iterator();
                            while (it.hasNext()) {
                                SelectionKey key = it.next();
                                it.remove();
                                try {
                                    handlerSelectionKey(key);
                                } catch (Throwable t) {
                                    if (key != null) {
                                        key.cancel();
                                        if (key.channel() != null) {
                                            key.channel().close();
                                        }
                                    }
                                    log.error("An error occurs in telnet session.", t);
                                }
                            }
                        } catch (Throwable t) {
                            log.error("An error occurs in telnet server.", t);
                            if (shutdown.get()) {
                                break;
                            }
                        }
                    }
                };

                ThreadUtil.createPool(ThreadUtil.PoolType.Singleton, "telnet线程-%d").execute(action);
            } catch (IOException e) {
                log.error("Unable to open telnet.", e);
                throw new IOExceptionWrapper(e);
            }
        }
    }

    /**
     * 关闭telnet服务端
     */
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            try {
                acceptorSvr.close();
                selector.close();
            } catch (Throwable t) {
                log.error("An error occurs when shutdown telnet server.", t);
                throw new TelnetException(t);
            }
        }
    }

    private void handlerSelectionKey(SelectionKey key) throws IOException {
        if (!key.isValid()) {
            return;
        } else if (key.isAcceptable()) {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ, new TelnetProtocolHandler(sc, handler));
            sc.write(ByteBuffer.wrap(TelnetProtocolHandler.NEGOTIATION_MESSAGE));
        } else if (key.isReadable()) {
            TelnetProtocolHandler telnetProtocolHandler = (TelnetProtocolHandler) key.attachment();
            telnetProtocolHandler.handle();
        }
    }
}
