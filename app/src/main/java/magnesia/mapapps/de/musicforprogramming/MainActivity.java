package magnesia.mapapps.de.musicforprogramming;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import magnesia.mapapps.de.musicforprogramming.List.RecyclerTouchListener;
import magnesia.mapapps.de.musicforprogramming.List.Song;
import magnesia.mapapps.de.musicforprogramming.List.SongsAdapter;

public class MainActivity extends AppCompatActivity {

    private List<Song> songList = new ArrayList<>();
    private SongsAdapter mAdapter;
    private MyBroadRequestReceiver receiver;

    // Default song and URL
    private String selectedSong = "Datassette";
    private String selectedURL = "http://datashat.net/music_for_programming_1-datassette.mp3";

    // MusicPlayer service
    private PlayerService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    // Player handling objects
    private boolean playPause;
    private boolean initial = true;

    // UI elements
    private FloatingActionButton fab_play, fab_ff, fab_rew;
    private CollapsingToolbarLayout collapsingToolbar;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbars
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitleEnabled(true);
        collapsingToolbar.setTitle(selectedSong);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // stuff for List (RecyclerView)
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new SongsAdapter(songList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        // intent filter & Progress Dialog
        IntentFilter filterStart = new IntentFilter("Start ProgressDialog");
        IntentFilter filterStop = new IntentFilter("Stop ProgressDialog");
        receiver = new MyBroadRequestReceiver();
        registerReceiver(receiver, filterStart);
        registerReceiver(receiver, filterStop);
        progressDialog = new ProgressDialog(this);

        // row click listener
        recyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        musicSrv.stopSong();
                        if (playPause) {
                            fab_ff.setVisibility(View.INVISIBLE);
                            fab_rew.setVisibility(View.INVISIBLE);
                            showSnakeBar(view, R.string.stopped_playback);
                        }
                        playPause = false;
                        initial = true;
                        musicSrv.setSong(songList.get(position).getUrl());
                        fab_play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                        collapsingToolbar.setTitle(songList.get(position).getTitle());
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        showSnakeBar(view, R.string.coming_soon);
                    }
                })
        );

        prepareSongData();

        // play button listener
        fab_play = (FloatingActionButton) findViewById(R.id.fab_play);
        fab_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!playPause) {
                    if (initial) {
                        musicSrv.playSong();
                    } else {
                        musicSrv.playResume();
                        showSnakeBar(view, R.string.resume);
                    }
                    fab_ff.setVisibility(View.VISIBLE);
                    fab_rew.setVisibility(View.VISIBLE);
                    fab_play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                    playPause = true;
                    initial = false;
                } else {
                    if (initial) {
                        musicSrv.stopSong();
                        showSnakeBar(view, R.string.stopped_playback);

                    } else {
                        musicSrv.pauseSong();
                        // showSnakeBar(view, R.string.pause);
                    }
                    fab_ff.setVisibility(View.INVISIBLE);
                    fab_rew.setVisibility(View.INVISIBLE);
                    fab_play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                    playPause = false;
                }
            }
        });

        // fast forward 30 seconds
        fab_ff = (FloatingActionButton) findViewById(R.id.fab_ff);
        fab_ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicSrv.fastforwardfSong();
                showSnakeBar(view, R.string.fast_forward);
            }
        });

        // rewind 30 seconds
        fab_rew = (FloatingActionButton) findViewById(R.id.fab_rew);
        fab_rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicSrv.rewindSong();
                showSnakeBar(view, R.string.rewind);
            }
        });

        // TODO: 27.02.2018  check for internet in general
        checkWiFi();

        // TODO: Localisation
        Locale locale = new Locale("de");
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.MusicBinder binder = (PlayerService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass song
            musicSrv.setSong(selectedURL);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void checkWiFi() {

        // checks the Wifi state
        ConnectivityManager connectiManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectiManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // if there is no wifi, fires an alert
        if (!mWifi.isConnected()) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            LinearLayout layout = new LinearLayout(this);
            layout.setPadding(50, 40, 50, 10);
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(R.string.alert_title);
            tvTitle.setPadding(50, 40, 50, 10);
            tvTitle.setTypeface(Typeface.create("'Conso', Courier New, monospace",
                    Typeface.NORMAL));
            tvTitle.setGravity(Gravity.CENTER_VERTICAL);
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f);
            tvTitle.setTextColor(Color.parseColor("#ec007c"));

            TextView tvMessage = new TextView(this);
            tvMessage.setText(R.string.alert_message);
            tvMessage.setTypeface(Typeface.create("'Conso', Courier New, monospace",
                    Typeface.NORMAL));
            tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            tvMessage.setTextColor(Color.parseColor("#ec007c"));

            layout.addView(tvMessage);
            builder.setView(layout);
            builder.setCustomTitle(tvTitle);

            builder.setNeutralButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    builder.setCancelable(true);
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00303e")));
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#ec007c"));
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText(R.string.alert_ok);
                }
            });

            dialog.show();
        }
    }

    private void showSnakeBar (View view, Integer messsage) {
        Snackbar snackbar = Snackbar.make(view, getText(messsage), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    // first 10 songs from musicForProgramming.net
    private void prepareSongData() {

        Song song = new Song("Datassette", "1:02:16",
                "http://datashat.net/music_for_programming_1-datassette.mp3", "01: ");
        songList.add(song);

        song = new Song("Sunjammer", "0:51:15",
                "http://datashat.net/music_for_programming_2-sunjammer.mp3", "02: ");
        songList.add(song);

        song = new Song("Datassette", "1:17:07",
                "http://datashat.net/music_for_programming_3-datassette.mp3", "03: ");
        songList.add(song);

        song = new Song("Com Truise", "0:50:46",
                "http://datashat.net/music_for_programming_4-com_truise.mp3", "04: ");
        songList.add(song);

        song = new Song("Abe Mangger", "1:08:51",
                "http://datashat.net/music_for_programming_5-abe_mangger.mp3", "05: ");
        songList.add(song);

        song = new Song("Gods Of The New Age", "0:55:58",
                "http://datashat.net/music_for_programming_6-gods_of_the_new_age.mp3", "06: ");
        songList.add(song);

        song = new Song("Tahlhoff Garten + Untitled", "1:06:58",
                "http://datashat.net/music_for_programming_7-tahlhoff_garten_and_untitled.mp3", "07: ");
        songList.add(song);

        song = new Song("Connectedness Locus", "0:55:24",
                "http://datashat.net/music_for_programming_8-connectedness_locus.mp3", "08: ");
        songList.add(song);

        song = new Song("Datassette", "1:12:02",
                "http://datashat.net/music_for_programming_9-datassette.mp3", "09: ");
        songList.add(song);

        song = new Song("Unity Gain Temple", "0:51:06",
                "http://datashat.net/music_for_programming_10-unity_gain_temple.mp3", "10: ");
        songList.add(song);

        mAdapter.notifyDataSetChanged();
    }

    // sets optionsmenu in appbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // handel click events in optionsmenu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_about) {
            // coming soon :)
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // from here: lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "onStart invoked");
        if (playIntent == null) {
            playIntent = new Intent(this, PlayerService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "onResume invoked");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause", "onPause invoked");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop", "onStop invoked");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "onDestroy invoked, PlayerService stopped and Broadcast unregistered");
        stopService(playIntent);
        musicSrv = null;
        unregisterReceiver(receiver);
    }

    public class MyBroadRequestReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("Start ProgressDialog")) {
                progressDialog.setMessage("Buffering...");
                progressDialog.show();
            }
            if (intent.getAction().equals("Stop ProgressDialog")) {
                progressDialog.cancel();
            }
        }
    }
}