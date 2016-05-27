package com.object0r.TorRange;


import com.object0r.TorRange.datatypes.EntriesRange;
import com.object0r.toortools.ConsoleColors;
import com.object0r.toortools.DB;
import org.ini4j.Ini;

import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import static java.util.UUID.randomUUID;

public abstract class ProxyRangeWorkerManager extends ProxyWorkerManager
{
    private static final String LOG_FILE = "log.txt";
    private static long PROXY_RANGE_STEP = 1;

    public String getPrefix()
    {
        return prefix;
    }

    String prefix;

    int exitSeconds;
    private static final String LATEST_ENTRY = "LATEST_ENTRY";

    Vector<EntriesRange> ranges = new Vector<EntriesRange>();
    EntriesRange currentRange;
    protected long totalEntriesCount;
    protected DB doneRanges;


    /**
     * The last entry where our "request" succeed. Used for dynamic skip.
     */
    long lastSuccessfulEntry;

    /**
     * This is a conf value, if @failCount continues fails happen then we skip by @failSkip
     * -1 means its not used.
     */
    long failCount =-1;

    /**
     * This is a conf value. When we have failCount continues fails (@currentEntry - @lastSuccessfullEntry > 0) then we increase current entry by @failSkip.
     * -1 means its not used.
     */
    long failSkip = -1;


    public ProxyRangeWorkerManager(String iniFilename, Class workerClass)
    {
        super(iniFilename, workerClass);
        if (iniFilename != null)
        {
            readTorRangeOptions(iniFilename);
        }
        //createTorScript();
    }


