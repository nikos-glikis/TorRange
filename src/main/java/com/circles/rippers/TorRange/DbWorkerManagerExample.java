package com.circles.rippers.TorRange;

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
        dbConnectionUrl = "jdbc:mysql://x4.glikis.net/alexa_top_import";
        dbIdColumn= "id";
        dbValuesTable= "alexa_milion";
        dbValueColumn= "domain";
        dbConnectionUsername= "root";
        dbConnectionPassword= "goldbitcoinvps234!";
    }
}
