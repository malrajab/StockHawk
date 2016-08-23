package com.sam_chordas.android.stockhawk.utils;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.sam_chordas.android.stockhawk.BuildConfig;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.controller.service.StockTaskService;
import com.sam_chordas.android.stockhawk.model.db.QuoteColumns;
import com.sam_chordas.android.stockhawk.model.db.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.db.StockHistoryColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Edited by M. Alrajab
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();
  public static boolean showPercent = true;
  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        String dataCreatedTime=jsonObject.getString("created");
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          batchOperations.add(buildBatchOperation(jsonObject,dataCreatedTime));
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject,dataCreatedTime));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject, String dataCreated){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.CREATED, dataCreated);
      builder.withValue(QuoteColumns.LASTETRADETIME, jsonObject.getString("LastTradeTime"));
      builder.withValue(QuoteColumns.LASTTRADEDATE, jsonObject.getString("LastTradeDate"));
      builder.withValue(QuoteColumns.COMPANYLISTINGNAME, jsonObject.getString("Name"));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

    public static ArrayList quoteGraphJsonToContentValues(String JSON, String ticker){
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        String json2=JSON.split(Pattern.quote("("))[1];
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try{
            jsonObject = new JSONObject(json2);
            if (jsonObject != null && jsonObject.length() != 0){
                resultsArray = jsonObject.getJSONArray("series");
                    if (resultsArray != null && resultsArray.length() != 0)
                        for (int i = 0; i < resultsArray.length(); i++){
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildGraphBatchOperation(jsonObject,ticker));
                        }
                    }
        } catch (JSONException e){Log.e(LOG_TAG, "String to JSON failed: " + e);}
        return batchOperations;
    }

    public static ContentProviderOperation buildGraphBatchOperation(JSONObject jsonObject, String ticker){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.GraphHistory.CONTENT_URI);
        try {
            builder.withValue(StockHistoryColumns.SYMBOL, ticker);
            builder.withValue(StockHistoryColumns.CLOSEPRICE, Float.valueOf(jsonObject.getString("close")));
            builder.withValue(StockHistoryColumns.TIMESTAMP, Long.valueOf(jsonObject.getString("Timestamp")));
        } catch (JSONException e){
            e.printStackTrace();
        }
        return builder.build();
    }

  public static String getRequestedDate(int backYears, int backMonths, int backDays){
    backDays=backDays>0?0:backDays;
    backMonths=backMonths>0?0:backMonths;
    backYears=backYears>0?0:backYears;
    Calendar calendar =Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH,backDays);
    calendar.add(Calendar.MONTH,backMonths);
    calendar.add(Calendar.YEAR,backYears);
    Date date=calendar.getTime();
    SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy");
    return dateFormat.format(date);
  }

  public static String getRequestedTime(int backHours, int backMins, int backSec){
    backSec=backSec>0?0:backSec;
    backMins=backMins>0?0:backMins;
    backHours=backHours>0?0:backHours;
    Calendar calendar =Calendar.getInstance();
    calendar.add(Calendar.HOUR,backHours);
    calendar.add(Calendar.MINUTE,backMins);
    calendar.add(Calendar.SECOND,backSec);
    Date date=calendar.getTime();
    SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a");
    String str=timeFormat.format(date).toLowerCase();
    String[] tStr=str.split(" ");str=tStr[0]+tStr[1];
    return str;
  }
  @SuppressWarnings("ResourceType")
  public static @StockTaskService.StockMarketStatus int getAppStatus(Context c){
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
    return sp.getInt(c.getString(R.string.pref_stock_status_key), StockTaskService.STOCKMARKET_STATUS_UNKNOWN);
  }

  static public boolean isNetworkAvailable(Context c) {
    ConnectivityManager cm =
            (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    return activeNetwork != null &&
            activeNetwork.isConnectedOrConnecting();
  }
  public static void setStethoWatch(Context context) {
    if(BuildConfig.DEBUG) {
      Stetho.initialize(
              Stetho.newInitializerBuilder(context)
                      .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                      .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context))
                      .build()
      );
    }
  }

  public static boolean getMarketStatus(Context c){

    Cursor cursor=c.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
            new String[]{ QuoteColumns.LASTETRADETIME},
            QuoteColumns.LASTETRADETIME + " = ? and  "+QuoteColumns.LASTTRADEDATE + " >= ?" ,
            new String[]{getRequestedDate(0,0,0),getRequestedTime(0,-30,0)},
            null);

    return cursor != null && cursor.moveToFirst();
  }

}
