Threaded Range Processor, with TOR Integration
==============================================

Introduction
-------------

This repository contains a library that can read one or more Ranges, and process each number in that process. It also contains an application of that library, that downloads phone numbers info from TrueCaller.

Each thread is connected to a tor, which it has control over. When needed the ip of the Thread can be changed, and a readUrl method is provided that downloads some url, using TOR.

Requirements
-----------

To run this you need a linux box with tor and java (obviously) installed.

TOR path must be added in the PATH of the linux box tor can be manually runned.

This has been tested only for root user.

Configuration
-------------

Create a .ini file similar to netherlands.ini

    [WorkerManager]
    threads=55
    prefix=+31

    [truecaller-ripper]
    rangesfile=netherlands.txt

Threads is how much Threads you want to run, prefix if it exists is the prefix to use when processing the numbers.

Base section for the ini is the [WorkerManager] but you can add your own after that.

Running
-------

To run this, 

1) execute ./start_tor_instances in one screen window (Or terminal window). The number in the for loop must be greater than the number of 
2) ./start

How to create your own RangeProcessor
--------------------------------------

To create your own RangeProcessor, take a look at TrueCallerWorker and TrueCallerWorkerManager.

Worker is a thread that performs some work on an Entry (number) from the Range.

WorkerManager is responsible for: 

1) To give read the ranges and options.
2) Give values to the Workers in a synchronized way
3) Do all jobs that need to be synchronized.

You will have to create and run only ONE WorkerManager. When a WorkerManager is created, you then create the Workers (Threads) and you pass the WorkerManager as an argument.

Steps to create a new software.

1) Create a file similar to TrueCallerWorkerManager (Extends WorkerManager).

2) Create a worker class that extends TrueCallerWorker. Create a constructor similar to that of TrueCallerWorker. 

3) Create all abstract methods. Read the comments for more info of what each method does.

