package com.snavi.player;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SongProgressListener {


    // CONST //////////////////////////////////////////////////////////////////////////////////////
    public static final int PERMISSION_CODE_EXTERNAL_STORAGE = 113;

    public static final Uri SONGS_URI  = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final int URI_IDX   = 0;
    private static final int TITLE_IDX = 1;


    // fields ////////////////////////////////////////////////////////////////////////////////////
    private ArrayList<Uri>             m_uris;
    private ArrayList<String>          m_titles;
    private MediaPlayer                m_mediaPlayer;
    private SongProgress               m_songProgress;
    private TextView                   m_currTime;
    private SeekBar                    m_seekBar;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        initMediaFields();
        initViews();
        initSongs();
        initRecyclerView();
        registerToSeekBar();
        setPlayPauseButtonListener();

        getExternalStoragePermission();
    }



    private void initMediaFields()
    {
        m_mediaPlayer = new MediaPlayer();
        m_songProgress = new SongProgress(m_mediaPlayer);
        m_songProgress.registerListener(this);
        m_songProgress.start();
    }



    private void initViews()
    {
        m_currTime = findViewById(R.id.activity_main_tv_curr_time);
        m_seekBar = findViewById(R.id.activity_main_seek_bar);
    }



    private void initRecyclerView()
    {
        // fields ////////////////////////////////////////////////////////////////////////////////////
        RecyclerView recyclerView = findViewById(R.id.activity_main_recycler_view);
        recyclerView.setHasFixedSize(true);

        SongsAdapter songsAdapter = new SongsAdapter(m_uris, m_titles,
                findViewById(R.id.activity_main_play_bar), this, m_mediaPlayer);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(songsAdapter);
    }



    private void initSongs()
    {
        m_uris   = new ArrayList<>();
        m_titles = new ArrayList<>();

        Cursor cursor = getCursor();

        if (cursor == null)
            return;

        while(cursor.moveToNext())
        {
            m_uris.add(Uri.parse(SONGS_URI + "/" + cursor.getString(URI_IDX)));
            m_titles.add(cursor.getString(TITLE_IDX));
        }

        cursor.close();
    }



    private Cursor getCursor()
    {
        ContentResolver contentResolver = getContentResolver();
        String[] projection = new String[2];
        projection[URI_IDX]   = "_id";
        projection[TITLE_IDX] = "title";

        return contentResolver.query(
                SONGS_URI,
                projection,
                null, null, null);
    }



    private void getExternalStoragePermission()
    {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
            this.requestPermissions(permission,PERMISSION_CODE_EXTERNAL_STORAGE);
        }
    }



    @Override
    public void onPause()
    {
        super.onPause();

        m_mediaPlayer.pause();

        // change button on play button
        ImageButton playButton = findViewById(R.id.activity_main_play_button);
        playButton.setImageDrawable(getDrawable(R.drawable.play_icon));
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();

        m_songProgress.stopReading();
        m_mediaPlayer.release();
    }



    @Override
    public void currentSecond(int currSecond)
    {
        setBarProgress(currSecond);

        // without this -> exception
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_currTime.setText(getInMinSecFormat(m_mediaPlayer.getCurrentPosition() / 1000));
            }
        });
    }



    private void registerToSeekBar()
    {
        SeekBar seekBar = findViewById(R.id.activity_main_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                m_mediaPlayer.seekTo(seekBar.getProgress() * 1000);
            }
        });
    }



    private void setPlayPauseButtonListener()
    {
        final ImageButton playButton = findViewById(R.id.activity_main_play_button);

        playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (m_mediaPlayer.isPlaying())
                {
                    m_mediaPlayer.pause();
                    playButton.setImageDrawable(getDrawable(R.drawable.play_icon));
                }
                else
                {
                    m_mediaPlayer.start();
                    playButton.setImageDrawable(getDrawable(R.drawable.pause_icon));
                }
            }
        });
    }



    private void setBarProgress(int progressInSeconds)
    {
        try // may be updated at the same time when user clicks
        {
            m_seekBar.setProgress(progressInSeconds);
        }
        catch (ConcurrentModificationException e)
        {
            e.printStackTrace();
        }
    }



    public static String getInMinSecFormat(int seconds)
    {
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }
}
