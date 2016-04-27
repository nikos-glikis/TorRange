package com.object0r.TorRange;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;

public class TorConnection extends ProxyConnection
{
    private int socksPort;
    private int controlPort;
    private String password;
    static int DEFAULT_SOCKS_PORT = 10000;
    static int DEFAULT_CONTROL_PORT = 20000;
    static String DEFAULT_PASSWORD = "password1";
    static String tmpDir;

    public boolean connect()
    {
        try
        {
            String path = "/tmp/tor/"+getSocksPort();
            if (!new File(path).exists()) {
                new File(path).mkdirs();
                //System.out.println("Does not exist");

            }
            String directory;
            if (OsHelper.isWindows())
            {
                directory = "tmp\\tor\\"+getSocksPort();
            }
            else
            {
                directory = "tmp/tor/"+getSocksPort();
            }
            File f = new File(directory);

            if (!f.exists())
            {
                new File(directory).mkdirs();
                if (!new File(directory).exists())
                {
                    System.out.println("Error, directory does not exist.");
                    System.exit(0);
                }
            }
            String command=null;
            if (OsHelper.isLinux())
            {
                command = "tor --RunAsDaemon 0   --CookieAuthentication 1 --NewCircuitPeriod 300000   --ControlPort "+getControlPort()+" --SocksPort "+getSocksPort()+" --DataDirectory  "+directory+" --PidFile "+directory+"/my.pid --CookieAuthFile "+directory+"/cookie";
            }
            else if (OsHelper.isWindows())
            {
                directory = "tmp\\tor\\"+getSocksPort();
                command = "tor --RunAsDaemon 0   --CookieAuthentication 1   --NewCircuitPeriod 300000  --ControlPort "+getControlPort()+" --SocksPort "+getSocksPort()+" --DataDirectory  "+directory+" --CookieAuthFile "+directory+"\\cookie";
                //System.out.println(command);
                /*System.out.println("Windows are not yet supported.");
                System.exit(0);*/
            }
            else
            {
                System.out.println("Os Not supported");
                System.exit(0);
            }

            //System.out.println(command);
            /*if (isFirstConnect)
            {
                isFirstConnect = false;
                Thread.sleep(firstConnectSleepSeconds*1000);
            }*/
            //TODO check if tor is in path.

            /*Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command);
            pr.waitFor();
            System.out.println("Error code from tor is: "+pr.exitValue());*/
            System.out.println(command);
            new CommandRunner(command, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }

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
                //Write restart script.
                PrintWriter pr = new PrintWriter(new FileOutputStream(tmpDir+controlPort));
                pr.println("(echo authenticate '\""+password+"\"'; echo signal newnym; echo quit) | nc localhost "+controlPort);
                pr.close();

                //Write shutdown script.
                pr = new PrintWriter(new FileOutputStream(tmpDir+controlPort+".exit"));
                pr.println("(echo authenticate '\""+password+"\"'; echo SIGNAL SHUTDOWN) | nc localhost "+controlPort);
                pr.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        //connect();
    }

    public void closeTor()
    {
        try
        {
            if (OsHelper.isWindows())
            {
                try
                {
                    Socket echoSocket = new Socket("localhost", controlPort);
                    PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                    out.println("authenticate \""+password+"\"");
                    out.println("signal shutdown");
                    out.println("quit");
                    out.close();
                }
                catch (Exception e)
                {
                   System.out.println("Tor Close Error: "+e.toString());
                }

                /*Scanner sc = new Scanner(in);
                while (sc.hasNext())
                {
                    System.out.println(sc.nextLine());
                }*/
            }
            else
            {

                String command = "sh "+tmpDir+controlPort+".exit" ;
                OsCommandOutput out = OsHelper.runCommandAndGetOutput(command);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

                String command = "sh "+tmpDir+controlPort ;
                OsCommandOutput out = OsHelper.runCommandAndGetOutput(command);
            }

            Thread.sleep(10000);
            String newIp = Utilities.getIp();
            //System.out.println("new ip: "+newIp);


        }
        catch (Exception e)
        {
            System.out.println("Exception happened. (tor change ip)"+e);
            String newIp = Utilities.getIp();
            //System.out.println("new ip: "+newIp);
            //e.printStackTrace();

        }
    }

    @Override
    public void changeIpHttps()
    {
        changeIp();;
    }

    @Override
    public Proxy getProxy() {
        SocketAddress addr = new
                InetSocketAddress("localhost", getSocksPort());
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, addr);
        return proxy;
    }

    @Override
    public ProxyInfo getProxyInfo() {
        ProxyInfo proxyInfo = new ProxyInfo();
        try {
            proxyInfo.setType(ProxyInfo.PROXY_TYPES_SOCKS5);
        } catch (Exception e) { }
        proxyInfo.setHost("localhost");
        proxyInfo.setPort(this.getSocksPort()+"");
        return proxyInfo;
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
