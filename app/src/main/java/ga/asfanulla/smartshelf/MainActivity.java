package ga.asfanulla.smartshelf;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recycle_view)
    RecyclerView recyclerView;

    @BindView(R.id.calculate)
    Button calcu;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    ItemAdapter adapter;

    ArrayList<HashMap<String , String>> itemlist;
    ProgressDialog loading;

    AlertDialog alert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        createNotificationChannel();

        itemlist = new ArrayList<>();
        adapter = new ItemAdapter(this, itemlist);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 0));
        recyclerView.setAdapter(adapter);

        getData2();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData2();
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    public void getData2(){
        loading = ProgressDialog.show(this,"Please wait...","Processing...",false,false);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://ec2-13-127-51-219.ap-south-1.compute.amazonaws.com/test.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        swipeContainer.setRefreshing(false);
                        itemlist.clear();
                        try {
                            JSONArray jsonarray = new JSONArray(response.trim());
                            JSONObject jsonobject = null;
                            final double x[] = new double[jsonarray.length()];
                            final double y[] = new double[jsonarray.length()];

                            for (int i = 0; i < jsonarray.length(); i++) {
                                jsonobject = jsonarray.getJSONObject(i);
                                String id = jsonobject.getString("id");
                                String weight = jsonobject.getString("wght");
                                String time = jsonobject.getString("time");
                                String date = jsonobject.getString("dte");
                                String stamp = jsonobject.getString("timestmp");

                                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                Date datee = null;
                                try {
                                    datee = dateFormat.parse(time);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                double seconds = 0;
                                seconds = datee.getTime();
                                x[i] = seconds;
                                y[i] = Double.parseDouble(weight);

                                HashMap<String, String> temp = new HashMap<String, String>();
                                temp.put("id", id);
                                temp.put("weight", weight);
                                temp.put("time", time);
                                temp.put("date", date);
                                temp.put("stamp", stamp);
                                itemlist.add(temp);

                            }

                            adapter.notifyDataSetChanged();

                            final String wth = jsonarray.getJSONObject(0).getString("wght");
                            if(wth.equals("0")){
                                calcu.setText("Order");
                            } else {
                                calcu.setText("Calculate");
                            }

                            if(Integer.parseInt(wth) <= 10){

                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

                                NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(MainActivity.this, "1")
                                        .setColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary))
                                        .setSmallIcon(getApplicationInfo().icon)
                                        .setContentTitle("Item Status")
                                        .setContentText("Quantity low refill required")
                                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                        .setVisibility(1)
                                        .setSound(notificationSoundURI)
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                                notificationManager.notify(2, mNotificationBuilder.build());
                            }

                            calcu.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(wth.equals("0")) {
                                        Uri uri = Uri.parse("http://amazon.in/Pantry");
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent);
                                    } else {
                                        calculate(x, y, wth);
                                    }
                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
                swipeContainer.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("get_data","true");
                return params;
            }
        };
        queue.add(stringRequest);

    }

    private void calculate(double[] x, double[] y, String fst){
        LinearRegression linearRegression = new LinearRegression(x, y);
        String field = String.valueOf(linearRegression);
        String lf = "";
        double xi = Double.parseDouble(fst);
        double li = Double.parseDouble(field);
        Log.d("si/", String.valueOf(xi));

        if (field.toLowerCase().startsWith("+") || field.toLowerCase().startsWith("-")) {
            lf = field.substring(1);
        } else if(Double.compare(li,xi) == 0){
            lf = "To infinity and beyond";
        } else {
            lf = field;
        }

//        long days = TimeUnit.MILLISECONDS.toDays(Long.parseLong(lf));
  //      Log.d("days/", String.valueOf(days));

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("The Future of Item-X").setMessage(lf)
                .setCancelable(false)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       alert.dismiss();
                    }
                });
        alert = builder.create();
        alert.show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ch1";
            String description = "order";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }


}
