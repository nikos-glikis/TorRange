package com.object0r.TorRange;

import org.ini4j.Ini;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

public abstract class ProxyWorkerManager extends WorkerManager
{
    private static final String LOG_FILE = "log.txt";
    private static final String LATEST_ENTRY = "LATEST_ENTRY";

    Class<ProxyWorker> workerClass;
    protected String session;
    protected DB state;
    int exitSeconds = 5;

    private int workerCount = 50;
    static private int torRangeStart = 0;
    protected boolean useProxy = true;
    protected String iniFilename = "";
    long secondsBetweenIdleChecks = 120;

    public int saveEvery = 300;

    long currentEntry;

    protected DB doneRanges;

    public boolean useProxy()
    {
        return useProxy;
    }

    public boolean useTor()
    {
        return useProxy;
    }

    public ProxyWorkerManager(String iniFilename, Class workerClass)
    {
        this.workerClass = workerClass;

        if (iniFilename != null)
        {
            basicReadGeneralOptions(iniFilename);
            readGeneralOptions(iniFilename);
            readOptions(iniFilename);
        }
        state = new DB(session, "state");
        try
        {
            for (int i = 0; i < workerCount; i++)
            {
                try
                {
                    ProxyWorker worker = getNewWorker(i);
                    worker.setActive(false);
                    worker.start();
                    workers.add(worker);
                    allWorkers.add(worker);
                    Thread.sleep(100);
                }
                catch (InvocationTargetException e)
                {
                    e.printStackTrace();
                }
                catch (NoSuchMethodException e)
                {
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        startIdleWorkersCheck();
    }

    private ProxyWorker getNewWorker(int i) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
    {
        //Constructor AND class should be public.
        return workerClass.getConstructor(ProxyWorkerManager.class, int.class).newInstance(this, i);
    }

    private void startIdleWorkersCheck()
    {
        new Thread()
        {
            public void run()
            {

                while (true)
                {
                    //Check every 15 minutes.
                    try
                    {
                        Thread.sleep(900*1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    try
                    {
                        for (int i = 0; i < workers.size(); i++)
                        {
                            workers.get(i).setIdle(true);
                        }
                        Thread.sleep(secondsBetweenIdleChecks * 1000);
                        for (int i = 0; i < workers.size(); i++)
                        {
                            if (workers.get(i).isIdle())
                            {
                                workers.get(i)._shutDown();
                                ProxyWorker newWorker = getNewWorker(workers.get(i).getWorkerId());
                                allWorkers.add(newWorker);
                                workers.set(i, newWorker);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void startWorkers()
    {
        for (ProxyWorker worker : workers)
        {
            worker.setActive(true);
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void stopWorkers()
    {
        for (ProxyWorker worker : workers)
        {
            worker.setActive(false);
        }
    }

    /**
     * This function is called when Ctrl+C is called or when the user tries to close the process.
     * Close resources, commit changes, close threads etc.
     */
    public abstract void prepareForExit();

    public int getActiveThreadCount()
    {
        int totalCount=0;
        for (ProxyWorker proxyWorker: allWorkers)
        {
            if (proxyWorker.isActive && proxyWorker.isReady && !proxyWorker.isInterrupted())
            {
                totalCount++;
            }
        }
        return totalCount;
    }

    public int getWorkerCount()
    {
        return workerCount;
    }

    public abstract void readOptions(String filename);

    /**
     * Returns a value from the ini with parameters.
     *
     * @param section  - Section in the ini.
     * @param variable - Variable.
     *                 <p/>
     *                 for example to get
     *                 [ConnectionManager]
     *                 remoteHost=192.168.1.200
     *                 run readOptions("ConnectionManager,"remoteHost");
     * @return null|String
     */
    public String getIniValue(String section, String variable)
    {
        return getIniValue(section, variable, null);
    }

    /**
     * Returns a value from the ini with parameters.
     *
     * @param section  - Section in the ini.
     * @param variable - Variable.
     *                 <p/>
     *                 for example to get
     *                 [ConnectionManager]
     *                 remoteHost=192.168.1.200
     *                 run readOptions("ConnectionManager,"remoteHost");
     * @return null|String
     */
    public String getIniValue(String section, String variable, String defaultValue)
    {
        String value = null;
        try
        {
            Ini prefs = new Ini(new File(iniFilename));
            if (prefs.get(section, variable) != null)
            {
                value = prefs.get(section, variable);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return value;
    }

    public void readGeneralOptions(String filename)
    {
        try
        {
            session = filename.replace(".ini", "");
            Ini prefs = new Ini(new File(filename));
            this.iniFilename = filename;

            doneRanges = new DB(session, "doneRanges");
            if (prefs.get("ProxyWorkerManager", "torRangeStart") != null)
            {
                torRangeStart = Integer.parseInt(prefs.get("ProxyWorkerManager", "torRangeStart"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void basicReadGeneralOptions(String filename)
    {
        try
        {
            session = filename.replace(".ini", "");
            Ini prefs = new Ini(new File(filename));

            try
            {
                String threadCountString = prefs.get("ProxyWorkerManager", "threads");
                if (threadCountString != null)
                {
                    workerCount = Integer.parseInt(threadCountString);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            System.out.println("Starting " + workerCount + " threads");

            try
            {
                torRangeStart = Integer.parseInt(prefs.get("ProxyWorkerManager", "torRangeStart"));
            }
            catch (Exception e)
            {

            }

            if (prefs.get("ProxyWorkerManager", "reportEvery") != null)
            {
                try
                {
                    reportEverySeconds = Long.parseLong(prefs.get("ProxyWorkerManager", "reportEvery"));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            if (reportEverySeconds == Long.MAX_VALUE)
            {
                System.out.println("Automatic reporting is not active.");
            }
            else
            {
                System.out.println("Automatic reporting every " + reportEverySeconds + ".");
            }

            try
            {
                String useTor = prefs.get("ProxyWorkerManager", "useProxy");
                if (useTor != null)
                {

                }
                else
                {
                    useTor = prefs.get("ProxyWorkerManager", "useTor");
                }
                if (useTor == null)
                {

                }
                else if (useTor.equals("false"))
                {
                    this.useProxy = false;
                }
                else
                {
                    this.useProxy = true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                exitSeconds = Integer.parseInt(prefs.get("ProxyWorkerManager", "exitSeconds"));
            }
            catch (Exception e)
            {
               /* System.out.println("Exit seconds error.");
                System.out.println(e);*/
            }

            System.out.println("Exit Seconds is: " + exitSeconds);

            if (this.useProxy)
            {
                System.out.println("Tor is enabled.");
            }
            else
            {
                System.out.println("Tor is disabled");
            }
            System.out.println("Sleeping for 5 seconds, just in case this is an error.");
            Thread.sleep(5000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Some error happened while reading the session ini. [" + filename + "]");
            System.exit(0);
        }
    }


    /**
     * Override this when needed.
     *
     * @return
     */
    abstract public String getNextEntry();

    void simpleLog(String text)
    {
        simpleLog(text, "sessions/" + session + "/" + LOG_FILE);
    }

    synchronized public void simpleLog(String text, String filename)
    {
        try
        {
            if (!new File(filename).getParentFile().exists())
            {
                new File(filename).getParentFile().mkdirs();
            }

            PrintWriter pr;
            pr = new PrintWriter(new FileOutputStream(filename, true));
            pr.println(text);
            pr.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int exitInSeconds()
    {
        return exitSeconds;
    }

    static public int getTorRangeStart()
    {
        return torRangeStart;
    }

    public void printGeneralReport()
    {
        ConsoleColors.printCyan("Active Thread Count: " + getActiveThreadCount() );

        double percentage;
        if (getTotalJobsCount() == 0)
        {
            percentage = 0;
        }
        else
        {
            percentage = ((getDoneCount() + 0.0) * 100) / getTotalJobsCount();
        }

        DecimalFormat df = new DecimalFormat("#.00");

        ConsoleColors.printCyan("Done: " + getDoneCount() + "/" + getTotalJobsCount() + " - " + df.format(percentage) + "%");
    }

    public void saveCurrentEntry()
    {
        saveCurrentEntry(currentEntry + "");
    }

    public void saveCurrentEntry(String currentEntry)
    {
        System.out.println("Saving Current Number: " + currentEntry);
        state.put(LATEST_ENTRY, currentEntry);
        PrintWriter pr = null;
        try
        {
            pr = new PrintWriter("sessions/" + session + "/latest.txt");
            pr.println(currentEntry);
            pr.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    public int getSaveEvery()
    {
        return saveEvery;
    }

}
