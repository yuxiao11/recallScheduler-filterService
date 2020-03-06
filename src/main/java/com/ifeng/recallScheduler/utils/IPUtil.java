package com.ifeng.recallScheduler.utils;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class IPUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(IPUtil.class);


    private final static String IP_SEPARATOR = ",";

    private static String localIp;

    /**
     * return 机器名
     */
    public static String getHostName() {
        try {
            InetAddress ia = InetAddress.getByName("127.0.0.1");
            return ia.getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    /**
     * 判断操作系统是否是Windows
     *
     * @return
     */
    public static boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }
    
    
    /**
     * 获取Linux下的IP地址
     *
     * @return IP地址
     * @throws SocketException
     */
    public static String getLinuxLocalIp() {
        if (StringUtils.isNotBlank(localIp)) return localIp;
        String ip = "";
        if(isWindowsOS()){
            localIp = getHostName();
        	return getHostName();
        }
        
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ipAddress = inetAddress.getHostAddress().toString();
                            if (!ipAddress.contains("::") && !ipAddress.contains("0:0:") && !ipAddress.contains("fe80")) {
                                ip = ipAddress;
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            logger.error("获取ip地址异常");
            ip = "127.0.0.1";
            ex.printStackTrace();
        }
        localIp = ip;

        logger.info("IP:"+ip);
        return ip;
    }

    public static String[] getIps() {

        List<String> ips = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets))
                if (netint.getHardwareAddress() != null) {
                    List<InterfaceAddress> list = netint.getInterfaceAddresses();
                    for (InterfaceAddress interfaceAddress : list) {
                        String localIp = interfaceAddress.getAddress().getHostAddress();
                        if (StringUtils.isNotBlank(localIp)) {
                            ips.add(localIp);
                        }
                    }
                }
        } catch (SocketException e1) {
            return null;
        }
        return ips.toArray(new String[ips.size()]);
    }

    /**不建议使用，未检测是否可用
     * 判断是否开发或测试机 (Hard Code)
     * 
     * @param
     * @return
     */
    @Deprecated
    public static boolean isDev() {
        String[] ips = getIps();
        if (ips == null) {
            return false;
        }
        for (String ip : ips) {
            if (ip.startsWith("10.10")) {
                return true;
            }
            if (ip.startsWith("192.168")) {
                return true;
            }
        }
        //		String hostname = getHostName();
        //		for (String devHost : devHosts) {
        //			if (hostname.equalsIgnoreCase(devHost)) {
        //				return true;
        //			}
        //		}
        return false;
    }

    public static String getIP(HttpServletRequest request) {

        String ip = request.getHeader("X-Real-IP");

        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-forwarded-for");
        } else {
            return ip;
        }

        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        } else {
            //当有多级反向代理时，x-forwarded-for值为多个时取第一个ip地址
            if (ip.indexOf(IP_SEPARATOR) != -1) {
                ip = ip.substring(0, ip.indexOf(IP_SEPARATOR));
            }
            return ip;
        }

        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        } else {
            return ip;
        }

        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            return ip;
        }

        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = "";
        }
        return ip;
    }
    
    
    	public static void main(String[] args) {
            System.out.println(getLinuxLocalIp());
            System.out.println(getLinuxLocalIp());
            System.out.println(getLinuxLocalIp());
            System.out.println(getLinuxLocalIp());
    	}
}
