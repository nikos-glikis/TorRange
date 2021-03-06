package com.object0r.TorRange;

import com.object0r.TorRange.connections.ProxyConnection;
import com.object0r.TorRange.connections.TorConnection;
import com.object0r.TorRange.datatypes.ProxyInfo;
import com.object0r.TorRange.datatypes.ReadUrlResult;
import com.object0r.toortools.Utilities;
import com.object0r.toortools.http.HttpRequestInformation;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

abstract public class ProxyWorker extends Thread
{
    protected Proxy proxy = Proxy.NO_PROXY;
    protected ProxyWorkerManager manager;
    protected ProxyConnection proxyConnection;

    //weather or not the worker is active
    protected boolean isActive = true;
    //if the worker is ready to start processing things (for example false if waiting for tor to connect)
    protected boolean isReady = true;
    private boolean isTurnedOff = false;

    //requests per minute.
    static int frequencies[] = new int[60];
    static int lastMinute = 0;
    Calendar frequenciesCalendar;

    /**
     * Used to check from manager if thread is running. Only for manager checks. Does not mean anything.
     */
    private boolean isIdle = false;

    protected int workerId;

    public ProxyWorker(ProxyWorkerManager manager, int workerId)
    {
        this.manager = manager;
        this.workerId = workerId;
    }

    public Proxy getProxy()
    {
        return proxy = proxyConnection.getProxy();
    }

    public void changeIp()
    {
        if (manager.useProxy())
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
            verifyTor(false);
            initProxy();
        }
    }

    public void run()
    {
        try
        {
            if (manager == null)
            {
                System.out.println("Manager is null");
                System.exit(0);
            }

            if (manager.useProxy)
            {
                changeIp();
            }

            if (proxy == null)
            {
                initProxy();
            }

            while (!isTurnedOff)
            {
                updateFrequencies();
                if (!isActive || !isReady)
                {
                    Thread.sleep(5000);
                    continue;
                }
                if (!getNextAndProcess())
                {
                    _shutDown();
                    return;
                }
                setIdle(false);
                if (manager.exiting)
                {
                    _shutDown();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void updateFrequencies()
    {
        frequenciesCalendar = Calendar.getInstance();
        int currentMinute = frequenciesCalendar.get(Calendar.MINUTE);

        if (lastMinute != currentMinute)
        {
            synchronized (this)
            {
                int requestsPerMinute = 0;
                for (int num : frequencies)
                {
                    requestsPerMinute += num;
                }
                manager.setRequestsPerMinute(requestsPerMinute);
                frequencies = new int[60];
            }
            lastMinute = currentMinute;
        }
        // frequencies points to current second at this point
        int requestSecond = frequenciesCalendar.get(Calendar.SECOND);
        frequencies[requestSecond]++;
    }

    /**
     * Returns true if there is indeed next to process and false when response is false.
     *
     * @return boolean
     */
    protected boolean getNextAndProcess()
    {
        String nextToProcess = manager.getNextEntry();
        if (nextToProcess == null)
        {
            _shutDown();
            return false;
        }
        setIdle(false);
        process(nextToProcess);
        return true;
    }

    public void verifyTor(final boolean killOld)
    {
        Thread t = new Thread()
        {
            public void run()
            {
                isReady = false;
                if (killOld)
                {
                    ((TorConnection) proxyConnection).connect();
                }
                while (true)
                {

                    try
                    {
                        SocketAddress addr = new InetSocketAddress("localhost", ((TorConnection) proxyConnection).getSocksPort());
                        //Try to connect.
                        //Stops here until connection is established.
                        Proxy proxy = new Proxy(Proxy.Type.SOCKS, addr);
                        URLConnection uc = new URL("https://www.yahoo.com/").openConnection(proxy);
                        uc.setReadTimeout(20000);
                        uc.setConnectTimeout(20000);
                        Scanner sc = new Scanner(uc.getInputStream());
                        while (sc.hasNext())
                        {
                            sc.nextLine();
                        }
                        System.out.println("Tor (" + workerId + ") is up and running.");
                        isReady = true;
                        break;
                    }
                    catch (Exception e)
                    {
                        //e.printStackTrace();
                        //System.out.println("Tor connection is not yet established. Trying again in 5 seconds.");
                        try
                        {
                            Thread.sleep(5000);
                        }
                        catch (Exception ee)
                        {
                            ee.printStackTrace();
                        }
                    }

                }
            }
        };
        t.start();
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
        return readUrl(url, proxy, 20, 20, 0, 0);
    }

    public String readUrl(String url, Proxy proxy, int readTimeoutSeconds, int connectTimeoutSeconds, int tries, int maxRetries) throws Exception
    {
        try
        {
            URL website = new URL(url);

            URLConnection connection;
            if (proxy != null)
            {
                connection = website.openConnection(proxy);
            }
            else
            {
                connection = website.openConnection();
            }
            connection.setReadTimeout(readTimeoutSeconds * 1000);
            connection.setConnectTimeout(connectTimeoutSeconds * 1000);

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
                if (proxy != null)
                {
                    changeIp();
                }
                return readUrl(url, proxy, readTimeoutSeconds, connectTimeoutSeconds, tries + 1, maxRetries);
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
     *
     * @return String current viable user browser
     */
    public String getUserAgent()
    {
        return Utilities.getBrowserUserAgent();
    }

    public String readUrlWithProxy(String url) throws Exception
    {
        return readUrl(url, proxy, 20, 20, 0, 0);
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
            HttpURLConnection connection = (HttpURLConnection) oracle.openConnection(proxy);
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);

            InputStream is;
            if (connection.getHeaderField("Content-Encoding") != null && connection.getHeaderField("Content-Encoding").equals("gzip"))
            {
                //System.out.println("Gzip ole");
                byte[] buffer = new byte[1024];
                //GZIPInputStream  gzip = new GZIPInputStream (new ByteArrayInputStream (tBytes));
                is = new GZIPInputStream(connection.getInputStream());
            }
            else
            {
                is = connection.getInputStream();
            }
            //BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            byte[] bytes = IOUtils.toByteArray(is);

            readUrlResult.setBody(bytes);
            readUrlResult.setSuccessful(true);
            in.close();
            is.close();
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
        try
        {
            Thread.sleep(seconds * 1000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public abstract void shutDown();

    public synchronized void _shutDown()
    {
        isActive = false;
        isTurnedOff = true;
        isReady = false;
        shutDown();
    }

    public boolean isActive()
    {
        return isActive;
    }

    public void setActive(boolean active)
    {
        isActive = active;
    }

    public void simpleLog(String message)
    {
        this.manager.simpleLog(message);
    }

    public boolean isReady()
    {
        return isReady;
    }

    public void setReady(boolean ready)
    {
        isReady = ready;
    }

    public boolean isIdle()
    {
        return isIdle;
    }

    public void setIdle(boolean idle)
    {
        isIdle = idle;
    }

    public int getWorkerId()
    {
        return workerId;
    }

    /**
     * Returns a new com.object0r.toortools.http.HttpRequestInformation object armed with the proxy.
     *
     * @return returns HttpRequestInformation with proxy already set.
     */
    public HttpRequestInformation getNewHttpRequestInformation()
    {
        HttpRequestInformation httpRequestInformation = new HttpRequestInformation();
        httpRequestInformation.setProxy(getProxy());
        return httpRequestInformation;
    }
}
