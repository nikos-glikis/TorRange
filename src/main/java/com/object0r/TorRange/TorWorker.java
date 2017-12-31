package com.object0r.TorRange;

import com.object0r.TorRange.connections.DummyProxyConnection;
import com.object0r.TorRange.connections.TorConnection;

abstract public class TorWorker extends ProxyWorker
{

    ProxyWorkerManager manager;

    public TorWorker(ProxyWorkerManager manager, final int id)
    {
        super(manager, id);

        this.manager = (ProxyWorkerManager) manager;
        if (manager.useTor())
        {
            proxyConnection = new TorConnection(this.manager.getTorRangeStart() + id % manager.getMaxTorConnections());
            verifyTor(true);
        }
        else
        {
            proxyConnection = new DummyProxyConnection();
        }
    }

    @Override
    public synchronized void shutDown()
    {
        System.out.println("Closing tor connection ...");
        proxyConnection.close();
    }
}
