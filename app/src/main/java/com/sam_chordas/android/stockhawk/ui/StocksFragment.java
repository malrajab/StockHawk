package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.controller.service.StockIntentService;
import com.sam_chordas.android.stockhawk.controller.service.StockTaskService;
import com.sam_chordas.android.stockhawk.controller.touch_helper.SimpleItemTouchHelperCallback;
import com.sam_chordas.android.stockhawk.model.db.QuoteColumns;
import com.sam_chordas.android.stockhawk.model.db.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.model.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.utils.Constants;
import com.sam_chordas.android.stockhawk.utils.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by m_alrajab on 8/20/16.
 *
 */
public class StocksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener{
    private OkHttpClient client = new OkHttpClient();
    private View tmpView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private String keyTicker;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private Context mContext;
    private Cursor mCursor;
    private RecyclerView mRecyclerView;
    private TextView tvEmpty;
    private QuoteCursorAdapter mCursorAdapter;



    public static StocksFragment newInstance() {
        StocksFragment addStockFragment = new StocksFragment();
        Bundle bundle = new Bundle();
        addStockFragment.setArguments(bundle);
        return addStockFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=getContext();
        mServiceIntent = new Intent(mContext, StockIntentService.class);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(mContext);
        editor=sharedPreferences.edit();

    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.stocks_fragment,container,false);
        tmpView=new View(mContext);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        //staggeredGridLayoutManager.
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);

        mCursorAdapter = new QuoteCursorAdapter(mContext, null);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(mContext,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View v, int position,String key) {
//                       GraphFragment newGraphFragment = new GraphFragment();
//                        Bundle bundle=new Bundle();bundle.putString(Constants.GRAPH_ARG_KEY,key);
//                        newGraphFragment.setArguments(bundle);
//                        getActivity().getSupportFragmentManager().beginTransaction()
//                                .add(R.id.graph_of_fragment, newGraphFragment)
//                                .commit();
                        final String mKey=key;keyTicker=key;
                        tmpView.setBackgroundColor(getResources().getColor(R.color.cardview_dark_background));
                        v.setBackground(getResources().getDrawable(R.drawable.backgroundcolorstock));
                        tmpView=v;
                        new AsyncTask<Void,Void,Void>(){
                            StringBuilder urlGraphStringBuilder = new StringBuilder();
                            @Override
                            protected Void doInBackground(Void... params) {
                                if(!sharedPreferences.contains(Constants.GRAPH_INIT_REQ_PREF+mKey)) {
                                    String urlGraph;
                                    String getResponse = "";
                                    String graphTicker = mKey;
                                    urlGraphStringBuilder.append("http://chartapi.finance.yahoo.com/instrument/1.0/" +
                                            graphTicker + "/chartdata;type=quote;range=14d/json");
                                    if (urlGraphStringBuilder != null) {
                                        urlGraph = urlGraphStringBuilder.toString();
                                        try {
                                            getResponse = fetchData(urlGraph);
                                            try {
                                                mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                                                        Utils.quoteGraphJsonToContentValues(getResponse, graphTicker));
                                            } catch (RemoteException | OperationApplicationException e) {
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    editor.putString(Constants.GRAPH_INIT_REQ_PREF + graphTicker, "DONE");
                                    editor.commit();
                                }
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                Intent intent=new Intent(mContext,GraphActivity.class);
                                intent.putExtra(Constants.GRAPH_ARG_KEY,mKey);
                                startActivity(intent);
                            }
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }
                        }.execute();

//                        final Handler handler=new Handler();
//                        final Runnable r = new Runnable(){
//                            public void run() {
//                            }};handler.postDelayed(r, 1200); // handler for testing. should be replaced by async
                    }
                }));
        mRecyclerView.setAdapter(mCursorAdapter);


        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null,this);
        //tvEmpty = (TextView) getActivity().findViewById(R.id.empty_rv_warning);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        tmpView.setBackgroundColor(getResources().getColor(R.color.cardview_dark_background));// rotattiooonn
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void updateEmptyView() {
        if ( mRecyclerView.getChildCount() == 0 ) {
            if ( null != tvEmpty ) {
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.empty_stock_list;
                @StockTaskService.StockMarketStatus int stockAppStatus = Utils.getAppStatus(mContext);
                switch (stockAppStatus) {
                    case StockTaskService.STOCKMARKET_STATUS_SERVER_DOWN:
                        message = R.string.empty_stock_list_server_down;
                        break;
                    case StockTaskService.STOCKMARKET_STATUS_SERVER_INVALID:
                        message = R.string.empty_stock_list_server_error;
                        break;
                    case StockTaskService.STOCKMARKET_STATUS_INVALID:
                        message = R.string.empty_stock_list_invalid_stock_name;
                        break;
                    default:
                        if (!Utils.isNetworkAvailable(mContext) ) {
                            message = R.string.empty_stock_list_no_network;
                        }
                }
                Toast.makeText(mContext,mContext.getString(message),Toast.LENGTH_LONG).show();
                tvEmpty.setText(message);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences shared, String key) {
        if ( key.equals(getString(R.string.pref_stock_status_key)) ) {
            updateEmptyView();
        } else if(key.equals("ALERT_INV")){
            new android.app.AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.no_fav_dialog_title))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(String.format(mContext.getString(R.string.no_fav_dialog_msg),
                     sharedPreferences.getString("ALERT_INV","your request")))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                       sharedPreferences.unregisterOnSharedPreferenceChangeListener(StocksFragment.this);
                         editor.putString("ALERT_INV","").commit();
                         sharedPreferences.registerOnSharedPreferenceChangeListener(StocksFragment.this);
                     }
                }
            ).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(getContext(), QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        mCursorAdapter.swapCursor(data);
        mCursor = data;
        //updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        mCursorAdapter.swapCursor(null);
    }

    @NonNull
    private String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
