package com.object0r.TorRange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

abstract public class WorkerManager extends Thread implements IWorkerManager
{
    Vector<ProxyWorker> workers = new Vector<ProxyWorker>();
    Vector<ProxyWorker> allWorkers = new Vector<ProxyWorker>();

    abstract public int exitInSeconds();

    protected boolean exiting = false;
    //Automatic Report Interval in seconds
    long reportEverySeconds = Long.MAX_VALUE;

    abstract public void prepareForExit();

    protected void shutDownAllWorkers()
    {
        for (final ProxyWorker worker : workers)
        {
            worker._shutDown();
        }
    }

    public WorkerManager()
    {
        Runtime.getRuntime().addShutdownHook(
                new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            System.out.println("\nExiting in " + exitInSeconds() + " seconds.");
                            exiting = true;
                            for (final ProxyWorker worker : workers)
                            {
                                new Thread()
                                {
                                    public void run()
                                    {
                                        try
                                        {
                                            worker._shutDown();
                                        }
                                        catch (Exception e)
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
                }
        );


        new Thread()
        {
            public void run()
            {
                try
                {


                    while (true)
                    {
                        final BufferedReader read = new BufferedReader(new InputStreamReader(System.in));

                        TimerTask ft = new TimerTask()
                        {
                            public void run()
                            {
                                try
                                {
                                    read.close();
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };

                        (new Timer()).schedule(ft, 30000);
                        try
                        {
                            read.read();
                            printReport();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        finally
                        {
                            if (exiting)
                            {
                                return;
                            }
                        }


                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread()
        {
            public void run()
            {
                try
                {
                    while (true)
                    {
                        Thread.sleep(reportEverySeconds);
                        if (exiting)
                        {
                            break;
                        }
                        printReport();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void printReport()
    {
        printGeneralReport();
    }

    abstract void printGeneralReport();
}
