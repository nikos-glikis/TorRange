package com.circles.rippers.TorRange;


import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

abstract public class TorWorker extends Thread
{
    protected TorConnection torConnection;
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

    private TorWorker()
    {
        System.out.println("Error, TorWorker called without arguments.");
        System.exit(0);
    }

    public void changeIp()
    {
        torConnection.changeIp();
        initProxy();
    }


    public void initProxy()
    {
        SocketAddress addr = new InetSocketAddress("127.0.0.1", torConnection.getSocksPort());
        proxy = new Proxy(Proxy.Type.SOCKS, addr);
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

    public void printTorPort() {
        ConsoleColors.printRed("Tor Socks Port: " + torConnection.getSocksPort());
    }


    public void killTorProcess(int waitSeconds)
    {
        Scanner sc =null;
        try {
            manager.decreaseThreadCount();
            try {
                sc = new Scanner(new FileInputStream("/tmp/tor/" + torConnection.getSocksPort() + "/my.pid"));
                int pid = Integer.parseInt(sc.nextLine());
                Runtime r = Runtime.getRuntime();
                Process p = r.exec("kill -9 " + pid);
                p.waitFor();
            } catch (Exception e) {
                ConsoleColors.printRed(e.toString());
            }

            FileUtils.deleteDirectory(new File("/tmp/tor/" + torConnection.getSocksPort() + "/"));
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
            changeIp();

            if (proxy == null)
            {
                initProxy();
            }


            while (true)
            {

                String nextToProcess = manager.getNextEntry();
                process(nextToProcess);
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
}
