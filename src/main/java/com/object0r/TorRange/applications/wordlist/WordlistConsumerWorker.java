package com.object0r.TorRange.applications.wordlist;

import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.TorWorker;
import com.object0r.TorRange.helpers.TorRangeHttpHelper;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

abstract public class WordlistConsumerWorker extends TorWorker
{
    public int threadCounter = 0;
    WordlistConsumerWorkerManager manager;

    public WordlistConsumerWorker(ProxyWorkerManager manager, final int id)
    {
        super(manager, id);
        this.manager = (WordlistConsumerWorkerManager) manager;

    }



    public void logWinner(String page, String entry)
    {
        try
        {
            System.out.println("We have a winner: " + entry);
            manager.logWinner(page, entry);
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
