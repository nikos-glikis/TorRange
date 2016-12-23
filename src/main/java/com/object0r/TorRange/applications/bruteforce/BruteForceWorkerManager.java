package com.object0r.TorRange.applications.bruteforce;


import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.helpers.BruteForcer;

import java.util.Vector;

public class BruteForceWorkerManager extends AbstractBruteForceWorkerManager
{
    public BruteForceWorkerManager(String iniFilename, Class workerClass)
    {
        super(iniFilename, workerClass);
    }

    @Override
    public void readOptions(String filename)
    {

    }
}
