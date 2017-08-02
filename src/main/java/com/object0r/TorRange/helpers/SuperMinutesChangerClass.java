package com.object0r.TorRange.helpers;

public class SuperMinutesChangerClass
{
    static int[] barriers = {1, 60, 60 * 24, 60 * 24 * 7, 60 * 24 * 365, Integer.MAX_VALUE};
    static String[] text = {"min", "hr", "day", "week", "year"};

    public static String minutesToHumanReadable(int minutes)
    {
        String toReturn = "";
        for (int i = 1; i < barriers.length; i++)
        {
            if (minutes < barriers[i])
            {
                int ammount = (minutes / barriers[i - 1]);
                toReturn = ">" + (ammount) + " " + text[i - 1];
                if (ammount > 1)
                {
                    toReturn += "s";
                }
                break;
            }
        }
        return toReturn;
    }
}
