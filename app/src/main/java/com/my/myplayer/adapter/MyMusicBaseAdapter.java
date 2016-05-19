package com.my.myplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.my.myplayer.R;
import com.my.myplayer.beans.SongInfo;
import com.my.myplayer.tools.MediaUtils;

import java.util.ArrayList;

/**
 * Created by dllo on 16/1/6.
 */
public class MyMusicBaseAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<SongInfo> songInfos;

    public MyMusicBaseAdapter(Context context, ArrayList<SongInfo> songInfos) {
        this.context = context;
        this.songInfos = songInfos;
    }

    public void setSongInfos(ArrayList<SongInfo> songInfos) {
        this.songInfos = songInfos;
    }

    @Override
    public int getCount() {
        return songInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return songInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.item_music_listview,viewGroup,false);
            viewHolder = new ViewHolder();
            viewHolder.tv_songer = (TextView) view.findViewById(R.id.tv_songer_item_music_listview);
            viewHolder.tv_SongName = (TextView) view.findViewById(R.id.tv_SongName_item_music_listview);
            viewHolder.tv_time = (TextView) view.findViewById(R.id.tv_time_item_music_listview);
            view.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) view.getTag();
        SongInfo songInfo = songInfos.get(i);
        viewHolder.tv_SongName.setText(songInfo.getTitle());
        viewHolder.tv_songer.setText(songInfo.getArtist());
        viewHolder.tv_time.setText(MediaUtils.formatTime(songInfo.getDuration()));
        return view;
    }

    static class ViewHolder{
        TextView tv_SongName;
        TextView tv_songer;
        TextView tv_time;
    }
}
