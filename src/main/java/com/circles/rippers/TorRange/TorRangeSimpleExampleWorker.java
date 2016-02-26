package com.circles.rippers.TorRange;

import com.circles.rippers.TorRange.TorConnection;
import com.circles.rippers.TorRange.TorWorker;
import com.circles.rippers.TorRange.WorkerManager;

public class TorRangeSimpleExampleWorker extends TorWorker
{
    public void process(String entry)
    {
        System.out.println("Entry: "+entry);

        try
        {
            System.out.println(readUrl("http://cpanel.com/showip.shtml"));
            Thread.sleep(1000);
            changeIp();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public TorRangeSimpleExampleWorker(WorkerManager manager, int id)
    {
        super(manager, id);
    }
}
