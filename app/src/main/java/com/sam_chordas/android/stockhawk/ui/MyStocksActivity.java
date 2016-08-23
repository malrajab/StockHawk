package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.controller.service.StockIntentService;
import com.sam_chordas.android.stockhawk.controller.service.StockTaskService;
import com.sam_chordas.android.stockhawk.model.db.QuoteColumns;
import com.sam_chordas.android.stockhawk.model.db.QuoteProvider;
import com.sam_chordas.android.stockhawk.utils.Constants;
import com.sam_chordas.android.stockhawk.utils.Utils;

import static com.sam_chordas.android.stockhawk.utils.Utils.setStethoWatch;

public class MyStocksActivity extends AppCompatActivity {
  private CharSequence mTitle;
  private Intent mServiceIntent;
  private Context mContext;
  boolean isConnected;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;
    setStethoWatch(mContext);
    isConnected = Utils.isNetworkAvailable(this);
    setContentView(R.layout.activity_my_stocks);
    // The intent service is for executing immediate pulls from the Yahoo API
    // GCMTaskService can only schedule tasks, they cannot execute immediately
    mServiceIntent = new Intent(this, StockIntentService.class);
    if (savedInstanceState == null){
      mServiceIntent.putExtra("tag", "init");
      if (isConnected) startService(mServiceIntent);else networkToast();
    }

    if(!Utils.getMarketStatus(mContext))
      ((TextView)findViewById(R.id.market_Status)).setText(mContext.getString(R.string.market_is_close));
    else
      ((TextView)findViewById(R.id.market_Status)).setText("");

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
      assert fab != null;
      fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (isConnected){
          new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
              .content(R.string.content_test)
              .inputType(InputType.TYPE_CLASS_TEXT)
              .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                  // On FAB click, receive user input. Make sure the stock doesn't already exist
                  // in the DB and proceed accordingly
                  Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                      new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ?",
                      new String[] { input.toString() }, null);
                  if (c!=null&&c.getCount() != 0) {
                    Toast toast =
                        Toast.makeText(MyStocksActivity.this, "This stock is already saved!",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                    toast.show();
                    return;
                  } else {
                    // Add the stock to DB
                    mServiceIntent.putExtra("tag", "add");
                    mServiceIntent.putExtra("symbol", input.toString());
                    startService(mServiceIntent);
                  }
                }
              })
              .show();
        } else {
          networkToast();
        }
      }
    });

    mTitle = getTitle();
    if (isConnected){
      long period = Constants.INTERVAL;
      long flex = 10L;
      String periodicTag = Constants.SETPERIODTAG;

      // create a periodic task to pull stocks once every hour after the app has been opened. This
      // is so Widget data stays up to date.
      PeriodicTask periodicTask = new PeriodicTask.Builder()
          .setService(StockTaskService.class).setPeriod(period).setFlex(flex)
          .setTag(periodicTag).setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
          .setRequiresCharging(false).build();
      GcmNetworkManager.getInstance(this).schedule(periodicTask);
    }
      StocksFragment secondFragment = new StocksFragment();
      secondFragment.setArguments(getIntent().getExtras());
      getSupportFragmentManager().beginTransaction()
              .add(R.id.stocks_Fragment, secondFragment)
              .commit();
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  public void networkToast(){
     //((TextView)findViewById(R.id.empty_rv_warning)).setText(getString(R.string.empty_stock_list_no_network));
    Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.my_stocks, menu);
      restoreActionBar();
      return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    } else if(id == R.id.action_refresh ){

      if (isConnected) {
/*        OneoffTask onOffTask = new OneoffTask.Builder()
              .setService(StockTaskService.class)
              .setTag(Constants.SETPERIODTAG).setExecutionWindow(0,2)
              .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
              .setRequiresCharging(false).build();
        GcmNetworkManager.getInstance(this).schedule(onOffTask);*/
        mServiceIntent.putExtra("tag", "init");
        startService(mServiceIntent);
        Toast.makeText(mContext, getString(R.string.stock_refresh), Toast.LENGTH_LONG).show();
      }else networkToast();
      return true;
    }
    if (id == R.id.action_change_units || id==R.id.action_change_units_altr){
      // this is for changing stock changes from percent value to dollar value
      Utils.showPercent = !Utils.showPercent;
      this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
    }
    return super.onOptionsItemSelected(item);
  }
}
