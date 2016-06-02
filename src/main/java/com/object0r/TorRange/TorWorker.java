package com.object0r.TorRange;


import com.object0r.TorRange.connections.TorConnection;

abstract public class TorWorker extends ProxyWorker
{

    int timeToSleepAfterKillTor = 30;

    ProxyWorkerManager manager;

    public TorWorker(ProxyWorkerManager manager, final int id)
    {
        super(manager, id);

        this.manager = (ProxyWorkerManager)manager;
        proxyConnection = new TorConnection(this.manager.getTorRangeStart() + id);

        if (manager.useTor())
        {
            verifyTor(true);
        }
    }

    @Override
    public synchronized void shutDown()
    {
        System.out.println("Closing tor connection ...");
        ((TorConnection) proxyConnection).closeTor();
    }
}
