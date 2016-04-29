package com.object0r.TorRange;

import org.ini4j.Ini;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

public abstract class ProxyWorkerManager extends WorkerManager
{
    private static final String LOG_FILE =  "log.txt";
    String prefix;
    protected String session;
    protected DB state;
    int exitSeconds = 5;

    static private int activeThreadCount;
    private int threadCount = 50;
    static private int torRangeStart = 0;
    protected boolean useProxy = true;
    protected String iniFilename = "";

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

    public void registerWorker(ProxyWorker worker)
    {
        //worker.setActive(false);
        workers.add(worker);
    }

    public ProxyWorkerManager(String iniFilename)
    {
        if (iniFilename != null)
        {
            basicReadGeneralOptions(iniFilename);
            readGeneralOptions(iniFilename);
            readOptions(iniFilename);
        }
        //createTorScript();
    }

    public void startWorkers()
    {
        for(ProxyWorker worker :workers )
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
        for(ProxyWorker worker :workers )
        {
            worker.setActive(true);
        }
    }



    /* Creates start_tor_instances.sh script */
    protected void createTorScript()
    {
        try
        {
            //String template = Utilities.readFile("start_tor_instances_template.sh");
            String template = "#!/usr/bin/env bash\n" +
                    "#rm -rf /tmp/tor\n" +
                    "while :\n" +
                    "do\n" +
                    "    for i in {0..[[threadCount]]}\n" +
                    "    do\n" +
                    "        mkdir -p /tmp/tor/$socksport\n" +
                    "        controlport=$((i + [[controlPortStart]]))\n" +
                    "        socksport=$((i + [[controlPortEnd]]))\n" +
                    "        tor --RunAsDaemon 0 --CookieAuthentication 0 --NewCircuitPeriod 300000  --ControlPort $controlport --SocksPort $socksport --DataDirectory  /tmp/tor/$socksport --PidFile /tmp/tor/$socksport/my.pid &\n" +
                    "        sleep 0.3\n" +
                    "    done\n" +
                    "    sleep 5\n" +
                    "done";
            String finalScript = template.replace("[[threadCount]]", new Integer(threadCount+5).toString() );
            finalScript = finalScript.replace("[[controlPortStart]]" , new Integer(torRangeStart+10000).toString());
            finalScript = finalScript.replace("[[controlPortEnd]]" , new Integer(torRangeStart+20000).toString());
            PrintWriter pr = new PrintWriter("start_tor_instances.sh");
            pr.println(finalScript);
            pr.close();
            //System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This function is called when Ctrl+C is called or when the user tries to close the process.
     * Close resources, commit changes, close threads etc.
     */
    public abstract void prepareForExit();

    synchronized public void increaseThreadCount()
    {
        activeThreadCount++;
    }

    synchronized public void decreaseThreadCount()
    {
        activeThreadCount--;
    }

    static public int getActiveThreadCount()
    {
        return activeThreadCount;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public abstract void readOptions(String filename);

    /**
     * Returns a value from the ini with parameters.
     * @param section - Section in the ini.
     * @param variable - Variable.
     *
     *  for example to get
     *  [ConnectionManager]
     *  remoteHost=192.168.1.200
     * run readOptions("ConnectionManager,"remoteHost");
     * @return null|String
     */
    public String getIniValue (String section, String variable)
    {
        return getIniValue(section, variable, null);
    }

    /**
     * Returns a value from the ini with parameters.
     * @param section - Section in the ini.
     * @param variable - Variable.
     *
     *  for example to get
     *  [ConnectionManager]
     *  remoteHost=192.168.1.200
     * run readOptions("ConnectionManager,"remoteHost");
     * @return null|String
     */
    public String getIniValue (String section, String variable, String defaultValue)
    {
        String value = null;
        try
        {
            Ini prefs = new Ini(new File(iniFilename));
            if (prefs.get(section, variable)!=null)
            {
                value =prefs.get(section, variable);
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
            if (prefs.get("ProxyWorkerManager", "torRangeStart") !=null)
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
        try {
            session = filename.replace(".ini", "");
            Ini prefs = new Ini(new File(filename));

            try {
                String threadCountString = prefs.get("ProxyWorkerManager", "threads");
                if (threadCountString != null) {
                    threadCount = Integer.parseInt(threadCountString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Starting " + threadCount + " threads");

            try {
                torRangeStart = Integer.parseInt(prefs.get("ProxyWorkerManager", "torRangeStart"));
            } catch (Exception e) {

            }

            if (prefs.get("ProxyWorkerManager", "reportEvery") !=null)
            {
                try {
                    reportEverySeconds = Long.parseLong(prefs.get("ProxyWorkerManager", "reportEvery"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (reportEverySeconds == Long.MAX_VALUE)
            {
                System.out.println("Automatic reporting is not active.");
            }
            else
            {
                System.out.println("Automatic reporting every "+reportEverySeconds+".");
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
            } catch (Exception e)
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

            System.out.println("Exit Seconds is: "+exitSeconds);

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
            System.out.println("Some error happened while reading the session ini. ["+filename+"]");
            System.exit(0);
        }
    }


    /**
     * Override this when needed.
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
}
