Threaded Range Processor, with TOR Integration
==============================================

Introduction
-------------

WARNING - 
Documentation is a little bit behind, as I didn't intented to release this. I will update as soon as I have some time.

This repository contains a library that can read one or more Ranges, and process each number in that process. It also contains an application of that library, that downloads and the ip of each thread for each number in the range.

Each thread is connected to a tor, which it has control over. When needed the ip of the Thread can be changed, and a readUrl method is provided that downloads some url, using TOR.

Requirements
-----------

To run this you need a linux box with tor and java (obviously) installed.

TOR path must be added in the PATH of the linux box tor can be manually runned.

This has been tested only for root user.

Configuration
-------------

Create a .ini file similar to inis/example.ini

    [WorkerManager]
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
    


If you want to consume a [mysql|sqlite|JDBC Compatible] database you can add:

    [dbInfo]
    dbConnectionUrl=jdbc:mysql://localhost/mydatabase
    dbConnectionUsername=username
    dbConnectionPassword=password
    dbValuesTable=table
    dbIdColumn=id
    dbValueColumn=value
    #if below is not set, default is: com.mysql.jdbc.Driver
    dbConnectionClass=com.mysql.jdbc.Driver
    #default is 200
    dbFetchSize=100
    
And for a main sample see DbMain.java (only difference is: WorkerManager torRangeSimpleExampleManager = new DbWorkerManagerExample(args[0]);)

DbWorkerManagerExample class:

    public class DbWorkerManagerExample extends DbWorkerManager
    {
    
        public DbWorkerManagerExample(String iniFilename)
        {
            super(iniFilename);
        }
    
        @Override
        public void prepareForExit()
        {
    
        }
    
        @Override
        public void readOptions(String filename)
        {
        
        }
    }

and DbMain:

    package com.circles.rippers.TorRange;
    
    
    public class MainDb
    {
    
        public static void main(String[] args)
        {
            if (args.length == 0) {
                System.out.println("No session ini in arguments.");
                System.out.println("Usage: ");
                System.out.println("java -cp java -cp out/production/TorRange-ripper/:lib/* com.circles.rippers.TorRange.Main inis/example.ini");
    
                System.exit(0);
            }
    
            try
            {
                WorkerManager torRangeSimpleExampleManager = new DbWorkerManagerExample(args[0]);
    
                System.out.println("Starting "+torRangeSimpleExampleManager.getThreadCount()+" Threads");
                for (int i = 0 ; i < torRangeSimpleExampleManager.getThreadCount(); i++)
                {
                    new TorRangeSimpleExampleWorker( torRangeSimpleExampleManager, i).start();
                    Thread.sleep(2000);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

Threads is how much Threads you want to run, prefix if it exists is the prefix to use when processing the numbers.

Base section for the ini is the [WorkerManager] but you can add your own after that.

Running
-------

To run this, 

1) execute ./start_tor_instances in one screen window (Or terminal window). This file is automatically generated according to your settings. You must run main at least once for this file to be created. 

2) ./start

How to create your own RangeProcessor
--------------------------------------

To create your own RangeProcessor, take a look at TorRangeSimpleExampleManager and TorRangeSimpleExampleWorker.

Worker is a thread that performs some work on an Entry (number) from the Range.

WorkerManager is responsible for: 

1) To give read the ranges and options.
2) Give values to the Workers in a synchronized way
3) Do all jobs that need to be synchronized.

You will have to create and run only ONE WorkerManager. When a WorkerManager is created, you then create the Workers (Threads) and you pass the WorkerManager as an argument.

Steps to create a new software.

1) Create a file that Extends WorkerManager. If you want to consume a database table, extend DbWorkerManager or use default.

2) Create a worker class that extends TorWorker. Create a constructor similar to that of TorRangeSimpleExampleWorker. 

3) Create all abstract methods. Read the comments for more info of what each method does.

4) Overwrite ProxyRangeWorkerManager.getUserRanges(); to give your ranges. If you have rangesfile=somefile.txt in the ini, the ranges from this file are loaded.

Progress Report
===============

At any point press enter to see the progress report. To make your own report, override the public void printReport() method in WorkerManager(). Old report is available at printGeneralReport.

Install Jar in local maven repo
===============================

mvn install:install-file  -DgroupId=com.object0r -DartifactId=TorRange -Dversion={version} -Dpackaging=jar -Dfile=target/TorRange-{version}-jar-with-dependencies.jar
mvn install:install-file  -DgroupId=com.object0r -DartifactId=TorRange -Dversion=1.0.3 -Dpackaging=jar -Dfile=target/TorRange-1.0.3-jar-with-dependencies.jar

On above adjust the version.

In pom.xml

        <dependency>
            <groupId>com.object0r</groupId>
            <artifactId>TorRange</artifactId>
            <version>1.0.3</version>
        </dependency>
        
        
Applications:
=============

1) Database

- TorRange can consume a database (effitiently). 
- Details described above

2) Wordlist:

See package com.object0r.TorRange. 
Requires:
    
    [wordlist]
    passwordfile=/root/wordlists/rockyou.txt

in the .ini file along with the other TorRange requests.
