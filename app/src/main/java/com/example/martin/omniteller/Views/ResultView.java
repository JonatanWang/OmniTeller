package com.example.martin.omniteller.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.martin.omniteller.R;

import java.util.List;

/**
 * Created by Martin on 2017-12-20.
 */

public class ResultView extends LinearLayout {

    private String title = "Title";
    private String info = "Info";
    private TextView titleView;
    private TextView infoView;
    LinearLayout layout = null;



    public ResultView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.result_view, this, true);

        titleView = (TextView) findViewById(R.id.resultView_title);
        infoView = (TextView) findViewById(R.id.resultView_infotext);
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
        titleView.setText(title);
    }

    public void setInfo(String info) {
        this.info = info;
        infoView.setText(info);
    }
}
