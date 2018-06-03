package com.appspot.usbhidterminal;



import android.os.Bundle;
import android.app.Activity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;



public class TestResults_Activity extends Activity {

    GraphView graph = (GraphView) findViewById(R.id.graph1);
    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
            new DataPoint(0, 1),
            new DataPoint(1, 5),
            new DataPoint(2, 3),
            new DataPoint(3, 2),
            new DataPoint(4, 6)
    });




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results_);
        graph.addSeries(series);
    }

}
