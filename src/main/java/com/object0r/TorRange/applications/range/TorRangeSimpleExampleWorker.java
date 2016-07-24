package com.object0r.TorRange.applications.range;

import com.object0r.TorRange.ProxyRangeWorkerManager;
import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.TorWorker;

public class TorRangeSimpleExampleWorker extends TorWorker
{
    public void process(String entry)
    {
        System.out.println("Entry: " + entry);

        try
        {
            System.out.println(readUrl("http://cpanel.com/showip.shtml"));

            //manager.markSuccessful(entry);

            sleep(1000);
            changeIp();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    ProxyRangeWorkerManager manager ;
    public TorRangeSimpleExampleWorker(ProxyWorkerManager manager, int id)
    {
        super(manager, id);
        this.manager = (ProxyRangeWorkerManager) manager;
    }
}
