package com.object0r.TorRange.applications.bruteforce;

import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.TorWorker;

public class BruteForceWorker extends TorWorker
{
    public int threadCounter = 0;
    AbstractBruteForceWorkerManager manager;

    public BruteForceWorker(AbstractBruteForceWorkerManager manager, final int id)
    {
        super(manager, id);

    }

    public void process(String entry)
    {
        System.out.println("Entry: " + entry);

        try
        {
            System.out.println(readUrl("http://cpanel.com/showip.shtml"));

            //manager.markSuccessful(entry);

            sleep(1000);
            changeIp();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
