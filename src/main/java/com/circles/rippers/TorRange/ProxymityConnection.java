package com.circles.rippers.TorRange;


import java.net.InetSocketAddress;
import java.net.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ProxymityConnection extends ProxyConnection
{
    private ProxymityConnection()
    {

    }

    Connection dbConnection;
    String databaseName;
    String tableName = "proxymity_proxies";
    public void ProxymityConnection(Connection sqlConnection, String database)
    {
        this.dbConnection = sqlConnection;
        this.databaseName = database;
        changeIp();
    }

    @Override
    public void changeIp()
    {
        try
        {
            generateRanndomProxy();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void changeIp(String type) throws Exception
    {
        generateRanndomProxy(type);
    }

    public void changeIpHttps() throws Exception
    {
        generateRanndomProxy(ProxyInfo.PROXY_TYPES_HTTPS);
    }

    public void changeIpSocks() throws Exception
    {
        generateRanndomProxy("socks");
    }

    public void changeIpHttp() throws Exception
    {
        generateRanndomProxy(ProxyInfo.PROXY_TYPES_HTTP);
    }

    protected void generateRanndomProxy() throws Exception
    {
        generateRanndomProxy("any");
    }

    protected Proxy generateRanndomProxy(String type) throws Exception
    {

        proxyInfo = new ProxyInfo();
        String where = "1";

        if (type.equals(ProxyInfo.PROXY_TYPES_SOCKS4))
        {
            where = "  type= '"+ProxyInfo.PROXY_TYPES_SOCKS4+"' ";
        }
        else if (type.equals(ProxyInfo.PROXY_TYPES_SOCKS5))
        {
            where = "  type= '"+ProxyInfo.PROXY_TYPES_SOCKS5+"' ";
        }
        else if (type.equals(ProxyInfo.PROXY_TYPES_HTTP))
        {
            where = "  type= '"+ProxyInfo.PROXY_TYPES_HTTP+"' ";
        }
        else if (type.equals(ProxyInfo.PROXY_TYPES_HTTPS))
        {
            where = " https = 'yes' ";
        }
        else if (type.equals("socks"))
        {
            where = " ( type='" +ProxyInfo.PROXY_TYPES_SOCKS5+"' OR type='" +ProxyInfo.PROXY_TYPES_SOCKS5+"') ";
        }

        try
        {
            Statement st = dbConnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT host,port,type FROM "+databaseName+"." + tableName + " WHERE status = 'active' AND "+where+" ORDER BY RAND() LIMIT 1");
            if (rs.next())
            {

                String host = rs.getString(1);
                String port = rs.getString(2);
                String dbProxyType = rs.getString(3);
                proxyInfo.setHost(host);
                proxyInfo.setPort(port);

                st.close();

                Proxy.Type pType = null;

                if (dbProxyType.equals(ProxyInfo.PROXY_TYPES_SOCKS4)) {
                    proxyInfo.setType(ProxyInfo.PROXY_TYPES_SOCKS4);
                    pType = Proxy.Type.SOCKS;
                } else if (dbProxyType.equals(ProxyInfo.PROXY_TYPES_SOCKS5)) {
                    pType = Proxy.Type.SOCKS;
                    proxyInfo.setType(ProxyInfo.PROXY_TYPES_SOCKS5);
                } else if (dbProxyType.equals(ProxyInfo.PROXY_TYPES_HTTP)) {
                    pType = Proxy.Type.HTTP;
                    proxyInfo.setType(ProxyInfo.PROXY_TYPES_HTTP);
                } else if (dbProxyType.equals(ProxyInfo.PROXY_TYPES_HTTPS)) {
                    proxyInfo.setType(ProxyInfo.PROXY_TYPES_HTTP);
                    pType = Proxy.Type.HTTP;
                } else {
                    System.out.println("Else");
                    proxyInfo.setType(ProxyInfo.PROXY_TYPES_HTTP);
                    pType =Proxy.Type.HTTP;
                }
                proxy =new Proxy(pType, new InetSocketAddress(host, Integer.parseInt(port) ));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
        return null;
    }
}
