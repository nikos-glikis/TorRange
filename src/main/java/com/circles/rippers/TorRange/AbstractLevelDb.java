package com.circles.rippers.TorRange;

import java.util.ArrayList;
import java.util.HashMap;

public interface AbstractLevelDb
{

    void put(String key, String value);
    void delete(String key);
    String getAsString(String key);
    byte[] get(String key);
    void close();
    void copyTo(AbstractLevelDb destination);
    void deleteAll();
    void batchPut(HashMap p);
    void batchDelete(ArrayList<String> p);
}
