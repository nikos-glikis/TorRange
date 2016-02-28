package com.circles.rippers.TorRange;

public class ReadUrlResult
{
    boolean successful =false;
    byte []body;
    Exception exception;

    public boolean isSuccessful()
    {
        return successful;
    }

    public void setSuccessful(boolean successful)
    {
        this.successful = successful;
    }

    public byte[] getBody()
    {
        return body;
    }

    public String getBodyAsString()
    {
        return new String(body);
    }

    public void setBody(byte[] body)
    {
        this.body = body;
    }

    public Exception getException()
    {
        return exception;
    }

    public void setException(Exception exception)
    {
        this.exception = exception;
    }
}
