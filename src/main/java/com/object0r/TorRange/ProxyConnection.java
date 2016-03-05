package com.object0r.TorRange;


import java.net.Proxy;

abstract public class ProxyConnection
{
    Proxy proxy;
    ProxyInfo proxyInfo;

    /** This must generate a new proxy and proxyInfo **/
    abstract public void changeIp();


    /** This must generate a new proxy and proxyInfo, that support https **/
    abstract public void changeIpHttps() throws Exception;

    public Proxy getProxy()
    {
        return proxy;
    }

    public ProxyInfo getProxyInfo()
    {
        return proxyInfo;
    }


}
