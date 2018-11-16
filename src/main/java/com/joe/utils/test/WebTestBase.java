package com.joe.utils.test;

import java.util.concurrent.Callable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 提供基础的web测试环境，所有测试方法必须通过{@link #run(Runnable)}方法执行，否则不会初始化spring-web上下文，初始化的spring-web上
 * 下文的web端口号是随机的，可以通过{@link #getPort()}获取，可以通过{@link #getBaseUrl()}来获取baseUrl，同时可以覆
 * 写{@link #getSource()}方法来传进来一个source，可以覆写{@link #init()}方法来进行执行前的一些初始化工作，可以覆
 * 写{@link #destroy()}来进行执行完毕的上下文销毁工作
 * 
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月14日 23:03 JoeKerouac Exp $
 */
@Configuration
@EnableAutoConfiguration
public abstract class WebTestBase {
    private static ThreadLocal<Integer>                        portHolder    = new ThreadLocal<>();
    private static ThreadLocal<String>                         urlHolder = new ThreadLocal<>();
    private static ThreadLocal<ConfigurableApplicationContext> contextHolder = new ThreadLocal<>();

    /**
     * 运行测试用例
     * @param callable 测试用例函数
     * @param <T> 测试用例需要返回的数据类型
     * @return 测试用例返回的数据
     */
    protected <T> T run(Callable<T> callable) {
        init0();
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            destroy0();
        }
    }

    /**
     * 运行测试用例
     * @param runnable 测试用例函数
     */
    protected void run(Runnable runnable) {
        init0();
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            destroy0();
        }
    }

    /**
     * 获取端口
     * @return 端口号，需要在(0, 60000)的范围
     */
    protected int getPort() {
        return portHolder.get();
    }

    /**
     * 获取基础URL
     * @return 基础URL，示例：http://127.0.0.1:10/
     */
    protected String getBaseUrl() {
        return urlHolder.get();
    }

    /**
     * 初始化方法，执行run前会自动执行
     */
    protected void init() {

    }

    /**
     * 销毁方法，执行run后会自动执行
     */
    protected void destroy() {

    }

    /**
     * 获取Source
     * @return 默认为当前class
     */
    protected Class<?> getSource() {
        return this.getClass();
    }

    /**
     * 随机生成一个取值范围为[30000, 60000)的端口号码
     * @return 随机端口号
     */
    protected int randomPort() {
        return (int) (Math.random() * 30000) + 30000;
    }

    @Bean
    public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        int port = portHolder.get();
        if (port <= 0 || port >= 60000) {
            throw new RuntimeException("端口号不合法:" + port);
        }
        factory.setPort(port);
        return factory;
    }

    /**
     * 初始spring-web上下文
     */
    private void init0() {
        Class<?> source = getSource();
        //初始化端口号和url
        portHolder.set(randomPort());
        urlHolder.set("http://127.0.0.1:" + portHolder.get() + "/");
        contextHolder.set(SpringApplication.run(source));
        init();
    }

    /**
     * 销毁spring-web上下文，销毁数据
     */
    private void destroy0() {
        destroy();
        contextHolder.get().close();
        portHolder.remove();
    }
}
