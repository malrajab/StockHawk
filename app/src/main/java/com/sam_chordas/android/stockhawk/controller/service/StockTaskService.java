package com.sam_chordas.android.stockhawk.controller.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.model.db.QuoteColumns;
import com.sam_chordas.android.stockhawk.model.db.QuoteProvider;
import com.sam_chordas.android.stockhawk.utils.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLEncoder;
import java.util.Calendar;

/**
 * Created by sam_chordas on 9/30/15.
 * Edited by Moaath Alrajab
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService{
  private SharedPreferences sharedPreferences;
  private SharedPreferences.Editor editor;
  private String LOG_TAG = StockTaskService.class.getSimpleName();

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({STOCKMARKET_STATUS_OK, STOCKMARKET_STATUS_SERVER_DOWN, STOCKMARKET_STATUS_SERVER_INVALID,  STOCKMARKET_STATUS_UNKNOWN, STOCKMARKET_STATUS_INVALID})
  public @interface StockMarketStatus {}
  public static final int STOCKMARKET_STATUS_OK = 0;
  public static final int STOCKMARKET_STATUS_SERVER_DOWN = 1;
  public static final int STOCKMARKET_STATUS_SERVER_INVALID = 2;
  public static final int STOCKMARKET_STATUS_UNKNOWN = 3;
  public static final int STOCKMARKET_STATUS_INVALID = 4;
  private OkHttpClient client = new OkHttpClient();
  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();
  private boolean isUpdate;


  public StockTaskService(){}

  public StockTaskService(Context context){
    mContext = context;
    sharedPreferences= PreferenceManager.getDefaultSharedPreferences(mContext);
    editor=sharedPreferences.edit();
  }
  String fetchData(String url) throws IOException{
    Request request = new Request.Builder()
            .url(url)
            .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }


  @Override
  public int onRunTask(TaskParams params){

    Log.v(LOG_TAG,Utils.getRequestedDate(-5,0,49));
    Log.v("mmmm -->>>>", ""+Calendar.getInstance().getTimeInMillis()/1000);
    Cursor initQueryCursor;
    if (mContext == null){
      mContext = this;
    }
    StringBuilder urlStringBuilder = new StringBuilder();
    try{
      // Base URL for the Yahoo query
      urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
      urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
              + "in (", "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (params.getTag().equals("init") || params.getTag().equals("periodic")){
      isUpdate = true;
      initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
              new String[] { "Distinct " + QuoteColumns.SYMBOL }, null,
              null, null);
      if (initQueryCursor.getCount() == 0 || initQueryCursor == null){
        // Init task. Populates DB with quotes for the symbols seen below
        try {
          urlStringBuilder.append(
                  URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      } else if (initQueryCursor != null){
        DatabaseUtils.dumpCursor(initQueryCursor);
        initQueryCursor.moveToFirst();
        for (int i = 0; i < initQueryCursor.getCount(); i++){
          mStoredSymbols.append("\""+
                  initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol"))+"\",");
          initQueryCursor.moveToNext();
        }
        mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
        try {
          urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
    } else if (params.getTag().equals("add")){
      isUpdate = false;
      // get symbol from params.getExtra and build query
      String stockInput = params.getExtras().getString("symbol");
      try {
        urlStringBuilder.append(URLEncoder.encode("\""+stockInput+"\")", "UTF-8"));
      } catch (UnsupportedEncodingException e){
        e.printStackTrace();
      }
    }

    // finalize the URL for the API query.
    urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
            + "org%2Falltableswithkeys&callback=");

    String urlString;
    String getResponse;
    int result = GcmNetworkManager.RESULT_FAILURE;

    if (urlStringBuilder != null){
      urlString = urlStringBuilder.toString();
      try{
        getResponse = fetchData(urlString);
        if(getResponse.split("null").length>60 && params.getTag().equals("add")){
          editor.putString("ALERT_INV",params.getExtras().getString("symbol")).commit();
          return GcmNetworkManager.RESULT_FAILURE;}
        result = GcmNetworkManager.RESULT_SUCCESS;
        try {
          ContentValues contentValues = new ContentValues();
          // update ISCURRENT to 0 (false) so new data is current
          if (isUpdate){
            contentValues.put(QuoteColumns.ISCURRENT, 0);
            mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                    null, null);
          }
          mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                  Utils.quoteJsonToContentVals(getResponse));
        }catch (RemoteException | OperationApplicationException e){
          Log.e(LOG_TAG, "Error applying batch insert", e);
        }
      } catch (Exception e){
        e.printStackTrace();
      }
    }

    return result;
  }


  static private void setStockMarketStatus(Context c, @StockMarketStatus int stockStatus){
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
    SharedPreferences.Editor spe = sp.edit();
    spe.putInt(c.getString(R.string.pref_stock_status_key), stockStatus);
    spe.commit();
  }
}
