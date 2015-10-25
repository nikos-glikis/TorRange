package com.circles.rippers.TorRange;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Utilities
{
    public static Connection createSqliteConnection(String path)
    {
        Connection c = null;
        try
        {
            Class.forName("org.sqlite.JDBC");
            if (! new File(new File(path).getParent()).exists())
            {
                new File(new File(path).getParent()).mkdirs();
            }

            c = DriverManager.getConnection("jdbc:sqlite:" + path);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return c;
    }

    public static String postRequest(String targetURL, String postParams)
    {
        return postRequest(targetURL, postParams, false, "");
    }
    public static String postRequest(String targetURL, String postParams, boolean returnCookies)
    {
        return postRequest(targetURL, postParams, returnCookies, "");
    }
    public static String postRequest(String targetURL, String postParams, boolean returnCookies, String givenCookies)
    {
        URL url;
        HttpURLConnection connection = null;
        try {

            // Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            if ( givenCookies != null && !givenCookies.equals(""))
            {
                givenCookies = givenCookies.replace("Cookie: ","");
                connection.setRequestProperty("Cookie",givenCookies);
            }
            connection.setRequestProperty("Content-Length", ""
                    + Integer.toString(postParams.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            if (returnCookies)
            {
                connection.setInstanceFollowRedirects(false);
            }
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection
                    .getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();
            Scanner sc = new Scanner(is);
            StringBuffer response = new StringBuffer();
            while (sc.hasNext())
            {
                response.append(sc.nextLine()+"\n");
            }

            if (!returnCookies)
            {
                return response.toString();
            }
            else
            {
                String cookies = "";
                Map<String,List<String>> map = connection.getHeaderFields();
                for (Map.Entry<String, List<String>> entry : map.entrySet())
                {
                    //System.out.println(entry.getKey() + "/" + entry.getValue());
                    if (entry.getKey() != null && entry.getKey().equals("Set-Cookie"))
                    {
                        List<String> l = entry.getValue();
                        Iterator<String> iter = l.iterator();
                        for (String c : l)
                        {
                            cookies += c +"; ";
                        }
                    }
                }
                if (cookies.equals(""))
                {
                    return null;
                }
                else
                {
                    return cookies;
                }
            }

        }
        catch (Exception e)
        {

            e.printStackTrace();
            System.out.println("e");
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Ignores https errors.
     */
    static public void trustEverybody()
    {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
            public X509Certificate[] getAcceptedIssuers(){return null;}
            public void checkClientTrusted(X509Certificate[] certs, String authType){}
            public void checkServerTrusted(X509Certificate[] certs, String authType){}
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            ;
        }
    }

    public static String readFile(String path) throws IOException
    {
        return readFileEncoding(path, Charset.forName("UTF-8"));
    }

    public static String readFileEncoding(String path, Charset encoding)  throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    public static String readUrl(String url, String cookie) throws FileNotFoundException
    {
        return readUrl(url, cookie,"utf-8");
    }

    public static String readUrl(String url ) throws FileNotFoundException
    {
        return readUrl(url, "","utf-8");
    }
    public static String readUrl(String url, String cookie, String encoding) throws FileNotFoundException
    {
        try
        {
            URL oracle = new URL(url);
            cookie = cookie.replace("Cookie: ","");
            HttpURLConnection conn = (HttpURLConnection) oracle.openConnection();
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
            conn.setRequestProperty("Cookie",cookie);
            BufferedReader in = new BufferedReader( new InputStreamReader(conn.getInputStream(), encoding ) );

            String inputLine;
            StringBuffer sb = new StringBuffer();
            while ((inputLine = in.readLine()) != null)
            {
                sb.append(inputLine + "\n");

            }
            in.close();
            return sb.toString();
        }
        catch (FileNotFoundException e)
        {
            throw e;
            //System.out.println("_404 e");
        }

        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    public static String cut(String from, String to, String t)
    {
        String  text = new String(t);
        text = text.substring(text.indexOf(from)+from.length(),text.length());
        text = text.substring(0,text.indexOf(to));
        return text;
    }

    public static String stringToMD5(String data) throws Exception
    {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        messageDigest.update(data.getBytes());
        byte[] digest = messageDigest.digest();

        StringBuffer sb = new StringBuffer();
        for (byte b : digest)
        {
            sb.append(Integer.toHexString((int) (b & 0xff)));
        }
        return sb.toString();
    }

    public static  String getIp()
    {
        try
        {
            return Utilities.readUrl("http://cpanel.com/showip.shtml");
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
