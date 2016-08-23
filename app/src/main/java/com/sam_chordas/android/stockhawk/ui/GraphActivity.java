package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by m_alrajab on 8/20/16.
 * Hosting the graph fragment
 */
public class GraphActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        GraphFragment secondFragment = new GraphFragment();
        secondFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.graph_of_fragment, secondFragment)
                .commit();


    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        //getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }


}
