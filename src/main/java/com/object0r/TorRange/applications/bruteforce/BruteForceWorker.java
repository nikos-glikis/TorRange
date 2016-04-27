package com.object0r.TorRange.applications.bruteforce;

import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.TorWorker;
import com.object0r.TorRange.applications.wordlist.WordlistConsumerWorkerManager;
import com.object0r.TorRange.helpers.TorRangeHttpHelper;

public class BruteForceWorker extends TorWorker
{
    public int threadCounter = 0;
    BruteForceWorkerManager manager;
    public BruteForceWorker(ProxyWorkerManager manager, final int id)
    {
        super(manager, id);

    }
    public void process(String entry)
    {
        System.out.println("Entry: "+entry);
        System.exit(0);
    }
}
