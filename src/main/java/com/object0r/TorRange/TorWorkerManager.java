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

public abstract class TorWorkerManager extends WorkerManager
{
    private static final String LOG_FILE =  "log.txt";
    String prefix;
    protected String session;
    protected DB state;
    int exitSeconds;
    private static final String LATEST_ENTRY = "LATEST_PHONE";
    static private int activeThreadCount;
    private int threadCount = 50;
    static private int torRangeStart = 0;
    protected boolean useProxy = true;


    Vector<EntriesRange> ranges = new Vector<EntriesRange>();
    EntriesRange currentRange;
    long currentEntry;
    protected long totalEntriesCount;

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
        workers.add(worker);
    }

    public TorWorkerManager(String iniFilename)
    {
        if (iniFilename != null)
        {
            readGeneralOptions(iniFilename);
            readOptions(iniFilename);
        }
        createTorScript();
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
                    "        tor --RunAsDaemon 0 --CookieAuthentication 0 --NewCircuitPeriod 3000  --ControlPort $controlport --SocksPort $socksport --DataDirectory  /tmp/tor/$socksport --PidFile /tmp/tor/$socksport/my.pid &\n" +
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

    public void readGeneralOptions(String filename)
    {
        try
        {
            session = filename.replace(".ini", "");
            Ini prefs = new Ini(new File(filename));

            doneRanges = new DB(session, "doneRanges");
            torRangeStart = Integer.parseInt(prefs.get("TorWorkerManager", "torRangeStart"));
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
            doneRanges = new DB(session, "doneRanges");
            try
            {
                threadCount = Integer.parseInt(prefs.get("TorWorkerManager", "threads"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            torRangeStart = Integer.parseInt(prefs.get("TorWorkerManager", "torRangeStart"));
            readRanges("input/" + prefs.get("TorWorkerManager", "rangesfile"));
            state = new DB(session, "state");
            prefix ="";
            try {
                prefix = prefs.get("TorWorkerManager", "prefix");
                if (prefix == null)
                {
                    prefix = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try
            {
                String useTor = prefs.get("TorWorkerManager", "useProxy");
                if (useTor != null)
                {
                    if (useTor.equals("false"))
                    {
                        this.useProxy = false;
                    }
                    else
                    {
                        this.useProxy = true;
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                exitSeconds = Integer.parseInt(prefs.get("TorWorkerManager", "exitSeconds"));
            }
            catch (Exception e)
            {
                System.out.println("Exit seconds error.");
                System.out.println(e);
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
            //Thread.sleep(5000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Some error happened while reading the session ini. ["+filename+"]");
            System.exit(0);
        }
    }

    boolean isRangeDone(EntriesRange range)
    {
        String done = doneRanges.get(range.toString());
        return ! (done==null);
    }

    void addRangeToDone(EntriesRange range)
    {
        doneRanges.put(range.toString(), "true");
    }


    void saveCurrentEntry()
    {
        if (currentEntry != 0) {
            System.out.println("Saving Current Number: " + currentEntry);
            state.put(LATEST_ENTRY, currentEntry);
        }
    }

    /**
     * Override this when needed.
     * @return
     */
    abstract public String getNextEntry();



    protected synchronized void readRanges(String filename)
    {
        try {

            Scanner sc = new Scanner(new FileInputStream(filename));
            while (sc.hasNext()) {
                String line = sc.nextLine().trim().replace(" ","");
                if (line.charAt(0)=='#') {
                    continue;
                }

                StringTokenizer st = new StringTokenizer(line,"-");
                String startString = st.nextToken();
                String endString = st.nextToken();

                /*if (startString.length() != endString.length()) {
                    throw new Exception("Start has different length that stop.");
                }*/

                long start = Long.parseLong(startString);
                long end = Long.parseLong(endString);

                if (start> end) {
                    throw new Exception("start is bigger than end in phone range.");
                }

                EntriesRange entriesRange = new EntriesRange(start, end);
                if (ranges == null) {
                    ranges = new Vector<EntriesRange>();
                }
                ranges.add(entriesRange);
                System.out.println("Added Entry range: "+ entriesRange);
            }

            for (EntriesRange range : ranges) {
                totalEntriesCount +=range.getSize();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void updateCurrentRange()
    {
        System.out.println("CurrentPhoneRange does not exist, new start.");
        boolean found = false;
        for (EntriesRange range: ranges) {
            if (!isRangeDone(range)) {
                currentRange = range;
                System.out.println("Updating range, new range is: "+range);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Seems that all ranges have ended. Will stop in a few seconds.");
            try {
                Thread.sleep(20000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.exit(0);
        }
    }

    long getCurrentEntry() {
        try {
            String latestPhoneString  = state.get(LATEST_ENTRY);
            long phone;
            try {
                phone = Long.parseLong(latestPhoneString) - 50;
            } catch (Exception e) {
                return currentRange.getStart();
            }
            if (phone >= currentRange.getStart() && phone <= currentRange.getEnd()) {
                return phone;
            } else {
                throw new Exception("Current entry error, returning range start. "+phone);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return currentRange.getStart();
        }
    }

    static public int getTorRangeStart()
    {
        return torRangeStart;
    }

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
}
