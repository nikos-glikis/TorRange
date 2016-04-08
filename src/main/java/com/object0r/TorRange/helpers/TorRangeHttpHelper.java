package com.object0r.TorRange.helpers;


import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TorRangeHttpHelper
{
    public static String postRequest(String targetURL, String postParams, Proxy p, boolean returnCookies, String givenCookies)
    {
        if (p == null)
        {
            System.out.println("Proxy is null, returning as a precaution.");
            return"";
        }
        URL url;
        HttpURLConnection connection = null;
        try {

            // Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection(p);
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
}
