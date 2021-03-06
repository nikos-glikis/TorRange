package com.object0r.TorRange.applications.range;

import com.object0r.TorRange.ProxyRangeWorkerManager;
import com.object0r.TorRange.ProxyWorkerManager;
import org.ini4j.Ini;

import java.io.File;

public class TorRangeSimpleExampleManagerProxy extends ProxyRangeWorkerManager
{
    public TorRangeSimpleExampleManagerProxy(String iniFilename, Class workerClass)
    {
        super(iniFilename, workerClass, TorRangeSimpleExampleManagerProxy.class);
    }


    @Override
    public void readOptions(String filename)
    {
        try
        {

            Ini prefs = new Ini(new File(filename));
            System.out.println(prefs.get("simple-example", "someVariable"));

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

}
