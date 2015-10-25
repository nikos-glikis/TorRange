package com.circles.rippers.TorRange;

public class EntriesRange
{
    private long start;
    private long end;
    private long size;

    public EntriesRange(long start, long end)
    {
        this.start = start;
        this.end = end;
        size = end-start;
    }


    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getSize() {
        return size;
    }

    public String toString()
    {
        return "Start: "+start+ " End: "+end;
    }


}
