package com.object0r.TorRange.applications.db;

import com.object0r.TorRange.ProxyRangeWorkerManager;
import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.datatypes.EntriesRange;
import com.object0r.toortools.ConsoleColors;
import org.ini4j.Ini;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

abstract public class DbProxyWorkerManager extends ProxyRangeWorkerManager
{

    static Connection dbConnection;
    static public String dbConnectionClass;
    static public String dbConnectionUrl;
    static public String dbConnectionUsername;
    static public String dbConnectionPassword;
    static public String dbValuesTable;
    static public String dbIdColumn;
    static public String dbValueColumn;
    static public String dbFetchSize;
    static public DbRangeResult dbRangeResult;

    /**
     * Derived from dbValueColumn
     */
    static ArrayList<String> columns = new ArrayList<String>();

    /**
     * Derived from dbValueColumn - Used in select statements.
     */
    static String columnsString = "";

    public DbProxyWorkerManager(String iniFilename, Class<? extends DbProxyWorker> workerClass, Class<? extends ProxyWorkerManager> managerClass)
    {
        super(iniFilename, workerClass, managerClass);
        prepareColumns();
    }

    private void prepareColumns()
    {
        if (dbValueColumn.contains(","))
        {
            StringTokenizer stringTokenizer = new StringTokenizer(dbValueColumn, ",");
            while (stringTokenizer.hasMoreTokens())
            {
                columns.add(stringTokenizer.nextToken());
            }
        }
        else
        {
            columns.add(dbValueColumn);
        }

        for (String column : columns)
        {
            columnsString = columnsString + "`" + column.trim().replace("`", "") + "`,";
        }
        columnsString = columnsString.substring(0, columnsString.length() - 1);

    }

    public int getNextIdInt()
    {
        String stringId = basicGetNextEntry();
        int id = Integer.parseInt(stringId);
        if (dbRangeResult == null || id > dbRangeResult.end)
        {
            fetchNewResult(id);
        }
        return id;
    }

    public synchronized HashMap<String, String> getNextEntryMap()
    {
        HashMap<String, String> value = new HashMap<String, String>();
        try
        {
            try
            {
                initDb();
                int id = getNextIdInt();
                value = dbRangeResult.getAllValues(id);
                while (value == null)
                {
                    id = getNextIdInt();
                    value = dbRangeResult.getAllValues(id);
                }
                return value;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        return value;
    }

    public synchronized String getNextEntry()
    {
        try
        {
            try
            {
                initDb();
                int id = getNextIdInt();
                String value = dbRangeResult.getValue(id);
                while (value == null)
                {
                    id = getNextIdInt();
                    value = dbRangeResult.getValue(id);
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

    private void initDb()
    {
        try
        {
            if (dbConnection == null)
            {
                Class.forName(dbConnectionClass);
                dbConnection = DriverManager.getConnection(dbConnectionUrl, dbConnectionUsername, dbConnectionPassword);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ConsoleColors.printRed("There was some error connecting to the database. Exiting.");
            System.exit(-1);
        }
    }

    private void fetchNewResult(int id)
    {
        try
        {
            dbRangeResult = new DbRangeResult();
            dbRangeResult.setStart(id);
            int end = id + Integer.parseInt(dbFetchSize);
            dbRangeResult.setEnd(end);
            Statement st = dbConnection.createStatement();
            //String query = "SELECT `" + dbIdColumn + "`,`" + dbValueColumn + "` FROM `" + dbValuesTable + "` WHERE  `" + dbIdColumn + "` BETWEEN " + id + " AND " + (end) + "";
            String query = "SELECT `" + dbIdColumn + "`," + columnsString + " FROM `" + dbValuesTable + "` WHERE  `" + dbIdColumn + "` BETWEEN " + id + " AND " + (end) + "";

            ResultSet rs = st.executeQuery(query);
            while (rs.next())
            {
                dbRangeResult.addValue(rs.getInt(1), rs.getString(2));
                int i = 2;
                HashMap<String, String> hashMap = new HashMap<String, String>();

                for (String column : columns)
                {
                    hashMap.put(column.trim(), rs.getString(i++));
                }

                dbRangeResult.addToAllValues(rs.getInt(1), hashMap);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ConsoleColors.printRed("There was some error getting data from the database. Exiting now.");
            System.exit(-1);
        }
    }

    public Vector<EntriesRange> getUserRanges()
    {
        Vector<EntriesRange> ranges = new Vector<EntriesRange>();
        try
        {
            initDb();
            Statement st = dbConnection.createStatement();

            //Find out MIN
            String query = "SELECT MIN(`" + dbIdColumn + "`) FROM `" + dbValuesTable + "` ";
            //System.out.println(query);
            ResultSet rs = st.executeQuery(query);
            rs.next();
            System.out.println("Min is: " + rs.getInt(1));
            int min = rs.getInt(1);
            //Max
            query = "SELECT MAX(`" + dbIdColumn + "`) FROM `" + dbValuesTable + "` ";
            //System.out.println(query);
            rs = st.executeQuery(query);
            rs.next();
            int max = rs.getInt(1);
            System.out.println("Max is: " + max);
            EntriesRange entriesRange = new EntriesRange(min, max);
            ranges.add(entriesRange);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ConsoleColors.printRed("There was some error reading min/max values id. Please recheck database data.");
            System.exit(-1);
        }
        return ranges;
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

            System.out.println("dbConnectionClass: " + dbConnectionClass);
            System.out.println("dbConnectionUrl: " + dbConnectionUrl);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
