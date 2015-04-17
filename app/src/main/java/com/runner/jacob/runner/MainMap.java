package com.runner.jacob.runner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;


public class MainMap extends Activity implements OnMapReadyCallback {

    GoogleMap endMap;
    Circle circle;
    int minPathLength = 0;
    int maxPathLength = 20;
    int pathDistance = 25;
    Button button;
    LatLng place;
    String responseText = "";
    ArrayList<PolylineOptions> paths = new ArrayList<PolylineOptions>();
    ArrayList<Polyline> lines = new ArrayList<Polyline>();
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
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
           public void onMyLocationChange(Location arg0) {
               place = new LatLng(arg0.getLatitude(),arg0.getLongitude());
           }
        });
        listenForButtons();
    }

    public int MTM(int miles)
    {
        return ((int)(((double)miles)*1609.34));
    }

    private void reDrawMap()
    {
        //System.out.println(responseText);
        clearPaths();
        drawSearchRadius();
        processHTTPString();
    }

    public void drawSearchRadius()
    {
        circle = endMap.addCircle(new CircleOptions()
                .center(place)
                .radius(MTM(pathDistance))
                .fillColor(0x150000FF)
                .strokeColor(0x30FF0000)
        );
        System.out.println(MTM(pathDistance));
    }

    public void processHTTPString()
    {
        //split string into parts 1st part is garbage
        String[] temp = responseText.split("coordinates");
        ArrayList<String>[] tempS;
        if(temp.length == 1)
        {
            System.out.println("NO PATHS FOUND");
        }
        else {
            //split string into coordinates
            tempS = (ArrayList<String>[]) new ArrayList[temp.length - 1];
            for (int j = 1; j < temp.length; j++) {
                tempS[j - 1] = new ArrayList<String>(Arrays.asList(temp[j].split("(:\\[\\[)|(\\]\\]\\}\"\\})|(\\],\\[)|(,)")));
            }
            //remove the extra parts that regex didn't catch
            int tempN = tempS[temp.length - 2].size();
            tempS[temp.length - 2].remove(tempN - 1);
            tempS[temp.length - 2].remove(tempN - 2);
            //String[] check = temp[1].split("(:\\[\\[)|(\\]\\]\\}\"\\})|(\\],\\[)|(,)");
            //String[] check = temp[1].split("[-+]?[0-9]+.[-+]?[0-9]+");
            //int i = 0;
            //while (i < tempS[temp.length - 3].size()) {
            //    System.out.println(tempS[temp.length - 3].get(i));
            //    i++;
            //}

            //We now need to turn the arrays of paths into doubles and store them in the lists
            Random rand = new Random();
            for(int x = 0; x < tempS.length; x++)
            {
                int r = rand.nextInt(255);
                int g = rand.nextInt(255);
                int b = rand.nextInt(255);
                paths.add(new PolylineOptions());
                paths.get(x).color(Color.rgb(r,g,b));
                for(int y = 1; y < tempS[x].size()-6; y = y+2)
                {
                    paths.get(x).add(new LatLng(Double.parseDouble(tempS[x].get(y + 1)),Double.parseDouble(tempS[x].get(y))));
                }
            }

            //Now we need to turn the PolylineOptions into Polyline lines
            for(int z = 0; z < paths.size(); z ++)
            {
                lines.add(endMap.addPolyline(paths.get(z)));
                lines.get(z).setVisible(true);
            }
        }
    }

    public void clearPaths()
    {
        if(paths.size() > 0);
        {
            for(int i = 0; i < lines.size(); i++)
            {
                lines.get(i).setVisible(false);
            }
            paths.clear();
            lines.clear();
        }
        if(circle != null)
        {
            circle.setVisible(false);
        }
    }

    private class getURLInfo extends AsyncTask<Void, Void, Void>
    {
        //URL url;
        //HttpURLConnection conn;
        protected Void doInBackground(Void... unused)
        {
            try {
                //url = new URL("https://runsharer4440.cartodb.com/api/v2/sql?q=SELECT id,st_len,st_dist,st_path FROM (SELECT id,ST_Length(path) AS st_len,ST_Distance(ST_GeographyFromText('SRID=4326;POINT("+place.longitude+" "+place.latitude+")'), path) AS st_dist,ST_AsGeoJSON(path) AS st_path FROM routes) AS st_results WHERE st_len>="+MTM(minPathLength)+" AND st_len<="+MTM(maxPathLength)+" AND st_dist<="+MTM(pathDistance)+";&api_key=08c41a029438251adba9753b362edb15abd61acf");
                //System.out.println(url.toString());
                HttpContext httpContext = new BasicHttpContext();
                HttpClient httpClient = new DefaultHttpClient();
                String tempURL = "https://runsharer4440.cartodb.com/api/v2/sql?q=SELECT%20id,st_len,st_dist,st_path%20FROM%20(SELECT%20id,ST_Length(path)%20AS%20st_len,ST_Distance(ST_GeographyFromText('SRID=4326;POINT("+place.longitude+"%20"+place.latitude+")'),%20path)%20AS%20st_dist,ST_AsGeoJSON(path)%20AS%20st_path%20FROM%20routes)%20AS%20st_results%20WHERE%20st_len%3E="+MTM(minPathLength)+"%20AND%20st_len%3C="+MTM(maxPathLength)+"%20AND%20st_dist%3C="+MTM(pathDistance)+";&api_key=08c41a029438251adba9753b362edb15abd61acf";
                System.out.println(tempURL);
                HttpPost httpPost = new HttpPost(tempURL);
                HttpResponse response = httpClient.execute(httpPost, httpContext);
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                responseText = EntityUtils.toString(entity);
                //conn=(HttpURLConnection)url.openConnection();
                //System.out.println("openConnection is fine");
                //InputStream in = new BufferedInputStream(conn.getInputStream());
                System.out.println("I have a response");
                //response = CharStreams.toString(new InputStreamReader(in, "UTF-8"));
                //Scanner inStream = new Scanner(conn.getInputStream());
                //while(inStream.hasNextLine()){
                //   response+=(inStream.nextLine());
                //}
                //conn.setDoOutput(true);
                //conn.setRequestMethod("POST");
                //conn.setChunkedStreamingMode(0);
                //OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //writeStream(out);
            } catch(MalformedURLException e) {
                System.out.println("WRONG URL!");
            } catch(IOException e) {
                System.out.println("conn setup open connection issue.");
            } finally {
                //conn.disconnect();
                System.out.println("disconnected");
            }
            return null;
        }

        protected void onPostExecute(Void result)
        {
            reDrawMap();
        }
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
        CharSequence options[] = new CharSequence[] {"Search Distance", "Minimum Path Length", "Maximum Path Length"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Path Finding Options");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0)
                {
                    searchDistanceSetup();
                }
                else if(which == 1)
                {
                    minPathLengthSetup();
                }
                else
                {
                    maxPathLengthSetup();
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
                new getURLInfo().execute();
            }
        });
        builder.show();
    }

    public void minPathLengthSetup()
    {
        CharSequence options[] = new CharSequence[] {"20 Miles", "10 Miles", "5 Miles", "3 Miles", "0 Miles"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search for paths at least...");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0)
                {
                    minPathLength = 20;
                }
                else if(which == 1)
                {
                    minPathLength = 10;
                }
                else if(which == 2)
                {
                    minPathLength = 5;
                }
                else if(which == 3)
                {
                    minPathLength = 3;
                }
                else
                {
                    minPathLength = 0;
                }
                new getURLInfo().execute();
            }
        });
        builder.show();
    }

    public void maxPathLengthSetup()
    {
        CharSequence options[] = new CharSequence[] {"20 Miles", "10 Miles", "5 Miles", "3 Miles"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search for paths at most...");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0)
                {
                    maxPathLength = 20;
                }
                else if(which == 1)
                {
                    maxPathLength = 10;
                }
                else if(which == 2)
                {
                    maxPathLength = 5;
                }
                else
                {
                    maxPathLength = 3;
                }
                new getURLInfo().execute();
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
