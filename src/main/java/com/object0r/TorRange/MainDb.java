package com.object0r.TorRange;


public class MainDb
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
            ProxyWorkerManager torRangeSimpleExampleManager = new DbProxyWorkerManagerExample(args[0], TorRangeSimpleExampleWorker.class);
            torRangeSimpleExampleManager.startWorkers();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
