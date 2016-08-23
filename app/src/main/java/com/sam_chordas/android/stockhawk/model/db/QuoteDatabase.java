package com.sam_chordas.android.stockhawk.model.db;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by M. Alrajab
 * credit sam_chordas (init draft)on 10/5/15.
 */
@Database(version = QuoteDatabase.VERSION)
public class QuoteDatabase {
  private QuoteDatabase(){}

  public static final int VERSION = 7;

  @Table(QuoteColumns.class) public static final String QUOTES = "quotes";// MA this is the name of the quotes table
  @Table(StockHistoryColumns.class) public static final String GRAPHHISTORY = "graphHistory";// MA this is the record of stock price history, based on close-price
}
