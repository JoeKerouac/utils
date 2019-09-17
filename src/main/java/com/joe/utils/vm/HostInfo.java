package com.joe.utils.vm;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.joe.utils.concurrent.ThreadUtil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 机器信息
 *
 * @author JoeKerouac
 * @version 2019年08月27日 17:29
 */
@Data
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HostInfo {

    /**
     * HostInfo实例
     */
    private static volatile HostInfo INSTANCE;

    /**
     * hostName
     */
    private final String             hostName;

    /**
     * hostAddress
     */
    private final String             hostAddress;

    static {
        // 初始化
        INSTANCE = obtain();
        // 五分钟刷新一次
        Thread thread = new Thread(() -> {
            while (true) {
                ThreadUtil.sleep(5, TimeUnit.MINUTES);
                INSTANCE = obtain();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 获取默认网卡，有限获取
     * @return 默认网卡
     */
    private static HostInfo obtain() {
        try {
            // 获取所有网卡接口
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface
                .getNetworkInterfaces();
            List<InetGroup> list = new ArrayList<>();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                // 过滤掉P2P网络和本地网络（127.0.0.1）
                if (networkInterface.isPointToPoint() || networkInterface.isLoopback()) {
                    continue;
                }
                // 获取该网卡的所有ip地址（同一个网卡允许有多个IP）
                Enumeration<InetAddress> inetAddressEnumeration = networkInterface
                    .getInetAddresses();
                List<InetAddress> inetAddresses = new ArrayList<>();
                while (inetAddressEnumeration.hasMoreElements()) {
                    InetAddress inetAddress = inetAddressEnumeration.nextElement();
                    // 过滤掉ipV6
                    if (inetAddress instanceof Inet6Address) {
                        continue;
                    }

                    // 过滤掉通配地址（0.0.0.0）
                    if (inetAddress.isAnyLocalAddress()) {
                        continue;
                    }

                    try {
                        // 将网络不可达的排除
                        if (inetAddress.isReachable(100)) {
                            inetAddresses.add(inetAddress);
                        }
                    } catch (IOException e) {
                        log.debug("ip地址[{}:{}]不可达", inetAddress.getHostName(),
                            inetAddress.getHostAddress());
                    }
                }
                if (inetAddresses.size() > 0) {
                    list.add(new InetGroup(inetAddresses, networkInterface.getIndex()));
                }
            }

            if (list.size() > 0) {
                list.sort(HostInfo::compare);
                InetAddress inetAddress = list.get(0).inetAddressList.get(0);
                return new HostInfo(inetAddress.getHostName(), inetAddress.getHostAddress());
            }
        } catch (Throwable e) {
            log.debug("获取网卡异常", e);
        }
        return getLocal();
    }

    private static int compare(InetGroup o1, InetGroup o2) {
        return o1.index - o2.index;
    }

    /**
     * 网卡ip组
     */
    @Data
    @AllArgsConstructor
    private static class InetGroup {

        /**
         * 同一个网卡对应的多个ip
         */
        private List<InetAddress> inetAddressList;

        /**
         * 网卡index
         */
        private int               index;

    }

    /**
     * 获取机器信息
     * @return 机器信息
     */
    public static HostInfo getInstance() {
        return INSTANCE;
    }

    /**
     * 获取本地网络信息
     * @return 本地网络信息
     */
    private static HostInfo getLocal() {
        String hostName;
        String hostAddress;

        try {
            InetAddress localhost = InetAddress.getLocalHost();

            hostName = localhost.getHostName();
            hostAddress = localhost.getHostAddress();
        } catch (UnknownHostException e) {
            hostName = "localhost";
            hostAddress = "127.0.0.1";
        }
        return new HostInfo(hostName, hostAddress);
    }
}
