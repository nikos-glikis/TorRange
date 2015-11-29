package com.circles.rippers.TorRange;

import org.ini4j.Ini;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

abstract public class DbWorkerManager  extends WorkerManager
{

    static Connection dbConnection;
    static String dbConnectionClass;
    static String dbConnectionUrl;
    static String dbConnectionUsername;
    static String dbConnectionPassword;
    static String dbValuesTable;
    static String dbIdColumn;
    static String dbValueColumn;
    static String dbFetchSize;

    static DbRangeResult dbRangeResult;

    public DbWorkerManager(String iniFilename)
    {
        super(iniFilename);
    }

    public synchronized String getNextEntry()
    {
        try
        {
            try
            {
                String stringId = basicGetNextEntry();
                int id = Integer.parseInt(stringId);
                if (dbConnection == null )
                {
                    Class.forName(dbConnectionClass);
                    dbConnection = DriverManager.getConnection(dbConnectionUrl, dbConnectionUsername, dbConnectionPassword);
                }

                if (dbRangeResult == null || id > dbRangeResult.end)
                {
                    fetchNewResult(id);
                }

                String value = dbRangeResult.getValue(id);
                while (value == null)
                {
                    if (id > dbRangeResult.end)
                    {
                        fetchNewResult(id);
                    }
                    else
                    {
                        value=dbRangeResult.getValue(id);
                    }
                }
                return value;

            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(0);
            }
       }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    private void fetchNewResult(int id)
    {
        try
        {
            dbRangeResult = new DbRangeResult();
            dbRangeResult.setStart(id);
            int end = id+Integer.parseInt(dbFetchSize);
            dbRangeResult.setEnd(end);
            Statement st = dbConnection.createStatement();
            String query = "SELECT `"+dbIdColumn+"`,`"+dbValueColumn+"` FROM `"+dbValuesTable+"` WHERE  `"+dbIdColumn+"` BETWEEN "+id+" AND "+(end)+"";
            System.out.println(query);
            ResultSet rs = st.executeQuery(query);
            while (rs.next())
            {
                //System.out.println(rs.getString(1));
                dbRangeResult.addValue(rs.getInt(1), rs.getString(2));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void readGeneralOptions(String filename)
    {
        try
        {
            basicReadGeneralOptions(filename);
            Ini prefs = new Ini(new File(filename));
            dbConnectionUrl = (prefs.get("dbInfo", "dbConnectionUrl"));
            dbConnectionClass = (prefs.get("dbInfo", "dbConnectionClass"));
            dbConnectionPassword = (prefs.get("dbInfo", "dbConnectionPassword"));
            dbFetchSize = (prefs.get("dbInfo", "dbFetchSize"));
            if (dbFetchSize == null)
            {
                dbFetchSize = "200";
            }

            dbConnectionUsername = (prefs.get("dbInfo", "dbConnectionUsername"));

            dbValuesTable = (prefs.get("dbInfo", "dbValuesTable"));
            dbIdColumn = (prefs.get("dbInfo", "dbIdColumn"));
            dbValueColumn = (prefs.get("dbInfo", "dbValueColumn"));

            if (dbConnectionUrl == null)
            {
                throw new Exception("Connection url is not set.");
            }

            if (dbConnectionPassword == null || dbConnectionUsername == null)
            {
                throw new Exception("Db Username or password is not set.");
            }

            if (dbConnectionClass == null)
            {
                dbConnectionClass = "com.mysql.jdbc.Driver";
            }

            System.out.println("dbConnectionClass:" +dbConnectionClass);
            System.out.println("dbConnectionUrl:" +dbConnectionUrl);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
