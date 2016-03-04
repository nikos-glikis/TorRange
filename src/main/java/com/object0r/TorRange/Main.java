package com.object0r.TorRange;


public class Main {

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
            TorWorkerManager torRangeSimpleExampleManager = new TorRangeSimpleExampleManagerTor(args[0]);

            System.out.println("Starting "+torRangeSimpleExampleManager.getThreadCount()+" Threads");
            for (int i = 0 ; i < torRangeSimpleExampleManager.getThreadCount(); i++)
            {
                new TorRangeSimpleExampleWorker( torRangeSimpleExampleManager, i).start();
                Thread.sleep(2000);
            }

            //ProxymityConnection proxymityConnection = new ProxymityConnection();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