    void readTorRangeOptions(String iniFilename)
    {
        try
        {
            try
            {
                Ini prefs = new Ini(new File(iniFilename));
                if (prefs.get("ProxyWorkerManager", "saveEvery") != null)
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
                        updateCurrentRange();
                    }
                    else
                    {
                        readRanges("input/" + prefs.get("ProxyWorkerManager", "rangesfile"));
                    }
                }
                catch (Exception e)
                {
                    this.ranges = getUserRanges();
                    updateCurrentRange();
                }

                if (prefs.get("ProxyWorkerManager", "step") ==null )
                {
                    PROXY_RANGE_STEP = 1;
                }
                else
                {
                    try
                    {
                        PROXY_RANGE_STEP = Integer.parseInt(prefs.get("ProxyWorkerManager", "step"));
                    }
                    catch (Exception e)
                    {
                        System.out.println("Error converting step value, setting default of 1");
                        PROXY_RANGE_STEP = 1;
                    }
                }

                if (PROXY_RANGE_STEP !=1)
                {
                    System.out.println("Step is set to "+PROXY_RANGE_STEP);
                }

                //FailCount
                //

                try {
                    String failCountString = prefs.get("ProxyWorkerManager", "failCount");
                    if (failCountString != null) {
                        failCount= Integer.parseInt(failCountString);
                        if (failCount > 0) {
                            System.out.println("FailCount is:" +failCount);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    String failSkipString = prefs.get("ProxyWorkerManager", "failSkip");
                    if (failSkipString != null) {
                        failSkip = Integer.parseInt(failSkipString);
                        if (failSkip > 0) {
                            System.out.println("FailSkip is:" + failSkip);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (EntriesRange range : ranges)
                {
                    totalEntriesCount += range.getSize();
                }
                System.out.println("totalEntriesCount: " + totalEntriesCount);
                prefix = "";
                try
                {
                    prefix = prefs.get("ProxyWorkerManager", "prefix");
                    if (prefix == null)
                    {
                        prefix = "";
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                //state = new DB(session, "state");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            System.out.println("Save Every value is: " + getSaveEvery());
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
     *
     * @return returns how many entries are done.
     */
    public long getDoneCount()
    {
        if (currentRange == null || ranges == null)
        {
            return 0;
        }

        long doneCount = 0;

        for (EntriesRange range : ranges)
        {
            if (isRangeDone(range))
            {
                //System.out.println("Range is done: "+range);
                doneCount += range.getSize();
            }
        }

        doneCount += (currentEntry - currentRange.getStart());

        return doneCount;
    }

    /**
     * Returns how many phones are already processed.
     *
     * @return
     */
    public long getTotalJobsCount()
    {
        return totalEntriesCount;
    }

    boolean isRangeDone(EntriesRange range)
    {
        String done = doneRanges.get(range.toString());
        return !(done == null);
    }

    synchronized void addRangeToDone(EntriesRange range)
    {
        doneRanges.put(range.toString(), "true");
    }

    /**
     * Override this when needed.
     *
     * @return
     */
    public synchronized String getNextEntry()
    {
        return torRangeNextEntry();
    }

    public String basicGetNextEntry()
    {
        return torRangeNextEntry();
    }

    public synchronized String torRangeNextEntry()
    {
        if (exiting)
        {
            sleepForALogTime();
        }

        if (currentRange == null)
        {
            updateCurrentRange();
            //saveCurrentEntry();
        }

        if (currentEntry == 0)
        {
            currentEntry = getCurrentEntry();
            ConsoleColors.printCyan("Current entry is: " + currentEntry);
        }

        if (failSkipEnabled())
        {
            if (currentEntry - lastSuccessfulEntry >= failCount) {
                ConsoleColors.printBlue("Skipping "+ failSkip);
                currentEntry = currentEntry + failSkip;
                lastSuccessfulEntry = currentEntry;
            }
        }

        //noinspection StatementWithEmptyBody
        if (currentEntry <= currentRange.getEnd())
        {

        }
        else
        {
            addRangeToDone(currentRange);
            updateCurrentRange();
            currentEntry = currentRange.getStart();
            saveCurrentEntry();
        }

        if (currentEntry % (getSaveEvery()) == (this.getSaveEvery() - 1))
        {
            saveCurrentEntry();
        }

        currentEntry = currentEntry+PROXY_RANGE_STEP;
        return prefix + "" + currentEntry;

    }

    private void sleepForALogTime()
    {
        try
        {
            Thread.sleep(20000000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Deprecated
    protected synchronized void readRanges(String filename) throws Exception
    {
        try
        {
            Scanner sc = new Scanner(new FileInputStream(filename));
            while (sc.hasNext())
            {
                String line = sc.nextLine().trim().replace(" ", "");
                if (line.charAt(0) == '#')
                {
                    continue;
                }

                StringTokenizer st = new StringTokenizer(line, "-");
                String startString = st.nextToken();
                String endString = st.nextToken();

                /*if (startString.length() != endString.length()) {
                    throw new Exception("Start has different length that stop.");
                }*/

                long start = Long.parseLong(startString);
                long end = Long.parseLong(endString);

                if (start > end)
                {
                    System.out.println("Input Error: start is bigger than end in phone range.");
                    System.exit(0);
                }

                EntriesRange entriesRange = new EntriesRange(start, end);
                if (ranges == null)
                {
                    ranges = new Vector<EntriesRange>();
                }
                ranges.add(entriesRange);
                System.out.println("Added Entry range: " + entriesRange);
            }


        }
        catch (Exception e)
        {
            System.out.println(e.toString());
            throw e;
            //e.printStackTrace();
            //System.exit(0);
        }
    }

    private void updateCurrentRange()
    {
        boolean found = false;
        for (EntriesRange range : ranges)
        {
            System.out.println("Checking range: " + range);
            if (!isRangeDone(range))
            {
                currentRange = range;
                System.out.println("Updating range, new range is: " + range);
                found = true;
                break;
            }
        }
        if (!found)
        {
            System.out.println("Seems that all ranges have ended. Will stop in a few seconds.");
            try
            {
                Thread.sleep(20000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            //System.exit(0);
        }
    }

    synchronized long getCurrentEntry()
    {
        try
        {
            String latestDoneString = state.get(LATEST_ENTRY);

            long entry;
            try
            {
                ConsoleColors.printRed("Latest Entry is: " + latestDoneString);

                entry = Long.parseLong(latestDoneString) - saveEvery * 2 * PROXY_RANGE_STEP;
                if (entry < 1)
                {
                    entry = 1;
                }
            }
            catch (Exception e)
            {
                ConsoleColors.printRed("GetCurrentEntry: " + e.toString());
                return currentRange.getStart();
            }
            if (
                    entry >=
                            currentRange.getStart()
                            &&
                            entry <=
                                    currentRange.getEnd())
            {
                return entry;
            }
            else
            {
                return currentRange.getStart();
                //throw new Exception("Current entry error, returning range start. " + entry);
            }
        }
        catch (Exception e)
        {
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
                //noinspection ResultOfMethodCallIgnored
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

    public synchronized void logWinner(String page, String entry)
    {
        logWinner(page, entry, "Success: ");
    }

    public synchronized void logWinner(String page, String entry, String logMessage)
    {
        try
        {
            String folder = "sessions/" + session + "/success_output";
            if (!new File(folder).exists())
            {
                //noinspection ResultOfMethodCallIgnored
                new File(folder).mkdirs();
            }
            String entrySafe = entry.replaceAll("\\W+", "");
            entrySafe = entrySafe + "_" + randomUUID() + ".htm";
            File f = File.createTempFile("result_", entrySafe, new File(folder));
            PrintWriter pr = new PrintWriter(f);
            pr.println(page);
            pr.close();

            pr = new PrintWriter(new FileOutputStream("log.txt", true));
            pr.println(logMessage + entry);
            pr.println("Result page saved in: " + f.getAbsolutePath());
            pr.println();
            pr.close();
            //String file = folder +"/"++".htm";
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //AutoSkip


    private boolean failSkipEnabled()
    {
        return failCount > 0 && failSkip > 0;
    }

    /**
     * Marks that an entry had a successful result. Used for autoskip.
     * @param entry
     */
    public void markSuccessful(String entry)
    {
        try
        {
            lastSuccessfulEntry = Long.parseLong(entry.substring(getPrefix().length()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
