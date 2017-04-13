package com.object0r.TorRange.applications.wordlist;

import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.datatypes.EntriesRange;
import com.object0r.TorRange.ProxyRangeWorkerManager;

import java.io.*;
import java.util.Scanner;
import java.util.Vector;

abstract public class WordlistConsumerWorkerManager extends ProxyRangeWorkerManager
{
    private final String LATEST_ENTRY_WORDLIST = "latest_entry_wordlist";
    private String wordlistFile;

    public WordlistConsumerWorkerManager(String iniFilename, Class workerClass)
    {
        super(iniFilename, workerClass, WordlistConsumerWorkerManager.class);
    }


    Scanner wordListScanner = null;
    static int globalCounter = 0;

    public synchronized String getNextEntry()
    {
        String returnString = "";
        try
        {
            int entry = Integer.parseInt(torRangeNextEntry());
            if (wordListScanner == null)
            {
                wordListScanner = new Scanner(new FileInputStream(wordlistFile));
                if (entry - 50 < 0)
                {
                    entry = 50;
                }
                for (int i = 0; i < entry - 50; i++)
                {
                    wordListScanner.nextLine();
                }
            }
            if (wordListScanner.hasNext())
            {
                returnString = wordListScanner.nextLine();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                Thread.sleep(20000);
            }
            catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
            System.exit(0);
        }
        if (globalCounter++ % saveEvery == saveEvery - 1)
        {
            saveCurrentEntryWordlist(returnString);
        }
        return returnString;
    }

    private synchronized void saveCurrentEntryWordlist(String currentEntry)
    {
        System.out.println("Saving Current Word: " + currentEntry);
        state.put(LATEST_ENTRY_WORDLIST, currentEntry);
        PrintWriter pr = null;
        try
        {
            pr = new PrintWriter("sessions/" + session + "/latest_wordlist.txt");
            pr.println(currentEntry);
            pr.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void readIniOptions(String filename)
    {
        super.readIniOptions(filename);
        readWordlistOptions(filename);
    }

    protected void readWordlistOptions(String filename)
    {
        try
        {
            wordlistFile = this.getIniValue("wordlist", "passwordfile");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public String getWordlistFile()
    {
        return wordlistFile;
    }

    public void setWordlistFile(String wordlistFile)
    {
        this.wordlistFile = wordlistFile;
    }

    public synchronized Vector<EntriesRange> getUserRanges()
    {
        Vector<EntriesRange> entriesRanges = new Vector<EntriesRange>();
        System.out.println("Wordlist is: " + wordlistFile);
        if (!new File(wordlistFile).exists())
        {
            System.out.println("Given wordlist file does not exist: " + wordlistFile);
            System.exit(0);
        }
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(wordlistFile));
            int lines = 0;
            while (reader.readLine() != null)
            {
                lines++;
            }
            System.out.println("Wordlist file has " + lines + " words (lines).");
            reader.close();
            EntriesRange entriesRange = new EntriesRange(1, lines + 1);
            entriesRanges.add(entriesRange);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return entriesRanges;
    }
}
