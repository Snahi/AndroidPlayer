package com.snavi.player;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;


public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {


    private ArrayList<Uri>    m_uris;
    private ArrayList<String> m_titles;
    private View              m_playBar;            // bar at the bottom of main activity with play/stop button
    private CardView          m_currentSongView;
    private Context           m_context;
    private MediaPlayer       m_mediaPlayer;
    private SeekBar           m_seekBar;



    SongsAdapter(ArrayList<Uri> uris, ArrayList<String> titles, View playBar, Context context,
                 MediaPlayer mediaPlayer)
    {
        m_uris     = uris;
        m_titles   = titles;
        m_playBar  = playBar;
        m_context  = context;
        m_seekBar  = m_playBar.findViewById(R.id.activity_main_seek_bar);

        m_mediaPlayer = mediaPlayer;
        m_mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        CardView cardView = (CardView) LayoutInflater.
                from(viewGroup.
                        getContext()).
                inflate(R.layout.song_card_view, viewGroup, false);

        return new ViewHolder(cardView);
    }




    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i)
    {
        viewHolder.bind(viewHolder.getAdapterPosition());
    }



    @Override
    public int getItemCount() {
        return m_uris.size();
    }



    private void setSongOnClickListener(CardView songView, final int adapterPosition)
    {
        songView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onClickColorChange(view);
                onClickMusicChange(adapterPosition);
                onClickBarChange(adapterPosition);
            }
        });
    }



    private void onClickColorChange(View view)
    {
        if (m_currentSongView != null)  // uncheck - change to basic color
        {
            m_currentSongView.setCardBackgroundColor(
                    view.getContext().getColor(R.color.song_card_view_non_selected_bg_color));
        }

        m_currentSongView = (CardView) view;
        m_currentSongView.setCardBackgroundColor(
                view.getContext().getColor(R.color.song_card_view_selected_bg_color));
    }



    private void onClickMusicChange(int uriIdx)
    {
        m_mediaPlayer.reset();
        try
        {
            m_mediaPlayer.setDataSource(m_context, m_uris.get(uriIdx));
            m_mediaPlayer.prepare();
            m_mediaPlayer.start();
            m_seekBar.setMax(m_mediaPlayer.getDuration() / 1000);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }



    private void onClickBarChange(int titleIdx)
    {
        m_playBar.setVisibility(View.VISIBLE);                                                          // make sure that is visible
        m_seekBar.setProgress(0);                                                                       // move slider to the beginning
        TextView totalTime = m_playBar.findViewById(R.id.activity_main_tv_total_time);                  // find tv with length of the song
        totalTime.setText(MainActivity.getInMinSecFormat(m_mediaPlayer.getDuration() / 1000));  // display length of the song
        TextView titleTv = m_playBar.findViewById(R.id.activity_main_tv_title);                         // find tv with song's title
        titleTv.setText(m_titles.get(titleIdx));                                                        // set song title (on bottom bar)
        ImageButton imageButton = m_playBar.findViewById(R.id.activity_main_play_button);               // find play/pause button
        imageButton.setImageDrawable(m_context.getDrawable(R.drawable.pause_icon));                     // change play/pause button to pause (song is playing)
    }


    // View Holder ////////////////////////////////////////////////////////////////////////////////

    class ViewHolder extends RecyclerView.ViewHolder {

        private CardView m_cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            m_cardView = (CardView) itemView;
        }



        void bind(int adapterPosition)
        {
            TextView titleTv = m_cardView.findViewById(R.id.song_card_view_song_title);                 // title in card view
            titleTv.setText(m_titles.get(adapterPosition));                                             // set title of current song in card view (list)
            setSongOnClickListener(m_cardView, adapterPosition);
        }
    }
}
