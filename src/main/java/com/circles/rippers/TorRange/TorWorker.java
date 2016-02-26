package com.circles.rippers.TorRange;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

abstract public class TorWorker extends ProxyWorker
{

    int timeToSleepAfterKillTor = 30;

    public TorWorker(WorkerManager manager, int id)
    {
        super(manager, id);
        proxyConnection = new TorConnection(WorkerManager.getTorRangeStart() + id);
    }


    /**
     * @deprecated
     */
/*    public void printTorPort()
    {
        ConsoleColors.printRed("Tor Socks Port: " + proxyConnection.getProxyInfo().getPort());
    }*/


    /**
     * @deprecated
     * @param waitSeconds
     */
    /*public void killTorProcess(int waitSeconds)
    {
        Scanner sc =null;
        try {
            manager.decreaseThreadCount();
            try {
                sc = new Scanner(new FileInputStream("/tmp/tor/" + proxyConnection.getProxyInfo().getPort() + "/my.pid"));
                int pid = Integer.parseInt(sc.nextLine());
                Runtime r = Runtime.getRuntime();
                Process p = r.exec("kill -9 " + pid);
                p.waitFor();
            } catch (Exception e) {
                ConsoleColors.printRed(e.toString());
            }

            FileUtils.deleteDirectory(new File("/tmp/tor/" + proxyConnection.getProxyInfo().getPort() + "/"));
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
    }*/






}
