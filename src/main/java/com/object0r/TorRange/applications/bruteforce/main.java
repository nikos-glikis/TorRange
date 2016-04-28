package com.object0r.TorRange.applications.bruteforce;

import com.object0r.TorRange.applications.wordlist.WordlistConsumerWorker;
import com.object0r.TorRange.applications.wordlist.WordlistConsumerWorkerManager;

public class main
{
    public static void main(String args[])
    {
        //run this with wordlistconsumer as input
        /**
         * Requires:
             [wordlist]
             passwordfile=C:\\wordlists\\rockyou.txt

         In ini.
         */
        try
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
                BruteForceWorkerManager bruteForcerWorkerManager = new BruteForceWorkerManager(args[0]);

                System.out.println("Starting "+bruteForcerWorkerManager.getThreadCount()+" Threads");
                for (int i = 0 ; i < bruteForcerWorkerManager.getThreadCount(); i++)
                {
                    new BruteForceWorker( bruteForcerWorkerManager, i).start();
                    Thread.sleep(2000);
                }

                //ProxymityConnection proxymityConnection = new ProxymityConnection();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
