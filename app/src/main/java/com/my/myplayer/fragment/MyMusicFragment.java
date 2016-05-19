package com.my.myplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.my.myplayer.R;
import com.my.myplayer.activity.MainActivity;
import com.my.myplayer.activity.PlayActivity;
import com.my.myplayer.adapter.MyMusicBaseAdapter;
import com.my.myplayer.beans.SongInfo;
import com.my.myplayer.tools.MediaUtils;

import java.util.ArrayList;

/**
 * Created by dllo on 16/1/6.
 */
public class MyMusicFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView listView;
    private ImageView iv_music, iv_playmusic, iv_nextmusic;
    private TextView tv_SongName, tv_singer;
    private ArrayList<SongInfo> songInfos;
    private MyMusicBaseAdapter adapter;

    private MainActivity mainActivity;

    private boolean isPause = false;   //判断是否是暂停状态
    private int position = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public static MyMusicFragment newInstance() {
        MyMusicFragment myMusicFragment = new MyMusicFragment();
        return myMusicFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_music, null);
        listView = (ListView) view.findViewById(R.id.ListView_my_music_fragment);

        iv_music = (ImageView) view.findViewById(R.id.iv_music_my_music_fragment);
        iv_playmusic = (ImageView) view.findViewById(R.id.iv_playmusic);
        iv_nextmusic = (ImageView) view.findViewById(R.id.iv_nextmusic);
        tv_SongName = (TextView) view.findViewById(R.id.tv_SongName);
        tv_singer = (TextView) view.findViewById(R.id.tv_singer);

        listView.setOnItemClickListener(this);
        iv_playmusic.setOnClickListener(this);
        iv_nextmusic.setOnClickListener(this);
        iv_music.setOnClickListener(this);
        loadData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity.bindPlayService();  //绑定播放服务
    }

    @Override
    public void onPause() {
        super.onPause();
        mainActivity.unbindPlayService();   //解除绑定
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * 加载本地音乐列表
     */
    private void loadData() {
        songInfos = MediaUtils.getSongInfos(mainActivity);
        adapter = new MyMusicBaseAdapter(mainActivity, songInfos);
        listView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mainActivity.playService.play(position);
    }

    //回调播放状态下得UI设置
    public void changeUIStatusOnPlay(int position) {
        if (position >= 0 && position < songInfos.size()) {
            SongInfo songInfo = songInfos.get(position);
            tv_SongName.setText(songInfo.getTitle());
            tv_singer.setText(songInfo.getArtist());

            if (mainActivity.playService.isPlaying()) {
                iv_playmusic.setImageResource(R.mipmap.player_btn_pause_normal);
            } else {
                iv_playmusic.setImageResource(R.mipmap.player_btn_play_normal);
            }

            Bitmap albumBitmap = MediaUtils.getArtwork(mainActivity, songInfo.getId(), songInfo.getAlbumId(), true, true);
            iv_music.setImageBitmap(albumBitmap);
            this.position = position;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 暂停/播放
            case R.id.iv_playmusic:
                if (mainActivity.playService.isPlaying()) {
                    //如果是正在播放就换位暂停图片
                    iv_playmusic.setImageResource(R.mipmap.player_btn_play_normal);
                    mainActivity.playService.pause();   //暂停
                    isPause = true;
                } else {
                    if (mainActivity.playService.isPause()) {
                        //如果是暂停的话换成播放图片
                        iv_playmusic.setImageResource(R.mipmap.player_btn_pause_normal);
                        mainActivity.playService.start();   //播放
                    } else {
                        mainActivity.playService.play(0);
                    }
                    isPause = false;
                }
                break;

            //下一首
            case R.id.iv_nextmusic:
                mainActivity.playService.next();
                break;

            case R.id.iv_music_my_music_fragment:
                Intent intent = new Intent(mainActivity, PlayActivity.class);
                intent.putExtra("isPause", isPause);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
