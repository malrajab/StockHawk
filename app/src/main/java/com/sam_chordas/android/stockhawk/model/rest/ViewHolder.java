package com.sam_chordas.android.stockhawk.model.rest;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.controller.touch_helper.ItemTouchHelperViewHolder;

/**
 * Created by m_alrajab on 8/20/16.
 * in preparation for the delete btn
 */
public  class ViewHolder extends RecyclerView.ViewHolder
        implements ItemTouchHelperViewHolder, View.OnClickListener{
    public final TextView symbol;
    public final TextView bidPrice;
    public final TextView change;
    //public final Button removeBtn;

    public ViewHolder(View itemView){
        super(itemView);
        symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
       // symbol.setTypeface(robotoLight);
        bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
        change = (TextView) itemView.findViewById(R.id.change);
        //removeBtn = (Button) itemView.findViewById(R.id.confirm_delete_btn);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onItemSelected(){
        //removeBtn.setVisibility(View.VISIBLE);
        itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onItemClear(){
        //removeBtn.setVisibility(View.GONE);
        itemView.setBackgroundColor(Color.parseColor("#FF424242"));
    }

    @Override
    public void onClick(View v) {

    }
}