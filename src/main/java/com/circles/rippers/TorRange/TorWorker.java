package com.circles.rippers.TorRange;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

abstract public class TorWorker extends Thread
{
    protected ProxyConnection torConnection;
    public WorkerManager manager;
    protected int id;
    public Proxy proxy;

    int timeToSleepAfterKillTor = 30;

    public TorWorker(WorkerManager manager, int id)
    {
        this.manager = manager;
        this.id = id;
        torConnection = new TorConnection(WorkerManager.getTorRangeStart() + id);
    }


    public void changeIp()
    {
        torConnection.changeIp();
        initProxy();
    }


    public void initProxy()
    {
        if (manager.useTor)
        {
            proxy = torConnection.getProxy();
            /*ProxyInfo proxyInfo = torConnection.getProxyInfo();
            SocketAddress addr = new InetSocketAddress(proxyInfo.getHost(), Integer.parseInt(proxyInfo.getPort()));
            if (proxyInfo.getType().equals(ProxyInfo.PROXY_TYPES_SOCKS4) || proxyInfo.getType().equals(ProxyInfo.PROXY_TYPES_SOCKS5))
            {
                proxy = new Proxy(Proxy.Type.SOCKS, addr);
            }
            else
            {
                proxy = new Proxy(Proxy.Type.THT, addr);
            }*/

        }
        else
        {
            //SocketAddress addr = new InetSocketAddress("127.0.0.1", torConnection.getSocksPort());
            proxy = Proxy.NO_PROXY;
        }
    }

    public String readUrl(String url) throws Exception
    {
        if (manager.useTor)
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

        connection.setRequestProperty("User-Agent", getUserAgent());

        BufferedReader in = new BufferedReader( new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);
        in.close();

        return response.toString();
    }

    public String readUrlWithTor(String url)  throws Exception
    {
        return readUrl(url, proxy);
    }

    /**
     * Overwrite this if needed.
     * @return
     */
    private String getUserAgent()
    {
        return "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";
    }

    public void printTorPort()
    {
        ConsoleColors.printRed("Tor Socks Port: " + torConnection.getProxyInfo().getPort());
    }


    public void killTorProcess(int waitSeconds)
    {
        Scanner sc =null;
        try {
            manager.decreaseThreadCount();
            try {
                sc = new Scanner(new FileInputStream("/tmp/tor/" +torConnection.getProxyInfo().getPort() + "/my.pid"));
                int pid = Integer.parseInt(sc.nextLine());
                Runtime r = Runtime.getRuntime();
                Process p = r.exec("kill -9 " + pid);
                p.waitFor();
            } catch (Exception e) {
                ConsoleColors.printRed(e.toString());
            }

            FileUtils.deleteDirectory(new File("/tmp/tor/" + torConnection.getProxyInfo().getPort() + "/"));
            if (waitSeconds == 0) {
                waitSeconds =timeToSleepAfterKillTor;
            }
            ConsoleColors.printRed("Sleeping for "+waitSeconds);

            sleepSeconds(waitSeconds);
            manager.increaseThreadCount();
        } catch (Exception e) {
            if (sc!= null) {
                sc.close();
            }

            ConsoleColors.printRed(e.toString());
            manager.increaseThreadCount();
            //killYourSelf();
        }
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
            if (manager.useTor)
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

    protected abstract void process(String nextToProcess);

    void sleepSeconds(int seconds)
    {
        try {
            Thread.sleep(seconds*1000);
        } catch (Exception e) {
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

    public void simpleLog(String message)
    {
        this.manager.simpleLog(message);
    }
}
