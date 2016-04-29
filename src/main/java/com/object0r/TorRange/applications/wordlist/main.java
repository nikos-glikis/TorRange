package com.object0r.TorRange.applications.wordlist;

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
                WordlistConsumerWorkerManager wordlistConsumerWorkerManager = new WordlistConsumerWorkerManager(args[0], WordlistConsumerWorker.class);
                wordlistConsumerWorkerManager.startWorkers();
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
