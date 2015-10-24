package com.circles.rippers.TorRange;

import com.circles.rippers.TorRange.TorConnection;
import com.circles.rippers.TorRange.TorWorker;
import com.circles.rippers.TorRange.WorkerManager;

public class TorRangeSimpleExampleWorker extends TorWorker
{
    public void process(String entry)
    {
        //TODO tor in ini
        System.out.println("Entry: "+entry);
        try
        {
            Thread.sleep(100);
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
