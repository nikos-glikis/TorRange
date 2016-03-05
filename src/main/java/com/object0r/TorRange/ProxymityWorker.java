package com.object0r.TorRange;


import java.sql.Connection;

abstract public class ProxymityWorker  extends ProxyWorker
{
    public ProxymityWorker(ProxyWorkerManager manager, int id, Connection dbConnection, String database)
    {
        super(manager, id);
        this.proxyConnection = new ProxymityConnection(dbConnection, database);
    }
}
