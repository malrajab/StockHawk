package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WStockWatchConfigureActivity WStockWatchConfigureActivity}
 */
public class WStockWatch extends AppWidgetProvider {
    public final String LOG_TAG = WStockWatch.class.getSimpleName();

    static void updateAppWidget(final Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        //CharSequence widgetText = WStockWatchConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wstock_watch);
        //views.setTextViewText(R.id.appwidget_text, widgetText);

        Intent intent=new Intent(context, MyStocksActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent,0);
        views.setOnClickPendingIntent(R.id.widget,pendingIntent);


//        ListView listView=(ListView)views.;
//
//        Cursor cursor;
//
//        SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,0,new String[]{},new int[]{});
//
//
//        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent=new Intent(context,GraphActivity.class);
//                intent.putExtra(Constants.GRAPH_ARG_KEY,mKey);
//                startActivity(intent);
//            }
//        });
//         Instruct the widget manager to update the widget

        views.setRemoteAdapter(R.id.widget_list, new Intent(context,DetailWidgetRemoteViewsService.class));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WStockWatchConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
























