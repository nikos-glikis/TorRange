package com.circles.rippers.TorRange;


import java.sql.Connection;

abstract public class ProxymityWorker  extends ProxyWorker
{
    public ProxymityWorker(WorkerManager manager, int id, Connection dbConnection, String database)
    {
        super(manager, id);
        this.proxyConnection = new ProxymityConnection(dbConnection, database);
    }
}
