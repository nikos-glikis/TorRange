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
    public synchronized String getNextEntry()
    {
        String returnString = "";
        try
        {
            int entry = Integer.parseInt(torRangeNextEntry());
            if (passwordListScanner  == null)
            {
                passwordListScanner = new Scanner(new FileInputStream(passwordFile));
                if (entry - 50 <  0)
                {
                    entry = 50;
                }
                for (int i = 0;i <entry-50 ;i++)
                {
                    passwordListScanner.nextLine();
                }
            }
            if (passwordListScanner.hasNext())
            {
                returnString= passwordListScanner.nextLine();
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

        return returnString;
    }

    @Override
    public void readOptions(String filename)
    {
        try
        {
            passwordFile= this.getIniValue("wordlist", "passwordfile");

        } catch (Exception e) {
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
        Vector <EntriesRange>entriesRanges = new Vector<EntriesRange>();
        System.out.println("Password file is: " +passwordFile);
        if (!new File(passwordFile).exists())
        {
            System.out.println("Given password file does not exist: "+passwordFile);
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
            System.out.println("Password file has " +lines+ " passwords.");
            reader.close();
            EntriesRange entriesRange = new EntriesRange(1,lines+1);
            entriesRanges.add(entriesRange);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return entriesRanges;
    }


}
