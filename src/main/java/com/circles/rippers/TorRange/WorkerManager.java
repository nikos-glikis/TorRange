package com.circles.rippers.TorRange;

import org.ini4j.Ini;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

public abstract class WorkerManager extends Thread
{
    String prefix;
    protected String session;
    protected DB state;
    protected boolean exiting= false;
    private static final String LATEST_ENTRY = "LATEST_PHONE";
    static private int activeThreadCount;
    private int threadCount = 50;
    static private int torRangeStart = 0;
    protected boolean useTor = true;
    private int saveEvery = 300;
    int exitSeconds = 10;

    Vector<EntriesRange> ranges = new Vector<EntriesRange>();
    EntriesRange currentRange;
    long currentEntry;
    protected long totalEntriesCount;

    protected DB doneRanges;

    public WorkerManager(String iniFilename)
    {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try
                {
                    System.out.println("\nExiting in " + exitSeconds + " seconds.");
                    exiting = true;
                    //Thread.sleep(exitSeconds * 1000);
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

                        ConsoleColors.printCyan("Active Thread Count: " + WorkerManager.getActiveThreadCount());
                        double percentage = ((getDoneCount()+0.0)*100)/ totalEntriesCount;
                        DecimalFormat df = new DecimalFormat("#.00");

                        ConsoleColors.printCyan("Done: " + getDoneCount() + "/" + totalEntriesCount + " - " + df.format(percentage) + "%");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        if (iniFilename != null) {
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
                    "        tor --RunAsDaemon 0 --CookieAuthentication 0 --HashedControlPassword \"\" --ControlPort $controlport --SocksPort $socksport --DataDirectory  /tmp/tor/$socksport --PidFile /tmp/tor/$socksport/my.pid &\n" +
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
        basicReadGeneralOptions(filename);
    }

    public void basicReadGeneralOptions(String filename)
    {
        try {
            session = filename.replace(".ini", "");
            Ini prefs = new Ini(new File(filename));
            doneRanges = new DB(session, "doneRanges");
            try
            {
                threadCount = Integer.parseInt(prefs.get("WorkerManager", "threads"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            torRangeStart = Integer.parseInt(prefs.get("WorkerManager", "torRangeStart"));
            readRanges("input/" + prefs.get("WorkerManager", "rangesfile"));
            state = new DB(session, "state");
            prefix ="";
            try {
                prefix = prefs.get("WorkerManager", "prefix");
                if (prefix == null)
                {
                    prefix = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                saveEvery = Integer.parseInt(prefs.get("WorkerManager", "saveEvery"));

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Save Every value is: "+saveEvery);
            try
            {
                String useTor = prefs.get("WorkerManager", "useTor");
                if (useTor != null)
                {
                    if (useTor.equals("false"))
                    {
                        this.useTor = false;
                    }
                    else
                    {
                        this.useTor = true;
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                exitSeconds = Integer.parseInt(prefs.get("WorkerManager", "exitSeconds"));
            }
            catch (Exception e)
            {
                System.out.println("Exit seconds error.");
                System.out.println(e);
            }

            System.out.println("Exit Seconds is: "+exitSeconds);

            if (this.useTor)
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

    /**
     * Returns how many phones are already processed.
     * @return
     */
    public long getDoneCount()
    {
        if (currentRange == null || ranges == null ) {
            return 0;
        }

        long doneCount=0;

        for (EntriesRange range : ranges) {
            if (isRangeDone(range)) {
                //System.out.println("Range is done: "+range);
                doneCount += range.getSize();
            }
        }

        doneCount+= (currentEntry - currentRange.getStart() );

        return doneCount;
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

    public String getNextEntry()
    {
        return basicGetNextEntry();
    }

    public synchronized String basicGetNextEntry()
    {
        if (exiting) {
            sleepForALogTime();
        }
        if (currentRange == null) {
            updateCurrentRange();
            saveCurrentEntry();
        }

        if (currentEntry ==0) {
            currentEntry = getCurrentEntry();
        }

        if (currentEntry <= currentRange.getEnd()) {

        } else {
            addRangeToDone(currentRange);
            updateCurrentRange();
            currentEntry = currentRange.getStart();
            saveCurrentEntry();
        }
        if (currentEntry % saveEvery ==0) {
            saveCurrentEntry();
        }
        return  prefix +""+(currentEntry++);

    }

    private void sleepForALogTime() {
        try {
            Thread.sleep(20000000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

}
