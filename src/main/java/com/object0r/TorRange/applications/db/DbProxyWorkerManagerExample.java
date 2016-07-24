package com.object0r.TorRange.applications.db;

public class DbProxyWorkerManagerExample extends DbProxyWorkerManager
{

    public DbProxyWorkerManagerExample(String iniFilename, Class workerClass)
    {
        super(iniFilename, workerClass);
    }

    @Override
    public void prepareForExit()
    {

    }

    @Override
    public void readOptions(String filename)
    {
       /*

         dbConnectionUrl = "jdbc:mysql://domain.com/dbName";
         dbIdColumn = "id";
         dbValuesTable = "table";
         dbValueColumn = "column";
         dbConnectionUsername = "root";
         dbConnectionPassword = "";

       */
    }
}
