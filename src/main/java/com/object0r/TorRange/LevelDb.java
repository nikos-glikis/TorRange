package com.object0r.TorRange;

import org.apache.commons.io.FileUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;
import static org.fusesource.leveldbjni.JniDBFactory.factory;

public class LevelDb extends com.object0r.toortools.db.LevelDb
{
    public LevelDb(String filename)
    {
        super(filename);
    }

    public LevelDb(String filename, boolean destroy)
    {
        super(filename, destroy);
    }

    public LevelDb(String directory, String filename, boolean destroy)
    {
        super(directory, filename, destroy);
    }
       /* DB db;
        String DATABASES_PATH = "dbs";
        String filename;
        public LevelDb(String filename)
        {
            new File(DATABASES_PATH+"/"+filename).mkdirs();
            this.filename = filename;
            init(filename);
        }

        public LevelDb(String filename, boolean destroy)
        {
            this.filename = filename;
            if (destroy)
            {
                destroy();
            }
            init(filename);
        }
    public void exportKeysToFile(String filename)
    {
        try {
            int badCount=0, totalCount=0, found=0;
            long max= 0;
            PrintWriter pr = new PrintWriter(new FileOutputStream(filename));
            DBIterator iterator = db.iterator();
            try {
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    String key = asString(iterator.peekNext().getKey());
                    String value = asString(iterator.peekNext().getValue());
                    pr.println(key + " - "+value);
                    totalCount++;
                   *//* long thisOne = Long.parseLong(key.replace("+31","").replace("null",""));
                    if (thisOne > max) {
                        max = thisOne;
                    }

                    //TODO remove me
                    if (value.contains("\"name\":\"Name Available\"")) {
                        this.delete(key);
                    };*//*
                }
            } finally {
                pr.close();
                System.out.println("Total: " + totalCount);
                *//*System.out.println("Bad: "+badCount);
                System.out.println("Max: "+max);*//*
                // Make sure you close the iterator to avoid resource leaks.
                iterator.close();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
        void destroy()
        {
            try
            {
                FileUtils.deleteDirectory(new File(DATABASES_PATH+"/"+filename));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        public void deleteAll()
        {
            //TODO
        }
        void init(String filename)
        {
            try
            {
                Options options = new Options();

                options.createIfMissing(true);

                try {

                    this.db = factory.open(new File(DATABASES_PATH+"/"+filename), options);

                    // Use the db in here....
                    *//*System.out.println("db test");
                    db.put(bytes("Tampanew"), bytes("rocks sadsad!! new"));
                    String value = asString(db.get(bytes("Tampa")));
                    System.out.println(value);
                    value = asString(db.get(bytes("Tampanew")));
                    System.out.println(value);
                    System.exit(0);*//*

                } finally {
                    // Make sure you close the db to shutdown the
                    // database and avoid resource leaks.
                    //db.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public void put(String key, String value)
        {

            this.db.put(bytes(key.toLowerCase()), bytes(value));
        }

        public String getAsString(String key)
        {
            return asString(get(key.toLowerCase()));
        }
        public  byte[] get(String key)
        {
            return this.db.get(bytes(key.toLowerCase()));
        }
        public  void close()
        {
            try
            {
                this.db.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        public  void delete(String key)
        {
            try
            {
                this.db.delete(key.toLowerCase().getBytes());

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public void copyTo(AbstractLevelDb destination)
        {
            try
            {
                DBIterator iterator = db.iterator();
                try {
                    int i = 1;
                    String[] values = new String[2000];
                    HashMap map = new HashMap();
                    for(iterator.seekToFirst(); iterator.hasNext(); iterator.next())
                    {
                        String key = asString(iterator.peekNext().getKey());
                        String value = asString(iterator.peekNext().getValue());
                        map.put(key,value);
                        if (i++ % 100000 == 0)
                        {
                            destination.batchPut(map);
                            map = new HashMap();
                            System.out.println(i + " " + key);
                        }
                    }
                } finally {
                    // Make sure you close the iterator to avoid resource leaks.
                    iterator.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public void batchPut(HashMap map)
        {
            //TODO better
            WriteBatch batch = db.createWriteBatch();

            try
            {

                String[] values = new String[map.size()*2];
                Iterator it = map.entrySet().iterator();
                int i = 0 ;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    //System.out.println(pair.getKey() + " = " + pair.getValue());
                    String key = pair.getKey().toString();
                    String value = pair.getValue().toString();

                    it.remove(); // avoids a ConcurrentModificationException
                    //System.out.println(key);
                    values[i++] = key;
                    values[i++] = value;
                    batch.put(key.toLowerCase().getBytes(), value.getBytes());
                    //put(key, value);
                    //System.out.println(getAsString(key));
                }
                db.write(batch);
                batch.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    public void batchDelete(ArrayList<String> list)
    {
        //TODO better
        WriteBatch batch = db.createWriteBatch();
        try
        {
            for (int i = 0; i<list.size(); i++)
            {
                String key = list.get(i);
                batch.delete(key.toLowerCase().getBytes());
            }
            db.write(batch);
            batch.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/
}