package com.object0r.TorRange.applications.bruteforce;

import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.TorWorker;

public class BruteForceWorker extends TorWorker
{
    public int threadCounter = 0;
    AbstractBruteForceWorkerManager manager;

    public BruteForceWorker(ProxyWorkerManager manager, final int id)
    {
        super(manager, id);

    }

    public void process(String entry)
    {
        System.out.println("Entry: " + entry);
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
