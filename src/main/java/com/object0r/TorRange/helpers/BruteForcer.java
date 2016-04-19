package com.object0r.TorRange.helpers;

import java.util.Arrays;
import java.util.Date;

public class BruteForcer
{
    public static String password = "AZZZZS";
    public char[] charset;

    private char[] currentGuess = new char[1];

    int minLength = 1;
    int maxLength = Integer.MAX_VALUE;

    boolean hasMore = true;

    public boolean hasMore()
    {
        return this.hasMore;
    }

    public String getNext()
    {
        increment();
        if (this.toString().length() > maxLength)
        {
            hasMore = false;
            return "";
        }
        else
        {
            return this.toString();
        }
    }

    public static void main(String args[])
    {
        BruteForcer in = null;
        try
        {
            in = new BruteForcer("ABCDEFGHIJKLMNOPQRSTUVWXYZ",6,6);

            String attempt = new String();
            Date start = new Date();
            while (true)
            {
                if (attempt.equals(password))
                {
                    Date end = new Date();
                    System.out.println("Password: " + attempt + "\nTotal time to crack: " + ((end.getTime() - start.getTime()) / 1000) + " seconds." + "\n");
                    break;
                }
                attempt = in.toString();
                // System.out.println("Tried: " + attempt);
                in.increment();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public BruteForcer(String stringCharset) throws Exception
    {
        this(stringCharset, 1, Integer.MAX_VALUE);
    }

    public BruteForcer(String stringCharset, int minLength, int maxLength) throws Exception
    {
        this.charset = stringCharset.toCharArray();
        if (minLength < 1)
        {
            throw new Exception("BruteForcer, minLength has an invalid value");
        }
        if (minLength > maxLength)
        {
            throw new Exception("BruteForcer, minLength has greater value than maxLength");
        }
        String start = "";
        for (int i = 0; i < minLength; i++)
        {
            start = start + charset[0];
        }
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.setStart(start);
    }

    public void setStart(String start)
    {
        currentGuess = start.toCharArray();
        //TODO check if start agrees with charset (characters and length)
    }

    public void increment()
    {
        //TODO check min/maxLenth
        int index = currentGuess.length - 1;
        while (index >= 0)
        {
            if (currentGuess[index] == charset[charset.length - 1])
            {
                if (index == 0)
                {
                    currentGuess = new char[currentGuess.length + 1];
                    Arrays.fill(currentGuess, charset[0]);
                    break;
                } else
                {
                    currentGuess[index] = charset[0];
                    index--;
                }
            } else
            {
                currentGuess[index] = charset[Arrays.binarySearch(charset, currentGuess[index]) + 1];
                break;
            }
        }
    }

    public String toString()
    {
        return String.valueOf(currentGuess);
    }
}