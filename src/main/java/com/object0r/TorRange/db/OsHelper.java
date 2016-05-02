package com.object0r.TorRange.db;

import com.object0r.TorRange.datatypes.OsCommandOutput;

public class OsHelper
{
    static int OS_TYPES_UNKNOWN = 0;
    static int OS_TYPES_WINDOWS = 1;
    static int OS_TYPES_LINUX = 2;

    static int getOs()
    {
        String os = System.getProperty("os.name");

        if (os.indexOf("Windows") > -1)
        {
            return OS_TYPES_WINDOWS;
        }
        else if (os.indexOf("Linux") > -1)
        {

            return OS_TYPES_LINUX;
        }
        else
        {
            return OS_TYPES_UNKNOWN;
        }
    }

    public static boolean isWindows()
    {
        return OsHelper.getOs() == OS_TYPES_WINDOWS;
    }

    public static boolean isLinux()
    {
        return getOs() == OS_TYPES_LINUX;
    }
    class CommandOutput
    {

    }
    public static boolean runCommand(String command) throws Exception
    {
        try
        {
            String s = null;
            //System.out.println(command);
            Process p = Runtime.getRuntime().exec(command);

            return true;


        }
        catch (Exception e)
        {
            Exception e2 =new  Exception("Some error happened when trying to run the command.");
            e.printStackTrace();
            e2.setStackTrace(e.getStackTrace());
            throw e2;
        }

    }

    public static OsCommandOutput runCommandAndGetOutput(String command) throws Exception
    {
        try
        {
            String s = null;
            Process p = Runtime.getRuntime().exec(command);

            /*BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            StringBuffer sb = new StringBuffer();
            StringBuffer errorBuffer = new StringBuffer();
            //System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                sb.append(s);
                //System.out.println(s);
            }

            // read any errors from the attempted command
            //System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                errorBuffer.append(s);
            }
            */
            return new OsCommandOutput("", "");


        }
        catch (Exception e)
        {
            Exception e2 =new  Exception("Some error happened when trying to run the command.");
            e2.setStackTrace(e.getStackTrace());
            throw e2;
        }
    }
}
