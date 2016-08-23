package com.sam_chordas.android.stockhawk.model.db;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by m_alrajab on 8/21/16.
 * for keeping the history of the stocks
 */
public class StockHistoryColumns {
    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String SYMBOL = "symbol";
    @DataType(DataType.Type.REAL) @NotNull
    public static final String CLOSEPRICE = "close_price";
    @DataType(DataType.Type.REAL) @NotNull
    public static final String TIMESTAMP = "Timestamp";

}