package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.model.db.QuoteColumns;
import com.sam_chordas.android.stockhawk.model.db.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.graph.StockMarketGraph;
import com.sam_chordas.android.stockhawk.utils.Constants;

import java.util.ArrayList;

/**
 * Created by m_alrajab on 8/20/16.
 * Hosting the graph
 */
public class GraphFragment extends Fragment {
    private LinearLayout mTimeBarLayout;
    private ArrayList<TextView> mItemsOfTimeBar=new ArrayList<>();
    private Context mContext;
    private String mGraphTitle;
    private StockMarketGraph stockMarketGraph;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    /**
     * Create fragment and pass bundle with data as its' arguments
     */
    public static GraphFragment newInstance(Bundle args) {
        GraphFragment fragment = new GraphFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=getContext();
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(mContext);
        editor=sharedPreferences.edit();
//        mGraphTitle=bundle.getString(Constants.GRAPH_ARG_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView= inflater.inflate(R.layout.graph_fragment,container,false);

        mGraphTitle=getActivity().getIntent().getStringExtra(Constants.GRAPH_ARG_KEY);// bad practice
        stockMarketGraph=new StockMarketGraph((CardView) rootView.findViewById(R.id.cardline) ,getContext(),mGraphTitle);
        stockMarketGraph.show();
        mTimeBarLayout=(LinearLayout)rootView.findViewById(R.id.timebar_container);

        // when the history db is design properly this can be be changed to all layout children
        for(int i=1;i<mTimeBarLayout.getChildCount()/2;i++){
            try {
                TextView tv=((TextView) mTimeBarLayout.getChildAt(i));
                if(sharedPreferences.contains(Constants.GRAPH_DRAW_PREF)&&tv.getText().toString()
                        .equals(sharedPreferences.getString(Constants.GRAPH_DRAW_PREF,getString(R.string.grapg_1d))))
                    tv.setBackground(getResources().getDrawable(R.drawable.backgroundcolor));
                tv.setOnClickListener(new SelectedTimeBarOptionListener());
                mItemsOfTimeBar.add(tv);
            }catch (Exception e){
                Log.e("Error",e.getMessage(),e);
            }i++;
        }
        return  rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String companyName="";

        Cursor cursor=getActivity().getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.COMPANYLISTINGNAME},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{mGraphTitle},
                null);
        if(cursor!=null&&cursor.moveToFirst())
            companyName=cursor.getString(0);
        ((TextView)getActivity().findViewById(R.id.chart_title)).setText(mGraphTitle+ " : " +companyName);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private class SelectedTimeBarOptionListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            for(TextView tv : mItemsOfTimeBar){
                editor.putString(Constants.GRAPH_DRAW_PREF,((TextView)v).getText().toString()).commit();
                tv.setBackground(getResources().getDrawable(R.color.material_bluish_a700));}
            v.setBackground(getResources().getDrawable(R.drawable.backgroundcolor));

            GraphFragment newGraphFragment = new GraphFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.graph_of_fragment, newGraphFragment)
                    .commit();
        }
    }

}
