package com.joe.utils.test;

import java.util.concurrent.Callable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.exception.UtilsException;
import com.joe.utils.function.CustomFunction;

/**
 * 提供基础的web测试环境，所有测试方法必须通过{@link #runCase(CustomFunction)}方法或者{@link #runCase(Callable)}方法执行，否则不
 * 会初始化spring-web上下文，初始化的spring-web上下文的web端口号是随机的，可以通过{@link #getPort()}获取，可以通
 * 过{@link #getBaseUrl()}来获取baseUrl，同时可以覆写{@link #getSource()}方法来传进来一个source，可以覆写{@link #init()}方法
 * 来进行执行前的一些初始化工作，可以覆写{@link #destroy()}来进行执行完毕的上下文销毁工作
 * 
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月14日 23:03 JoeKerouac Exp $
 */
@Configuration
@EnableAutoConfiguration
public abstract class WebBaseTest extends BaseTest {

    /**
     * 当前运行的web程序的端口号
     */
    private static ThreadLocal<Integer> PORT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 当前运行的web程序的url
     */
    private static ThreadLocal<String> URL_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 当前运行的web程序的spring上下文
     */
    private static ThreadLocal<ConfigurableApplicationContext> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 是否初始化，true表示已经初始化
     */
    private volatile boolean init;

    private final Object lock = new Object();

    /**
     * 获取端口
     * 
     * @return 端口号，需要在(0, 60000)的范围
     */
    protected int getPort() {
        return PORT_THREAD_LOCAL.get();
    }

    /**
     * 获取基础URL
     * 
     * @return 基础URL，示例：http://127.0.0.1:10/
     */
    protected String getBaseUrl() {
        return URL_THREAD_LOCAL.get();
    }

    /**
     * 获取Source
     * 
     * @return 默认为当前class
     */
    protected Class<?>[] getSource() {
        return new Class[] {this.getClass()};
    }

    /**
     * 随机生成一个取值范围为[30000, 60000)的端口号码
     * 
     * @return 随机端口号
     */
    protected int randomPort() {
        return (int)(Math.random() * 30000) + 30000;
    }

    @Bean
    public ServletWebServerFactory embeddedServletContainerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        int port = PORT_THREAD_LOCAL.get();
        if (port <= 0 || port >= 60000) {
            throw new RuntimeException("端口号不合法:" + port);
        }
        factory.setPort(port);
        return factory;
    }

    /**
     * 初始spring-web上下文
     */
    @Override
    protected void init() {
        if (init) {
            return;
        }
        synchronized (lock) {
            if (init) {
                return;
            }
            try {
                Class<?>[] source = getSource();
                // 初始化端口号和url
                PORT_THREAD_LOCAL.set(randomPort());
                URL_THREAD_LOCAL.set("http://127.0.0.1:" + PORT_THREAD_LOCAL.get() + "/");
                CONTEXT_THREAD_LOCAL
                    .set(SpringApplication.run(CollectionUtil.addTo(WebBaseTest.class, source), new String[0]));
            } catch (Throwable e) {
                throw new UtilsException(e, "初始化异常");
            }
            init = true;
        }
    }

    /**
     * 销毁spring-web上下文，销毁数据
     */
    @Override
    protected void destroy() {
        PORT_THREAD_LOCAL.remove();
        if (CONTEXT_THREAD_LOCAL.get() != null) {
            synchronized (lock) {
                if (CONTEXT_THREAD_LOCAL.get() != null) {
                    CONTEXT_THREAD_LOCAL.get().close();
                    CONTEXT_THREAD_LOCAL.remove();
                }
            }
        }
    }
}
