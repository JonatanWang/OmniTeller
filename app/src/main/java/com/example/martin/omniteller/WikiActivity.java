package com.example.martin.omniteller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by zyw on 2017-12-28.
 * Class to abstract title from main activity, parse url and show wikipedia page
 */

public class WikiActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        intent.setAction("android.intent.action.VIEW");
        String title = intent.getStringExtra("title");

        // Refine the title part of URL, as it must begin with capitalized letter and followed by uncapitalized letters
        // in an android mobile browser, e.g. "https://.... /Toilet_paper"
        String refinedTitle = title.substring(0, 1).concat(title.substring(1).toLowerCase());

        // Delete the potential backspaces within title, as URL has no backspace
        String url = "http://en.wikipedia.org/wiki/" + refinedTitle.replaceAll(" ", "_");
        Uri CONTENT_URI_BROWSERS = Uri.parse(url);
        intent.setData(CONTENT_URI_BROWSERS);

        // Set content to system default browser
        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
        startActivity(intent);
    }
}
