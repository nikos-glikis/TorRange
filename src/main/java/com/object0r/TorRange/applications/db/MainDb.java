package com.object0r.TorRange.applications.db;

import com.object0r.TorRange.ProxyWorkerManager;

public class MainDb
{

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("No session ini in arguments.");
            System.out.println("Usage: ");
            System.out.println("java -cp target/classes/;lib/* MainDb example.ini");

            System.exit(0);
        }

        ProxyWorkerManager dbRangeSimpleExampleManager = new DbProxyWorkerManagerExample(
                args[0],
                DbProxyWorkerExample.class
        );
        dbRangeSimpleExampleManager.startWorkers();

    }
}
