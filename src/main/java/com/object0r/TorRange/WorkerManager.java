package com.object0r.TorRange;

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
                        Thread.sleep(10000);

                        ConsoleColors.printCyan("Active Thread Count: " + TorWorkerManager.getActiveThreadCount());

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
