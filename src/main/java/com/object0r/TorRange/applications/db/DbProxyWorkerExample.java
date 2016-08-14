package com.object0r.TorRange.applications.db;

    import java.util.HashMap;
    import java.util.Map;
    import  com.object0r.TorRange.applications.db.DbProxyWorker;

    public class DbProxyWorkerExample extends DbProxyWorker
    {
        public DbProxyWorkerExample(DbProxyWorkerManagerExample manager, int id)
        {
            super(manager, id);
        }

        @Override
        void process(HashMap<String, String> values)
        {
            for (Map.Entry<String, String> entry : values.entrySet())
            {
                String key = entry.getKey();
                String value = entry.getValue();
                System.out.println("HashMap: Key: " + key + " value: " + value);
            }
            System.out.println("-----");
        }

        @Override
        protected void process(String entry)
        {
            System.out.println("Single: " + entry);
        }
    }
