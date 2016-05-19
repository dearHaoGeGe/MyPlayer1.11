package com.my.myplayer.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.my.myplayer.R;
import com.my.myplayer.beans.SongInfo;
import com.my.myplayer.service.PlayService;
import com.my.myplayer.tools.MediaUtils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/1/8.
 */
public class PlayActivity extends BaseActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener {

    private TextView tv_title, tv_start_time, tv_end_time;
    private ImageView iv_album, tv_previous, iv_play_pause, iv_next, iv_play_mode;
    private SeekBar seekBar;
    private ArrayList<SongInfo> songInfos;
    private int position;
    private static final int UPDATE_TIME = 0x1;     //更新播放时间的标记
    private boolean isPause = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_start_time = (TextView) findViewById(R.id.tv_start_time);
        tv_end_time = (TextView) findViewById(R.id.tv_end_time);
        iv_album = (ImageView) findViewById(R.id.iv_album);
        tv_previous = (ImageView) findViewById(R.id.tv_previous);
        iv_play_pause = (ImageView) findViewById(R.id.iv_play_pause);
        iv_next = (ImageView) findViewById(R.id.iv_next);
        iv_play_mode = (ImageView) findViewById(R.id.iv_play_mode);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(this);
        tv_previous.setOnClickListener(this);
        iv_play_pause.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        iv_play_mode.setOnClickListener(this);

        songInfos = MediaUtils.getSongInfos(this);

        myHandler = new MyHandler(this);      //一定要先声明在赋值
        isPause = getIntent().getBooleanExtra("isPause", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();      //绑定服务
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();    //解除绑定服务
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //当进度发生变化的时候    boolean fromUser表示是否是用户拖动的
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            playService.pause();
            playService.seekTo(progress);
        }
    }

    //开始拖动进度条的事件
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    //结束拖动进度条的事件
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        playService.start();
    }

    private static MyHandler myHandler;     //一定要先声明在赋值
    static class MyHandler extends Handler {

        private PlayActivity playActivity;

        public MyHandler(PlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (playActivity != null) {
                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.tv_start_time.setText(MediaUtils.formatTime(msg.arg1));
                        break;
                }
            }
        }
    }

    //实时更新进度条和时间
    @Override
    public void publish(int progress) {
        Message message = myHandler.obtainMessage(UPDATE_TIME);
        message.arg1 = progress;
        myHandler.sendMessage(message);
        seekBar.setProgress(progress);
    }

    @Override
    public void change(int position) {
        SongInfo songInfo = songInfos.get(position);
        tv_title.setText(songInfo.getTitle());      //设置歌名
        //设置图片
        Bitmap albumBitmap = MediaUtils.getArtwork(this, songInfo.getId(), songInfo.getAlbumId(), true, false);
        iv_album.setImageBitmap(albumBitmap);
        tv_end_time.setText(MediaUtils.formatTime(songInfo.getDuration()));     //获取歌的时间

        seekBar.setProgress(0); //设置当前的进度
        seekBar.setMax((int) songInfo.getDuration());   //进度的最大值
        if (playService.isPlaying()) {
            iv_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);       //改变播放按键
        } else {
            iv_play_pause.setImageResource(R.mipmap.player_btn_play_normal);       //改变播放按键
        }
        //选择播放模式
        switch (playService.getPlay_mode()) {
            case PlayService.ORDER_PLAY:
                iv_play_mode.setImageResource(R.mipmap.order);
                iv_play_mode.setTag(PlayService.ORDER_PLAY);    //设置标记
                break;

            case PlayService.RANDOM_PLAY:
                iv_play_mode.setImageResource(R.mipmap.random);
                iv_play_mode.setTag(PlayService.RANDOM_PLAY);   //设置标记
                break;

            case PlayService.SINGLE_PLAY:
                iv_play_mode.setImageResource(R.mipmap.single);
                iv_play_mode.setTag(PlayService.SINGLE_PLAY);   //设置标记
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play_pause:
                if (playService.isPlaying()) {
                    iv_play_pause.setImageResource(R.mipmap.play);
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        //如果是暂停的话换成播放图片
                        iv_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        playService.start();   //播放
                    } else {
                        playService.play(0);
                    }
                    isPause = false;
                }
                break;

            case R.id.iv_next:
                playService.next();
                break;

            case R.id.tv_previous:
                playService.prev();
                break;

            case R.id.iv_play_mode:
                int mode= (int) iv_play_mode.getTag();
                switch (mode){
                    case PlayService.ORDER_PLAY:
                        iv_play_mode.setImageResource(R.mipmap.random);
                        iv_play_mode.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.random_play), Toast.LENGTH_SHORT).show();
                        break;

                    case PlayService.RANDOM_PLAY:
                        iv_play_mode.setImageResource(R.mipmap.single);
                        iv_play_mode.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.single_play), Toast.LENGTH_SHORT).show();
                        break;

                    case PlayService.SINGLE_PLAY:
                        iv_play_mode.setImageResource(R.mipmap.order);
                        iv_play_mode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.order_play), Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            default:
                break;
        }
    }
}
