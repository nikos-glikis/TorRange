package com.object0r.TorRange;

public class DbProxyWorkerManagerExample extends DbProxyWorkerManager
{

    public DbProxyWorkerManagerExample(String iniFilename)
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
        dbConnectionUrl = "jdbc:mysql://domain.com/dbName";
        dbIdColumn= "id";
        dbValuesTable= "table";
        dbValueColumn= "column";
        dbConnectionUsername= "root";
        dbConnectionPassword= "";
    }
}
