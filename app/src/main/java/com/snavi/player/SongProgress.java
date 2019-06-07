package com.snavi.player;

import android.media.MediaPlayer;
import java.util.ArrayList;

public class SongProgress extends Thread {


    // CONST //////////////////////////////////////////////////////////////////////////////////////
    private static final int TIME_BETWEEN_READS = 1000;


    // fields /////////////////////////////////////////////////////////////////////////////////////
    private MediaPlayer m_mediaPlayer;

    private long m_lastMeasure;     // time of last measure
    private int  m_currSecond;      // current progress in seconds

    private boolean m_stop;         // false as long as should listen for progress

    private ArrayList<SongProgressListener> m_listeners;



    SongProgress(MediaPlayer mediaPlayer)
    {
        m_mediaPlayer = mediaPlayer;
        m_currSecond  = m_mediaPlayer.getCurrentPosition();
        m_listeners   = new ArrayList<>();
    }



    @Override
    public void run()
    {
        while(!m_stop)
        {
            if (System.currentTimeMillis() - m_lastMeasure >= TIME_BETWEEN_READS)
            {
                m_currSecond = m_mediaPlayer.getCurrentPosition() / 1000;
                notifyListeners();
                m_lastMeasure = System.currentTimeMillis();
            }
        }
    }



    void stopReading()
    {
        m_stop = true;

        try
        {
            this.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }



    void registerListener(SongProgressListener listener)
    {
        m_listeners.add(listener);
    }



    private void notifyListeners()
    {
        for (SongProgressListener listener : m_listeners)
            listener.currentSecond(m_currSecond);
    }
}
