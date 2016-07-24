package com.object0r.TorRange.applications.db;

import com.object0r.TorRange.TorWorker;

import java.util.HashMap;

abstract class DbProxyWorker extends TorWorker
{
    DbProxyWorkerManager manager;

    public DbProxyWorker(DbProxyWorkerManager manager, int id)
    {
        super(manager, id);
        this.manager = manager;
    }

    abstract void process(HashMap<String, String> values);
}
