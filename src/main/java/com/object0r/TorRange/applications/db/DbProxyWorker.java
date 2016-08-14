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

    @Override
    protected void getNextAndProcess()
    {
        HashMap<String, String> values = manager.getNextEntryMap();
        setIdle(false);
        process(values);
    }

    abstract void process(HashMap<String, String> values);
}
