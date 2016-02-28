package com.object0r.TorRange;


import java.net.Proxy;

abstract class ProxyConnection
{
    Proxy proxy;
    ProxyInfo proxyInfo;

    /** This must generate a new proxy and proxyInfo **/
    abstract public void changeIp();

    public Proxy getProxy()
    {
        return proxy;
    }

    public ProxyInfo getProxyInfo()
    {
        return proxyInfo;
    }


}
