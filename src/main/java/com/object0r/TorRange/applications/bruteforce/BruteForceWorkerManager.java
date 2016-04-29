package com.object0r.TorRange.applications.bruteforce;


import com.object0r.TorRange.ProxyRangeWorkerManager;
import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.helpers.BruteForcer;

import java.io.FileInputStream;
import java.util.Scanner;
import java.util.Vector;

public class BruteForceWorkerManager extends ProxyWorkerManager
{
    Vector<BruteForcer> bruteForcers;

    public BruteForceWorkerManager(String iniFilename, Class workerClass)
    {
        super(iniFilename, workerClass);
    }

    @Override
    public void prepareForExit()
    {

    }

    static long globalCounter = 0;

    public synchronized String getNextEntry()
    {
        String returnString = "";
        try
        {
            for (int i = bruteForcers.size() - 1; i >= 0; i--)
            {
                BruteForcer bruteForcer = bruteForcers.get(i);
                if (!bruteForcer.getNext().equals(""))
                {
                    break;
                }
                else
                {
                    bruteForcer.reset();
                    if (i == 0)
                    {
                        System.out.println("Brute brute force keywords has ended. Dying in 120 secods");
                        prepareForExit();
                        System.out.println("Global Counter: " + globalCounter);
                        Thread.sleep(120000);
                        System.exit(0);
                    }
                }
            }
            globalCounter++;
            if (globalCounter % (this.getSaveEvery()) == (this.getSaveEvery()- 1))
            {
                saveCurrentEntry(getCurrentJoinedString());
            }
            return getCurrentJoinedString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return returnString;
    }

    @Override
    public void readOptions(String filename)
    {
        try
        {
            bruteForcers = new Vector<BruteForcer>();
            boolean foundAtLeastOne = false;
            for (int i = 1; i < 100; i++)
            {

                String characters = this.getIniValue("bruteforce", "characters" + i);
                if (characters == null)
                {
                    break;
                }
                foundAtLeastOne = true;
                String minLength = this.getIniValue("bruteforce", "minimumlength" + i);
                if (minLength == null)
                {
                    minLength = "1";
                }
                String maxLength = this.getIniValue("bruteforce", "maximumlength" + i);
                if (maxLength == null)
                {
                    maxLength = "100";
                }

                if (Integer.parseInt(minLength) > Integer.parseInt(maxLength))
                {
                    throw new Exception("Error, MinLength more than MaxLength");
                }

                BruteForcer bruteForcer = new BruteForcer(characters, Integer.parseInt(minLength), Integer.parseInt(maxLength));

                String start = this.getIniValue("bruteforce", "start" + i);
                if (start != null)
                {
                    bruteForcer.setStart(start);
                }
                bruteForcers.add(i - 1, bruteForcer);
            }

            if (!foundAtLeastOne)
            {
                throw new Exception("No Brute Force Entries found.");
            }
            if (bruteForcers.size() > 0)
            {
                System.out.println(bruteForcers);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public String getCurrentJoinedString()
    {
        String currentJoinedString = "";
        for (BruteForcer bruteforce : bruteForcers)
        {
            currentJoinedString = currentJoinedString + bruteforce.toString();
        }
        return currentJoinedString;
    }

    @Override
    public long getDoneCount()
    {
        return 0;
    }

    @Override
    public long getTotalJobsCount()
    {
        return 0;
    }
}
