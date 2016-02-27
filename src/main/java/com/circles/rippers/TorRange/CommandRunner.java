package com.circles.rippers.TorRange;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandRunner extends Thread
{
    String command ;
    boolean forever;
    public CommandRunner(String command, boolean runForever)
    {
        this.command = command;
        this.forever = runForever;
        this.start();
    }

    public void run()
    {
        try
        {
            runCommand(command);

            if (forever)
            {
                while (true)
                {
                    try
                    {
                        Thread.sleep(5000);
                        runCommand(command);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public  boolean runCommand(String command) throws Exception
    {
        try
        {
            String s = null;
            Process p = Runtime.getRuntime().exec(command);

            p.waitFor();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";

            while ((line = b.readLine()) != null) {
                //System.out.println(line);
            }

           /* b = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = b.readLine()) != null) {
                System.out.println(line);
            }
*/
            b.close();
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
}
