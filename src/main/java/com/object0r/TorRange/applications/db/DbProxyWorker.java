package com.object0r.TorRange.applications.db;

import com.object0r.TorRange.TorWorker;

import java.util.HashMap;

abstract public class DbProxyWorker extends TorWorker
{
    DbProxyWorkerManager manager;

    public DbProxyWorker(DbProxyWorkerManager manager, int id)
    {
        super(manager, id);
        this.manager = manager;
    }

    /**
     * Returns true if there is indeed next to process and false when response is false.
     *
     * @return boolean
     */
    @Override
    protected boolean getNextAndProcess()
    {
        HashMap<String, String> values = manager.getNextEntryMap();
        if (values == null)
        {
            return false;
        }
        setIdle(false);
        process(values);
        return true;
    }

    abstract protected void process(HashMap<String, String> values);
}
