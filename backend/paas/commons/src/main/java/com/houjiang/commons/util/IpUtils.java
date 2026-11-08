package com.houjiang.commons.util;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * @author Amos
 */
public class IpUtils {

    /** 获取用户真实ip */
    public  static String getIpAddress() throws UnknownHostException {

        String hostAddress = Inet4Address.getLocalHost().getHostAddress();

        return hostAddress;
    }
}
