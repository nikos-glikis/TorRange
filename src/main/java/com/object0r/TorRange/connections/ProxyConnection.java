package com.object0r.TorRange.connections;


import com.object0r.TorRange.datatypes.ProxyInfo;

import java.net.Proxy;

abstract public class ProxyConnection
{
    Proxy proxy = Proxy.NO_PROXY;
    ProxyInfo proxyInfo;

    abstract public void close();

    /**
     * This must generate a new proxy and proxyInfo
     **/
    abstract public void changeIp();


    /**
     * This must generate a new proxy and proxyInfo, that support https
     **/
    abstract public void changeIpHttps() throws Exception;

    public Proxy getProxy()
    {
        if (proxy == null)
        {
            return Proxy.NO_PROXY;
        }
        return proxy;
    }

    public ProxyInfo getProxyInfo()
    {
        return proxyInfo;
    }
}
