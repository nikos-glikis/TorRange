package com.object0r.TorRange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Vector;

/**
 * Created by User on 28/2/2016.
 */
abstract public class WorkerManager extends Thread implements IWorkerManager
{
    Vector<ProxyWorker> workers = new Vector<ProxyWorker>();

    abstract public int exitInSeconds();

    protected boolean exiting= false;

    abstract public void prepareForExit();

    public WorkerManager()
    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try
                {
                    System.out.println("\nExiting in " + exitInSeconds() + " seconds.");
                    exiting = true;
                    for (final ProxyWorker worker: workers)
                    {
                        new Thread() {
                            public void run()
                            {
                                try
                                {
                                    worker._shutDown();
                                }
                                catch (Exception e )
                                {

                                }
                            }

                        }.start();
                    }
                    Thread.sleep(exitInSeconds() * 1000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                prepareForExit();
            }
        });


        new Thread()
        {
            public void run() {
                try {

                    while (true) {

                        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
                        read.read();
                        //Thread.sleep(10000);
                        printReport();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread()
        {
            public void run() {
                try
                {
                    while (true)
                    {
                        Thread.sleep(60000);
                        printReport();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void printReport()
    {
        printGeneralReport();
    }

    private void printGeneralReport()
    {
        ConsoleColors.printCyan("Active Thread Count: " + ProxyWorkerManager.getActiveThreadCount());

        double percentage;
        if (getTotalJobsCount() == 0)
        {
            percentage = 0;
        }
        else
        {
            percentage = ((getDoneCount()+0.0)*100)/ getTotalJobsCount();
        }

        DecimalFormat df = new DecimalFormat("#.00");

        ConsoleColors.printCyan("Done: " + getDoneCount() + "/" + getTotalJobsCount() + " - " + df.format(percentage) + "%");
    }


}
