package com.object0r.TorRange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class WorkerManager extends Thread implements IWorkerManager
{
    Vector<ProxyWorker> workers = new Vector<ProxyWorker>();
    Vector<ProxyWorker> allWorkers = new Vector<ProxyWorker>();

    protected Thread statusEnterThread;
    protected Thread statusThread;

    //How many requests are done per minute.
    private int requestsPerMinute;

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


        statusEnterThread = new Thread()
        {
            public void run()
            {
                try
                {
                    while (true)
                    {
                        final BufferedReader read = new BufferedReader(new InputStreamReader(System.in));

                        try
                        {
                            read.read();
                            printReport();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        statusEnterThread.start();

        statusThread = new Thread()
        {
            public void run()
            {
                try
                {
                    while (true)
                    {
                        Thread.sleep(reportEverySeconds);

                        printReport();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        statusThread.start();
    }

    public void printReport()
    {
        printGeneralReport();
    }

    abstract void printGeneralReport();

    public void setRequestsPerMinute(int requestsPerMinute)
    {
        this.requestsPerMinute = requestsPerMinute;
    }

    public int getRequestsPerMinute()
    {
        return this.requestsPerMinute;
    }
}
