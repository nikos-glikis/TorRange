package com.object0r.TorRange.applications.wordlist;

import com.object0r.TorRange.EntriesRange;
import com.object0r.TorRange.ProxyRangeWorkerManager;
import org.ini4j.Ini;

import java.io.*;
import java.util.Scanner;
import java.util.Vector;

import static java.util.UUID.randomUUID;

public class WordlistConsumerWorkerManager extends ProxyRangeWorkerManager
{
    private final String LATEST_ENTRY_WORDLIST = "lates_entry_wordlist";
    private String passwordFile;

    public WordlistConsumerWorkerManager(String iniFilename)
    {
        super(iniFilename);
    }

    @Override
    public void prepareForExit()
    {

    }

    Scanner passwordListScanner = null;
    static int globalCounter = 0;

    public synchronized String getNextEntry()
    {
        String returnString = "";
        try
        {
            int entry = Integer.parseInt(torRangeNextEntry());
            if (passwordListScanner == null)
            {
                passwordListScanner = new Scanner(new FileInputStream(passwordFile));
                if (entry - 50 < 0)
                {
                    entry = 50;
                }
                for (int i = 0; i < entry - 50; i++)
                {
                    passwordListScanner.nextLine();
                }
            }
            if (passwordListScanner.hasNext())
            {
                returnString = passwordListScanner.nextLine();
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
        if (globalCounter++ % saveEvery == saveEvery-1)
        {
            saveCurrentEntryWordlist(returnString);
        }
        return returnString;
    }

    private void saveCurrentEntryWordlist(String currentEntry)
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
    public void readOptions(String filename)
    {
        try
        {
            passwordFile = this.getIniValue("wordlist", "passwordfile");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public String getPasswordFile()
    {
        return passwordFile;
    }

    public void setPasswordFile(String passwordFile)
    {
        this.passwordFile = passwordFile;
    }

    public synchronized Vector<EntriesRange> getUserRanges()
    {
        Vector<EntriesRange> entriesRanges = new Vector<EntriesRange>();
        System.out.println("Wordlist is: " + passwordFile);
        if (!new File(passwordFile).exists())
        {
            System.out.println("Given wordlist file does not exist: " + passwordFile);
            System.exit(0);
        }
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(passwordFile));
            int lines = 0;
            while (reader.readLine() != null)
            {
                lines++;
            }
            System.out.println("Wordlist file has " + lines + " passwords.");
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
