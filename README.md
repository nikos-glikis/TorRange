Threaded Range Processor, with TOR Integration
==============================================

TorRange is a library that can create multiple threads and consume large input of any size efficiently. 

A Tor Instance is tied to each thread if needed and the thread can control the connection (Restart circuit/change ip etc).

The process can be stopped and resumed at any point. 

Introduction
-------------

This repository contains a library that can read one or more Integer Ranges/DatabaseTables/Wordlists/Bruteforce words, and process each entry in that process.

Each thread is connected to a tor connection, which it has control over. When needed the ip of the Thread can be refreshed (changeIp() in worker), and a readUrl(String url) method is provided that downloads some url, using TOR.

You can control the number of threads running and you can stop/resume the process at any point.

This library was created to process many millions of requests on a service that limits requests per ip, anonymously.

Requirements
-----------

To run this you need a linux box with tor and java (obviously) installed.

TOR path must be added in the PATH of the linux box tor can be manually runned. To verify that open a console and run tor command.

This has been tested only for root user.

Configuration
-------------

Create a .ini file similar to inis/example.ini

    [ProxyWorkerManager]
    #default is 50
    threads=100
    #default is empty String ""
    prefix=
    #default is true;
    torRangeStart=300
    #How ofter to save the current processed number. Default is 300
    saveEvery=50
    #useTor can be true or false
    #default is true for security, only writting false will disable tor.
    useTor=true
    #there is no default, please fill this. This file should be in the input direcotry
    rangesfile=simpleExample.txt
    #seconds to sleep after KILL signal. Default is 10
    exitSeconds=15
    


If you want to consume a [mysql|sqlite|JDBC Compatible] database you can see this example:

[https://github.com/nikos-glikis/TorRangeDbExample](https://github.com/nikos-glikis/TorRangeDbExample)

How to create your own RangeProcessor
--------------------------------------

To create your own RangeProcessor, take a look at TorRangeSimpleExampleManager and TorRangeSimpleExampleWorker.

Worker is a thread that performs some work on an Entry (number) from the Range.

WorkerManager is responsible for: 

1) To give read the ranges and options.
2) Give values to the Workers in a synchronized way
3) Do all jobs that need to be synchronized.

You will have to create and run only ONE WorkerManager. When a WorkerManager is created, you then create the Workers (Threads) and you pass the WorkerManager as an argument.

Steps to create a new app using TorRange.

1) Create a file that Extends WorkerManager.

2) Create a worker class that extends TorWorker. Create a constructor similar to that of TorRangeSimpleExampleWorker. 

3) Create all abstract methods. Read the comments for more info of what each method does.

4) Overwrite ProxyRangeWorkerManager.getUserRanges(); to give your ranges. If you have rangesfile=somefile.txt in the ini, the ranges from this file are loaded.

Progress Report
===============

At any point press enter to see the progress report. To make your own report, override the public void printReport() method in WorkerManager(). Old report is available at printGeneralReport.

        
Applications:
=============

1) Database

- TorRange can consume a database (efficiently). 
- Clone this repo to get started: [https://github.com/nikos-glikis/TorRangeDbExample](https://github.com/nikos-glikis/TorRangeDbExample)


2) Wordlist:

See package com.object0r.TorRange.applications.wordlist

You must extend WordlistConsumerWorker and possibly WordlistConsumerWorkerManager (Default is fine)

(Example repo coming soon)

Requires:
    
    [wordlist]
    passwordfile=/root/wordlists/rockyou.txt

in the .ini file along with the other TorRange requests.

3) Brute Force

(Example repo coming soon)