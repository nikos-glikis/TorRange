package com.object0r.TorRange.applications.range;


import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.applications.range.TorRangeSimpleExampleManagerProxy;
import com.object0r.TorRange.applications.range.TorRangeSimpleExampleWorker;

public class Main
{

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("No session ini in arguments.");
            System.out.println("Usage: ");
            System.out.println("java -cp target/classes/;lib/* Main example.ini");

            System.exit(0);
        }

        try
        {
            ProxyWorkerManager torRangeSimpleExampleManager = new TorRangeSimpleExampleManagerProxy(args[0], TorRangeSimpleExampleWorker.class);
            torRangeSimpleExampleManager.startWorkers();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
