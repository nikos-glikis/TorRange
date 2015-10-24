package com.circles.rippers.TorRange;


import java.io.*;
import java.net.Socket;

public class TorConnection
{
    private int socksPort;
    private int controlPort;
    private String password;
    static int DEFAULT_SOCKS_PORT = 10100;
    static int DEFAULT_CONTROL_PORT = 20100;
    static String DEFAULT_PASSWORD = "";
    static String tmpDir;

    public TorConnection()
    {
        this(DEFAULT_CONTROL_PORT, DEFAULT_SOCKS_PORT, DEFAULT_PASSWORD);
    }

    public TorConnection(int offset)
    {
        this(DEFAULT_CONTROL_PORT+offset, DEFAULT_SOCKS_PORT+offset, DEFAULT_PASSWORD);
        //System.out.println(offset);
    }

    public TorConnection(int socksPort, int controlPort, String password)
    {
        this.socksPort = socksPort;
        //System.out.println("Port is: "+this.socksPort);
        this.controlPort = controlPort;
        this.password = password;
        if (OsHelper.isLinux())
        {

            tmpDir = "/tmp/_toortools/tortmp/";
            if (!new File(tmpDir).exists()) {
                new File(tmpDir).mkdirs();
            }
            try
            {
                PrintWriter pr = new PrintWriter(new FileOutputStream(tmpDir+controlPort));
                pr.println("(echo authenticate '\"\"'; echo signal newnym; echo quit) | nc localhost "+controlPort);
                pr.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        //connect();
    }

    public void changeIp()
    {
        try
        {
            String ip = Utilities.getIp();
            //System.out.println("old ip: "+ip);
            if (OsHelper.isWindows())
            {
                Socket echoSocket = new Socket("localhost", controlPort);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                out.println("authenticate \""+password+"\"");
                out.println("signal newnym");
                out.println("quit");
                out.close();
                /*Scanner sc = new Scanner(in);
                while (sc.hasNext())
                {
                    System.out.println(sc.nextLine());
                }*/
            }
            else
            {
                //(echo authenticate '""'; echo signal newnym; echo quit) | nc localhost 9051
                //System.out.println(OsHelper.runCommandAndGetOutput(" echo authenticate '\"\"'; echo signal newnym; echo quit   | nc localhost "+controlPort).getErrorOutput());
                //String command = "/usr/bin/printf \"authenticate \\\"\\\"\\nsignal newnym\\nquit\\n\" | /bin/nc localhost " + controlPort;
                String command = "sh "+tmpDir+controlPort ;
                //System.out.println(command);
                OsCommandOutput out = OsHelper.runCommandAndGetOutput(command);
                //System.out.println(out.getStandardOutput());
                //System.out.println(out.getErrorOutput());
            }

            Thread.sleep(10000);
            String newIp = Utilities.getIp();
            //System.out.println("new ip: "+newIp);


        }
        catch (Exception e)
        {
            System.out.println("Exception happened. (tor change ip)");
            String newIp = Utilities.getIp();
            //System.out.println("new ip: "+newIp);
            //e.printStackTrace();

        }
    }

    boolean connect()
    {
        try
        {
            String path = "/tmp/tor/"+getSocksPort();
            if (!new File(path).exists()) {
                new File(path).mkdirs();
                //System.out.println("Does not exist");

            }
            //System.out.println("tor --RunAsDaemon 0 --CookieAuthentication 0 --HashedControlPassword \""+password+"\" --ControlPort " + controlPort + " --SocksPort " + socksPort);
            String command = "tor --RunAsDaemon 0 --CookieAuthentication 0 --HashedControlPassword \""+password+"\" --ControlPort " + controlPort + " --SocksPort " + socksPort +" --DataDirectory  /tmp/tor/"+getSocksPort();
            System.out.println(command);

            new CommandRunner(command);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }

    public int getSocksPort()
    {
        return socksPort;
    }

    public int getControlPort()
    {
        return controlPort;
    }
}
