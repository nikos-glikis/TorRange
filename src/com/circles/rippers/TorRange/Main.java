package com.circles.rippers.TorRange;

import com.circles.rippers.TorRange.example.trueCaller.TrueCallerWorker;
import com.circles.rippers.TorRange.example.trueCaller.TrueCallerWorkerManager;

public class Main {

    public static void main(String[] args)
    {
        if (args.length == 0) {
            System.out.println("No session ini in arguments.");
            System.out.println("Usage: ");
            System.out.println("java -cp java -cp out/production/TorRange-ripper/:lib/* com.circles.rippers.TorRange.Main netherlands.ini");

            System.exit(0);
        }

        //TODO facebook ids from photos
        //TODO find out max from uploaded and continue from there.
        //TODO join secondRun with dbs_uploaded

        try
        {
            TrueCallerWorkerManager trueCallerWorkerManager = new TrueCallerWorkerManager(args[0]);
            //trueCallerWorkerManager.readOptions(args[0]);
            System.out.println("Starting "+trueCallerWorkerManager.getThreadCount()+" Threads");
            for (int i = 0 ; i < trueCallerWorkerManager.getThreadCount(); i++)
            {
                new TrueCallerWorker( trueCallerWorkerManager, i).start();
                Thread.sleep(2000);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
