package com.compassl.anji.songs_ssw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/10/28.
 */

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder>{

    private List<Song> mSongList = new ArrayList<>();
    private Context mContext;
    private OnItemClickListenerRV mListener = null;
    private int currentPosition;


    public static interface OnItemClickListenerRV{
        void onItemClickRV(int position);
    }

    public void setOnItemClickListenerRV(OnItemClickListenerRV listener){
            this.mListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        if (mContext==null){
            mContext = parent.getContext();
        }
        final View view = LayoutInflater.from(mContext).inflate(R.layout.song_list_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        currentPosition= holder.getAdapterPosition();
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener!=null){
                    int position = holder.getAdapterPosition();
                    mListener.onItemClickRV(position);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = mSongList.get(position);
        holder.song_name.setText(song.getName());
        holder.song_name.getPaint().setFakeBoldText(true);
        Glide.with(mContext).load(song.getImgRes()).into(holder.song_img);
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    public RvAdapter(List<Song> mSongList) {
        this.mSongList = mSongList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView song_img;
        TextView song_name;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cv_item);
            song_img = (ImageView) itemView.findViewById(R.id.iv_song_img);
            song_name = (TextView) itemView.findViewById(R.id.tv_song_name);
        }
    }


}