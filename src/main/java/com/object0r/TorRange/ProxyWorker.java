package com.object0r.TorRange;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

abstract public class ProxyWorker extends  Thread
{
    protected Proxy proxy;
    protected ProxyWorkerManager manager;
    protected ProxyConnection proxyConnection;
    protected boolean isActive = true;

    protected int id;

    public ProxyWorker(ProxyWorkerManager manager, int id)
    {
        this.manager = manager;
        manager.registerWorker(this);
        this.id = id;
    }

    public Proxy getProxy()
    {
        return proxy;
    }

    public void changeIp()
    {
        proxyConnection.changeIp();
        try
        {
            Thread.sleep(3000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        initProxy();
    }

    public void verifyTor()
    {
        new Thread()
        {
            public void run()
            {
                isActive = false;
                ((TorConnection) proxyConnection).connect();
                while (true)
                {

                    SocketAddress addr = new InetSocketAddress("localhost", ((TorConnection) proxyConnection).getSocksPort());

                    //Try to connect.
                    //Stops here until connection is established.
                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, addr);
                    try
                    {
                        URLConnection uc = new URL("https://www.yahoo.com/").openConnection(proxy);
                        Scanner sc = new Scanner(uc.getInputStream());
                        while (sc.hasNext())
                        {
                            sc.nextLine();
                        }
                        break;
                    } catch (Exception e)
                    {
                        //e.printStackTrace();
                        System.out.println("Tor connection is not yet established. Trying again in 5 seconds.");
                        try { Thread.sleep(5000);} catch (Exception ee){ }
                    }
                }
                System.out.println("Tor ("+id+") is up and running.");
                isActive = true;
            }
        }.start();
        while(!isActive) {
            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void initProxy()
    {
        if (manager.useProxy)
        {
            proxy = proxyConnection.getProxy();
        }
        else
        {
            //SocketAddress addr = new InetSocketAddress("127.0.0.1", proxyConnection.getSocksPort());
            proxy = Proxy.NO_PROXY;
        }
    }

    public String readUrl(String url) throws Exception
    {
        if (manager.useProxy)
        {
            return readUrl(url, proxy);
        }
        else
        {
            return readUrl(url, null);
        }
    }

    public String readUrl(String url, Proxy proxy) throws Exception
    {
        return readUrl(url, proxy, 20,20,0,0);
    }

    public String readUrl(String url, Proxy proxy, int readTimeoutSeconds, int connectTimeoutSeconds, int tries, int maxRetries) throws Exception
    {
        try
        {
            URL website = new URL(url);

            URLConnection connection = null;
            if (proxy != null)
            {
                connection = website.openConnection(proxy);
            }
            else
            {
                connection = website.openConnection();
            }
            connection.setReadTimeout(readTimeoutSeconds*1000);
            connection.setConnectTimeout(connectTimeoutSeconds*1000);

            connection.setRequestProperty("User-Agent", getUserAgent());

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();

            return response.toString();
        }
        catch (SocketTimeoutException e)
        {
            if (tries > maxRetries)
            {
                if (proxy!=null)
                {
                    changeIp();
                }
                return readUrl(url, proxy, readTimeoutSeconds, connectTimeoutSeconds, tries+1, maxRetries);
            }
            else
            {
                throw e;
            }
        }
    }

    public ProxyInfo getProxyInfo()
    {
        return proxyConnection.getProxyInfo();
    }

    protected abstract void process(String nextToProcess);

    /**
     * Overwrite this if needed.
     * @return
     */
    public String getUserAgent()
    {
        return "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";
    }

    public String readUrlWithProxy(String url)  throws Exception
    {
        return readUrl(url, proxy, 20,20,0,0);
    }

    public void run()
    {
        try
        {
            if (manager == null) {
                System.out.println("Manager is null");
                System.exit(0);
            }
            manager.increaseThreadCount();
            if (manager.useProxy)
            {
                changeIp();
            }

            if (proxy == null)
            {
                initProxy();
            }


            while (true)
            {

                String nextToProcess = manager.getNextEntry();
                if (!isActive)
                {
                    Thread.sleep(5000);
                    continue;
                }
                process(nextToProcess);
                if (manager.exiting)
                {
                    Thread.sleep(60000000);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public ReadUrlResult resilientReadUrl(String url)
    {
        //TODO research what happens in https redirect, and www
        //System.out.print(".");
        String contents = null;
        ReadUrlResult readUrlResult = new ReadUrlResult();

        try
        {
            URL oracle = new URL(url);
            HttpURLConnection connection = (HttpURLConnection )oracle.openConnection(proxy);


            InputStream is;
            if (connection.getHeaderField("Content-Encoding") != null && connection.getHeaderField("Content-Encoding").equals("gzip"))
            {
                //System.out.println("Gzip ole");
                byte[] buffer = new byte[1024];
                //GZIPInputStream  gzip = new GZIPInputStream (new ByteArrayInputStream (tBytes));
                GZIPInputStream gzis = new GZIPInputStream(connection.getInputStream());
                is = gzis;
            }
            else
            {
                is = connection.getInputStream();
            }
            //BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            byte[] bytes = IOUtils.toByteArray(is);
            in.close();
            readUrlResult.setBody(bytes);
            readUrlResult.setSuccessful(true);
        }
        catch (FileNotFoundException e)
        {
            readUrlResult.setException(e);
        }
        catch (IllegalArgumentException e)
        {
            readUrlResult.setException(e);
        }
        catch (UnknownHostException e)
        {
            readUrlResult.setException(e);
            //return RESILIENT_UNKNOWN_HOST_EXCEPTION;
        }
        catch (IOException e)
        {
            readUrlResult.setException(e);
            //e.printStackTrace();
        }
        catch (Exception e)
        {
            readUrlResult.setException(e);
            //This should never happen.
            e.printStackTrace();
            System.exit(0);
        }
        return readUrlResult;
    }

    void sleepSeconds(int seconds)
    {
        try {
            Thread.sleep(seconds*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void shutDown();

    public synchronized  void _shutDown()
    {
        isActive = false;
        shutDown();

    }

    public void simpleLog(String message)
    {
        this.manager.simpleLog(message);
    }
}
