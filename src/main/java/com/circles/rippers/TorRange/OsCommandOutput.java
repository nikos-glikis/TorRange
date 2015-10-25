package com.circles.rippers.TorRange;

public class OsCommandOutput
{
    String standardOutput;
    String errorOutput;
    public OsCommandOutput(String s, String e)
    {
        this.standardOutput = s;
        this.errorOutput = e;
    }

    public String getStandardOutput()
    {
        return standardOutput;
    }

    public void setStandardOutput(String standardOutput)
    {
        this.standardOutput = standardOutput;
    }

    public String getErrorOutput()
    {
        return errorOutput;
    }

    public void setErrorOutput(String errorOutput)
    {
        this.errorOutput = errorOutput;
    }
}
