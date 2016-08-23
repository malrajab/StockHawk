package com.sam_chordas.android.stockhawk.model.db;

import android.net.Uri;
import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by M. Alrajab
 * credit sam_chordas (init draft)on 10/5/15.
 *
 */
@ContentProvider(authority = QuoteProvider.AUTHORITY, database = QuoteDatabase.class)
public class QuoteProvider {
  public static final String AUTHORITY = "com.sam_chordas.android.stockhawk.model.db.QuoteProvider";

  static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

  interface Path{
    String QUOTES = "quotes";
    String GRAPHHISTORY = "graphHistory";
  }

  private static Uri buildUri(String... paths){
    Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
    for (String path:paths){
      builder.appendPath(path);
    }
    return builder.build();
  }

  @TableEndpoint(table = QuoteDatabase.QUOTES) public static class Quotes{
    @ContentUri(
        path = Path.QUOTES,
        type = "vnd.android.cursor.dir/quote"
    )
    public static final Uri CONTENT_URI = buildUri(Path.QUOTES);

    @InexactContentUri(
        name = "QUOTE_ID",
        path = Path.QUOTES + "/*",
        type = "vnd.android.cursor.item/quote",
        whereColumn = QuoteColumns.SYMBOL,
        pathSegment = 1
    )
    public static Uri withSymbol(String symbol){
      return buildUri(Path.QUOTES, symbol);
    }
  }


  @TableEndpoint(table = QuoteDatabase.GRAPHHISTORY) public static class GraphHistory{
    @ContentUri(
            path = Path.GRAPHHISTORY,
            type = "vnd.android.cursor.dir/graph",
            defaultSort = StockHistoryColumns.TIMESTAMP + " DESC"
    )
    public static final Uri CONTENT_URI = buildUri(Path.GRAPHHISTORY);
    @InexactContentUri(
            name = "GRAPHHISTORY_ID",
            path = Path.GRAPHHISTORY + "/*",
            type = "vnd.android.cursor.item/graph",
            whereColumn = StockHistoryColumns.SYMBOL,
            pathSegment = 1
    )
    public static Uri withGraphSymbol(String symbol){
      return buildUri(Path.GRAPHHISTORY, symbol);
    }
  }
}
