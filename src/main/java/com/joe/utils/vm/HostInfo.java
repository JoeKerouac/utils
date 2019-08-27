package com.joe.utils.vm;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 机器信息
 *
 * @author JoeKerouac
 * @version 2019年08月27日 17:29
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HostInfo {

    /**
     * hostName
     */
    private final String hostName;

    /**
     * hostAddress
     */
    private final String hostAddress;

    /**
     * 获取机器信息
     * @return 机器信息
     */
    private static HostInfo getInstance() {
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
