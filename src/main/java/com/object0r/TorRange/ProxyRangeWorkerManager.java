package com.object0r.TorRange;



import org.ini4j.Ini;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

public abstract class ProxyRangeWorkerManager extends ProxyWorkerManager
{

    private static final String LOG_FILE =  "log.txt";
    String prefix;

    protected DB state;
    int exitSeconds;
    private static final String LATEST_ENTRY = "LATEST_PHONE";


    Vector<EntriesRange> ranges = new Vector<EntriesRange>();
    EntriesRange currentRange;
    protected long totalEntriesCount;
    protected DB doneRanges;
    private int saveEvery = 300;

    public ProxyRangeWorkerManager(String iniFilename)
    {
        super(iniFilename);
        if (iniFilename != null)
        {
            readTorRangeOptions(iniFilename);
        }
        createTorScript();
    }

    void readTorRangeOptions(String iniFilename)
    {
        try
        {
            try
            {
                Ini prefs = new Ini(new File(iniFilename));
                if (prefs.get("ProxyWorkerManager", "saveEvery") !=null)
                {
                    saveEvery = Integer.parseInt(prefs.get("ProxyWorkerManager", "saveEvery"));
                }
                doneRanges = new DB(session, "doneRanges");
                try
                {
                    //Backwards compatibility is a bitch.
                    if (prefs.get("ProxyWorkerManager", "rangesfile") == null)
                    {
                        this.ranges = getUserRanges();
                    }
                    else
                    {
                        readRanges("input/" + prefs.get("ProxyWorkerManager", "rangesfile"));
                    }
                }
                catch (Exception e)
                {
                    this.ranges = getUserRanges();
                }
                for (EntriesRange range : ranges) {
                    totalEntriesCount +=range.getSize();
                }
                prefix ="";
                try {
                    prefix = prefs.get("ProxyWorkerManager", "prefix");
                    if (prefix == null)
                    {
                        prefix = "";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                state = new DB(session, "state");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            System.out.println("Save Every value is: "+saveEvery);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public Vector<EntriesRange> getUserRanges()
    {
        return new Vector<EntriesRange>();
    }

    public abstract void readOptions(String filename);

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

    /**
     * Returns how many phones are already processed.
     * @return
     */
    public long getTotalJobsCount()
    {
        return totalEntriesCount;
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
    public String getNextEntry()
    {
        return torRangeNextEntry();
    }

    public String basicGetNextEntry()
    {
        return torRangeNextEntry();
    }

    public synchronized String torRangeNextEntry()
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


    /**
     *
     * @param filename
     */
    @Deprecated
    protected synchronized void readRanges(String filename) throws Exception
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



        } catch (Exception e) {
            System.out.println(e.toString());
            throw e;
            //e.printStackTrace();
            //System.exit(0);
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
