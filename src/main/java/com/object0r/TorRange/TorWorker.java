package com.object0r.TorRange;


import java.net.*;
import java.util.Scanner;

abstract public class TorWorker extends ProxyWorker
{

    int timeToSleepAfterKillTor = 30;

    public TorWorker(WorkerManager manager, final int id)
    {
        super(manager, id);
        proxyConnection = new TorConnection(WorkerManager.getTorRangeStart() + id);
        if (manager.useTor())
        {
            new Thread(){
                public void run()
                {
                    isActive = false;
                    ((TorConnection) proxyConnection).connect();
                    while (true)
                    {

                        SocketAddress addr = new
                                InetSocketAddress("localhost", ((TorConnection) proxyConnection).getSocksPort());

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
        }
    }

    @Override
    public synchronized void shutDown()
    {
        System.out.println("Closing tor connection ...");
        ((TorConnection) proxyConnection).closeTor();
    }
}
