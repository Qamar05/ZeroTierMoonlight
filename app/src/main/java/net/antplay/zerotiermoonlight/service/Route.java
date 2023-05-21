package net.antplay.zerotiermoonlight.service;


import net.antplay.zerotiermoonlight.util.InetAddressUtils;

import java.net.InetAddress;

import lombok.Data;

/**
 * 路由记录数据类
 */
@Data
public class Route {
    private final InetAddress address;
    private final int prefix;
    private InetAddress gateway = null;

    public Route(InetAddress address, int prefix) {
        this.address = address;
        this.prefix = prefix;
    }


    public boolean belongsToRoute(InetAddress inetAddress) {
        return this.address.equals(InetAddressUtils.addressToRoute(inetAddress, this.prefix));
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPrefix() {
        return prefix;
    }

    public InetAddress getGateway() {
        return gateway;
    }

    public void setGateway(InetAddress gateway) {
        this.gateway = gateway;
    }
}
