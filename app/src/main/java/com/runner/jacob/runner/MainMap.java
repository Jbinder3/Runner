package com.runner.jacob.runner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;


public class MainMap extends Activity implements OnMapReadyCallback {

    GoogleMap endMap;
    int pathLength = 20;
    int pathDistance = 25;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        button = (Button) findViewById(R.id.settingsButton);
        MapFragment mapFragment = ((MapFragment) this.getFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
    }

    protected void onResume() {
        super.onResume();
    }

    public void onMapReady(GoogleMap map)
    {
        endMap = map;
        map.setMyLocationEnabled(true);
        listenForButtons();
    }

    private void reDrawMap()
    {
        //
    }

    public void listenForButtons()
    {
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                buttonHasBeenClicked();
            }
        });
    }

    public void buttonHasBeenClicked()
    {
        CharSequence options[] = new CharSequence[] {"Search Distance", "Path Length"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Path Finding Options");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0)
                {
                    searchDistanceSetup();
                }
                else
                {
                    pathLengthSetup();
                }
            }
        });
        builder.show();
    }

    public void searchDistanceSetup()
    {
        CharSequence options[] = new CharSequence[] {"50 Miles", "25 Miles", "10 Miles", "5 Miles"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search for paths within...");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0)
                {
                    pathDistance = 50;
                }
                else if(which == 1)
                {
                    pathDistance = 25;
                }
                else if(which == 2)
                {
                    pathDistance = 10;
                }
                else
                {
                    pathDistance = 5;
                }
                reDrawMap();
            }
        });
        builder.show();
    }

    public void pathLengthSetup()
    {
        CharSequence options[] = new CharSequence[] {"20 Miles", "10 Miles", "5 Miles", "3 Miles"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search for paths at least...");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0)
                {
                    pathLength = 20;
                }
                else if(which == 1)
                {
                    pathLength = 10;
                }
                else if(which == 2)
                {
                    pathLength = 5;
                }
                else
                {
                    pathLength = 3;
                }
                reDrawMap();
            }
        });
        builder.show();
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
     */
}
