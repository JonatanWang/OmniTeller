package com.example.martin.omniteller.Models;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;


public class WikiParser{
    public static Elements getWikiInfoFromName(String name ){
        Document doc;

        try {
            doc = Jsoup.connect("http://en.wikipedia.org/wiki/"+name).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Elements paragraphs = doc.select("div.mw-parser-output p");

        return paragraphs;
    }
}

