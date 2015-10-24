package com.circles.rippers.TorRange;

import org.ini4j.Ini;

import java.io.File;
import java.io.FileInputStream;
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
    private int threadCount;
    static private int torRangeStart = 0;

    Vector<EntriesRange> ranges = new Vector<EntriesRange>();
    EntriesRange currentRange;
    long currentEntry;
    protected long totalEntriesCount;

    protected DB doneRanges;

    public WorkerManager(String iniFilename)
    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {

                prepareForExit();

            }
        });

        if (iniFilename != null) {
            readGeneralOptions(iniFilename);
            readOptions(iniFilename);
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
        try {
            session = filename.replace(".ini", "");
            Ini prefs = new Ini(new File(filename));

            threadCount = Integer.parseInt(prefs.get("WorkerManager", "threads"));
            torRangeStart = Integer.parseInt(prefs.get("WorkerManager", "torRangeStart"));

            prefix ="";
            try {
                prefix = prefs.get("WorkerManager", "prefix");
            } catch (Exception e) {

            }
        } catch (Exception e) {
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
                System.out.println("Range is done: "+range);
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
            System.out.println("Saving CurrentPhone: " + currentEntry);
            state.put(LATEST_ENTRY, currentEntry);
        }
    }

    public synchronized String getNextEntry()
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
        if (currentEntry % 300 ==0) {
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

                if (startString.length() != endString.length()) {
                    throw new Exception("Start has different length that stop.");
                }

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
            System.exit(0);
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
                throw new Exception("Current Phone error, returning range start. "+phone);
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
