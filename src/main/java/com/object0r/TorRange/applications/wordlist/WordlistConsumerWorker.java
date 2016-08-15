package com.object0r.TorRange.applications.wordlist;

import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.TorWorker;

abstract public class WordlistConsumerWorker extends TorWorker
{
    WordlistConsumerWorkerManager manager;

    public WordlistConsumerWorker(ProxyWorkerManager manager, final int id)
    {
        super(manager, id);
        this.manager = (WordlistConsumerWorkerManager) manager;

    }
}
