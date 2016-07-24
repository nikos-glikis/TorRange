package com.object0r.TorRange.applications.db;

import java.util.HashMap;
import java.util.Vector;

public class DbRangeResult
{
    public int start;
    public int end;
    public HashMap<Integer, String> values = new HashMap<Integer, String>();
    public HashMap<Integer, HashMap<String, String>> allValues = new HashMap<Integer, HashMap<String, String>>();


    public void addValue(int index, String value)
    {
        values.put(index, value);
    }

    public String getValue(int index)
    {
        return values.get(index);
    }

    public String getAllValues(int index)
    {
        return values.get(index);
    }

    public void addToAllValues(int index, HashMap<String, String> values)
    {
        allValues.put(index, values);
    }

    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }

    public int getEnd()
    {
        return end;
    }

    public void setEnd(int end)
    {
        this.end = end;
    }
}
