package com.object0r.TorRange.db;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;

public class DB
{
    Connection dBconnection, dBconnection2 ;
    public synchronized void put(Object key, Object value)
    {
        try
        {
            Statement st = dBconnection.createStatement();
            try
            {
                st.execute("INSERT INTO `values` VALUES ('"+key.toString().replace("'", "''")+"', '"+value.toString().replace("'","''")+"')");
            }
            catch (Exception e)
            {
                //e.printStackTrace();
                //System.exit(0);
                ;
                st.close();
                st = dBconnection.createStatement();
            }


            st.execute("UPDATE `values` SET `value` = '" + value.toString().replace("'", "''") + "' WHERE  `key` = '" + key.toString().replace("'", "''") + "'") ;
            st.close();
            st = dBconnection2.createStatement();

            try
            {

                st.execute("INSERT INTO `values` VALUES ('"+key.toString().replace("'", "''")+"', '"+value.toString().replace("'","''")+"')");

            }
            catch (Exception e)
            {
                //e.printStackTrace();
                //System.exit(0);
                ;
                st.close();
                st = dBconnection.createStatement();
            }

            st.execute("UPDATE `values` SET `value` = '" + value.toString().replace("'", "''") + "' WHERE  `key` = '" + key.toString().replace("'", "''") + "'") ;
            st.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public Vector<String> getAllKeys()
    {
        Vector<String> values = new Vector<String>();
        try
        {
            Statement st = dBconnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT `key` FROM `values`");
            while (rs.next())
            {
                values.add(rs.getString(1));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return values;
    }

    public synchronized String  get(int key)
    {
        return this.get(new Integer(key).toString());
    }

    public synchronized String  get(String key)
    {
        String value=null;
        try
        {
            Statement st = dBconnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT `value` FROM `values` WHERE `key` = '" + key.replace("'", "''") + "'");

            if (rs.next())
            {
                value = rs.getString(1);
            }
            rs.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return value;
    }
    public DB()
    {
        this("DefaultSession", "DefaultDb");
    }
    public synchronized HashMap<String, String> getAll()
    {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        try
        {
            Statement st = dBconnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM  `values` ");
            while (rs.next())
            {
                hashMap.put(rs.getString(1), rs.getString(2));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return hashMap;
    }
    public  DB(String SESSION_NAME, String name)
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            if (new File("sessions/"+SESSION_NAME+"/databases/"+name).exists() && new File("sessions/"+SESSION_NAME+"/databases/"+name).isDirectory())
            {

            }
            else
            {
                new File("sessions/"+SESSION_NAME+"/databases/"+name).mkdirs();
                new File("sessions/"+SESSION_NAME+"/databases_bak/"+name).mkdirs();
            }
            dBconnection = DriverManager.getConnection("jdbc:sqlite:sessions/" + SESSION_NAME + "/databases/" + name + "/" + name + ".db");
            dBconnection2 = DriverManager.getConnection("jdbc:sqlite:sessions/"+SESSION_NAME+"/databases_bak/"+name+"/"+name+".db");
            Statement st = dBconnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='values';");
            if (rs.next())
            {
                if (rs.getInt(1) == 0)
                {
                    st.execute("create table `values`(`key` varchar(50), `value` varchar(50));");
                    st.execute("CREATE UNIQUE INDEX `key` ON `values`(`key`)");

                }
            }

            //db2
            st =dBconnection2.createStatement();
            rs = st.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='values';");
            if (rs.next())
            {
                if (rs.getInt(1) == 0)
                {
                    st.execute("create table `values`(`key` varchar(50), `value` varchar(50));");
                    st.execute("CREATE UNIQUE INDEX `key` ON `values`(`key`)");

                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }
}