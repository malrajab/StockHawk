package com.sam_chordas.android.stockhawk.ui.graph;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.View;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.model.db.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.db.StockHistoryColumns;
import com.sam_chordas.android.stockhawk.utils.Constants;

/**
 * drawing the chart
 * I am using this library just to meet spec. It might have been much easier starting my own custom
 * drawing canvas instead. This graph doesn't take real-time change via invalidate method. I
 * might be missing something
 *
 * + chart resizing (w,h) is a big problem in this lib.
 */

public class StockMarketGraph {
    SharedPreferences sharedPreferences;
    private final LineChartView mChart;
    private final Context mContext;
    CordSupClass helperCord;
    private  String[] mLabels1;
    private  float[] mValues1;
    private  int min;
    private  int max;
    private  int mSamples=150;


    private Tooltip mTip;

    public StockMarketGraph(CardView card, Context context, String ticker){
        mContext = context;
        mChart = (LineChartView) card.findViewById(R.id.linechart);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(mContext);
        String str=sharedPreferences.getString(Constants.GRAPH_DRAW_PREF,mContext.getString(R.string.grapg_1d));
        switch (str){ case "1D": mSamples=150; break;case "1W": mSamples=700; break;case "1M": mSamples=1000;
        break;default: mSamples=150;}
        helperCord=new CordSupClass(mContext,0,ticker,mSamples);
        init();
    }

    private void init(){
    mValues1=helperCord.getPlotVal();
    mLabels1=helperCord.getLblVal();
    min=helperCord.getMinMaxAxisBorderValues()[0];
    max=helperCord.getMinMaxAxisBorderValues()[1];
}

    public void show() {
        mTip = new Tooltip(mContext, R.layout.linechart_three_tooltip, R.id.value);
        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(65), (int) Tools.fromDpToPx(25));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(100);
            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(100);
            mTip.setPivotX(Tools.fromDpToPx(65) / 2);
            mTip.setPivotY(Tools.fromDpToPx(25));
        }

        mChart.setTooltips(mTip);

        // Data
        LineSet dataset = new LineSet(mLabels1, mValues1);
        dataset.setColor(Color.parseColor("#b3b5bb"))
                .setFill(Color.parseColor("#952d374c"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setDotsRadius(0f)
                .setThickness(3)
                .setSmooth(true)
                .beginAt(0);
        mChart.addData(dataset);


        // Chart
        mChart.setBorderSpacing(Tools.fromDpToPx(0))
                .setAxisBorderValues(min, max)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(Color.parseColor("#000000"))
                .setGrid(ChartView.GridType.FULL,20,20,new Paint())
                .setXAxis(true)
                .setYAxis(true);

        Animation anim = new Animation()
                .setEasing(new BounceEase());
        mChart.show(anim);

    }


    // support class for getting the data for graph methods names explain purpose
    private class CordSupClass {

        private int max=Integer.MAX_VALUE;
        private Context mContext;
        private String mTicker;
        private long mTimeStamp;
        private Cursor mCursor;

        public CordSupClass(Context c, long timeStamp, String ticker, int max){
            this.mTicker=ticker;
            this.max=max;
            this.mContext=c;
            this.mTimeStamp=timeStamp;
            mCursor=mContext.getContentResolver().query(QuoteProvider.GraphHistory.CONTENT_URI,
                    new String[]{StockHistoryColumns.CLOSEPRICE},
                    StockHistoryColumns.SYMBOL + " = ? and " + StockHistoryColumns.TIMESTAMP + " >= ? ",
                    new String[]{mTicker, String.valueOf(mTimeStamp)},
                    null);
        }

        public float[] getPlotVal(){
            int i=0;
            float[] tVal=new float[getCount()];
            if(mCursor.moveToFirst()){
                do{
                    tVal[i]=mCursor.getFloat(mCursor.getColumnIndex(StockHistoryColumns.CLOSEPRICE));
                    i++;
                }while (mCursor.moveToNext() && i<max);
            }
            return tVal;
        }

        public String[] getLblVal(){
            String[] xx=new String[getCount()];
            // to be produce nice label
            for(int i=0;i<getCount();i++)
                xx[i]="";
            return xx;
        }

        public int getCount(){
            return Math.min(mCursor.getCount(),max);
        }

        public int[]  getMinMaxAxisBorderValues(){
            try {
                float[] x = getPlotVal();
                float min = x[0], max = x[0];
                for (int i = 0; i < getCount(); i++) {
                    if (min > x[i]) min = x[i];
                    if (max < x[i]) max = x[i];
                }
                return new int[]{(int) Math.floor(min - 1 - (max - min) * 0.9), (int) Math.ceil(max + 1 + (max - min) * 1.1)};
            }catch (Exception e){
                return new int[]{0,0};
            }
        }
    }
}
