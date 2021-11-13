package com.example.gottesdienstezug;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

// TODO as of 1 November 2021: Simplify the loaded content

public class MainActivity extends AppCompatActivity {
    Menu mMenu;
    String oldurl="",url;
    WebView webView;

    /* An instance of this class will be registered as a JavaScript interface */
    class MyJavaScriptInterface  {
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            Log.d("processHTML",html);
        }
    }

    private String getUrlSource(String site) throws IOException {
        //GNU Public, from ZunoZap Web Browser
        URL url = new URL(site);
        URLConnection urlc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder a = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            a.append(inputLine);
        in.close();

        return a.toString();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title);

        //finding view
        // loadWebsite = (Button) findViewById(R.id.loadwebsite);
        // loadHtml = (Button) findViewById(R.id.loadhtml);
        webView = (WebView) findViewById(R.id.webview);

        // Website URL
        oldurl="";
        url = "https://www.ref-zug.ch/zug-menzingen-walchwil/gottesdienste/";

        // static html
        final String sHtml = "<title>Gottesdienste in Zug</title>";
        //setting javascript enabled
        webView.getSettings().setJavaScriptEnabled(true);

        /* Register a new JavaScript interface called HTMLOUT */
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        //Enable can go forward
        Boolean canGoForward = webView.canGoForward();
        webView.canGoBack();

        //Enable can go Back
        if(webView.canGoBack()) {
            webView.goBack();
        }

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cmsg) {
                // check secret prefix
                if (cmsg.message().startsWith("MAGIC")) {
                    String msg = cmsg.message().substring(5); // strip off prefix

                    msg=msg.replaceAll("</head>","<style type=\"text/css\">.more{font-size:smaller !important;}</style></head>");
                    // msg=msg.replaceAll("<body>","<body><style type=\"text/css\">.text{font-size:smaller; !important}</style></head>");
                    msg=msg.replaceAll("<nav class=\"breadcrumb\"><ul><li><a href=\"/[a-z-]*/\">Home</a></li><li class=\"current\">Gottesdienste</li></ul></nav>", "");
                    // msg=msg.replaceAll("<header><h2 class=\"purple\">Gottesdienste </h2></header><p>&nbsp;&nbsp;<br> &nbsp;&nbsp;</p>","");
                    msg=msg.replaceAll("<header><h2 class=\"purple\">[a-zA-Z\\s]*</h2></header>","");
                    // msg=msg.replaceAll("nach oben</a>\\s*</div>[\\S\\s]*","</a></div></main></body></html>");
                    msg=msg.replaceAll("<h2 class=\"green collapsed\" data-toggle=\"collapse\" data-target=\"#eve\">Veranstaltungen</h2>","");
                    msg=msg.replaceAll("<div id=\"eve\" class=\" collapse\">","");
                    msg=msg.replaceAll("<a class=\"navbar-brand\" href=\"/zug-menzingen-walchwil/\">","<a class=\"navbar-brand\">");
                    msg=msg.replaceAll("<button type=\"button\" class=\"navbar-toggle collapsed\"[\\S\\s]*<span class=\"icon-bar\"></span>\\s*</button>","");
                    // msg=msg.replaceAll("<a class=\"more\" title=\"","<a class=\"more\" style=\"font-size:smaller;\" title=\"");

                    //msg=msg.replaceAll("<div class=\"load-more\">\\s*<div class=\"text-left\">[\\S\\s]*mehr\\sArtikel\\sladen\\s*</a>\\s*</div>\\s*</div>","<script type=\"text/javascript\">\n" +
                    //        "$(\"#DivContent\").load(\"https://www.ref-zug.ch/zug-menzingen-walchwil/gottesdienste/l/2?type=44217\");" + "\n" +
                    //        "  </script>");

                    // msg=msg.replaceAll("<a class=\"load-more-news2\" id=\"more-district\"","<a class=\"load-more-news2\" id=\"more-district\" onclick=\"alert('hello');console.log('MAGIC'+document.getElementsByTagName('html')[0].innerHTML);\"");
                    // Log.d("spill", "the guts");
                    msg=msg.replaceAll("<div id=\"navbar\" class=[\\S\\s]*</ul>\\s*</li>\\s*</ul>\\s*</div>","");
                    msg=msg.replaceAll("<div id=\"btn-eventsmobile-close\">\\s*<img[\\S\\s]*</div>","");
                    msg=msg.replaceAll("<h2>Über den Bezirk</h2>\\s*<ul>[\\S\\s]*</ul>","");
                    // int idx0=msg.indexOf("nach oben</a>");
                    // Log.d("setWebChromeClient",msg.substring(idx0,idx0+1000));
                    // int idx1=msg.indexOf("</div>",idx0);
                    // msg=msg.substring(0,idx1+6) + "</main></body></html>";
                    if(!oldurl.equals(url)) {
                        webView.loadDataWithBaseURL(url,msg, "text/html; charset=utf-8", "UTF-8", null);
                        oldurl=url;
                        return true;
                    }
                    return true;
                }

                return false;
            }
        });

        //SetWebViewClient
        webView.setWebViewClient(new myWebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                /*
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            String html =getUrlSource(url);
                            html = html.replaceAll("Gottesdienste","foo");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();

                /* This call inject JavaScript into the page which just finished loading. */
                // webView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                // webView.loadUrl("javascript:HTMLOUT.processHTML(document.documentElement.outerHTML);");
                view.loadUrl("javascript:console.log('MAGIC'+document.getElementsByTagName('html')[0].innerHTML);");

            }
        });

        webView.loadUrl(url);

        /*
        //load website button when clicked
        loadWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl(url);
            }
        });

        //load html button when clicked
        loadHtml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadData(sHtml, "text/html", "UTF-8");
            }
        });
        */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu=menu;
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        unCheckAll();
        switch (item.getItemId()) {
            case R.id.Aegeri_item:
                item.setChecked(true);
                oldurl="";
                url = "https://www.ref-zug.ch/aegeri/gottesdienste/";
                webView.loadUrl(url);
                return true;
            case R.id.Baar_item:
                item.setChecked(true);
                oldurl="";
                url = "https://www.ref-zug.ch/baar-neuheim/gottesdienste/";
                webView.loadUrl(url);
                return true;
            case R.id.Cham_item:
                item.setChecked(true);
                oldurl="";
                url = "https://www.ref-zug.ch/cham/gottesdienste/";
                webView.loadUrl(url);
                return true;
            case R.id.Huenenberg_item:
                item.setChecked(true);
                oldurl="";
                url = "https://www.ref-zug.ch/huenenberg/gottesdienste/";
                webView.loadUrl(url);
                return true;
            case R.id.Rotkreuz_item:
                item.setChecked(true);
                oldurl="";
                url = "https://www.ref-zug.ch/rotkreuz/gottesdienste/";
                webView.loadUrl(url);
                return true;
            case R.id.Steinhausen_item:
                item.setChecked(true);
                oldurl="";
                url = "https://www.ref-zug.ch/steinhausen/gottesdienste/";
                webView.loadUrl(url);
                return true;
            case R.id.Zug_item:
                item.setChecked(true);
                oldurl="";
                url = "https://www.ref-zug.ch/zug-menzingen-walchwil/gottesdienste/";
                webView.loadUrl(url);
                return true;
        }
        return false;
    }

    private void unCheckAll() {
        for (int i = 0; i < mMenu.size(); i++) {
            mMenu.getItem(i).setChecked(false);
        }
    }

    private class myWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
    /*
    @Override public void onBackPressed() {

        if(webView != null &amp;&amp; webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    */
}