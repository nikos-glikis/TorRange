package com.circles.rippers.TorRange;

import com.circles.rippers.TorRange.TorWorker;
import com.circles.rippers.TorRange.WorkerManager;
import org.ini4j.Ini;

import java.io.File;

public class TorRangeSimpleExampleManager extends WorkerManager
{
    public TorRangeSimpleExampleManager(String iniFilename)
    {
        super(iniFilename);
    }

    @Override
    public void prepareForExit()
    {

    }

    @Override
    public void readOptions(String filename)
    {
        try {

            Ini prefs = new Ini(new File(filename));
            System.out.println(prefs.get("simple-example", "someVariable"));

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

}
