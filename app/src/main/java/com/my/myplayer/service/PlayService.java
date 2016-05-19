package com.my.myplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.my.myplayer.beans.SongInfo;
import com.my.myplayer.tools.MediaUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音乐播放的服务组件
 * 实现功能:
 * 1、播放
 * 2、暂停
 * 3、上一首
 * 4、下一首
 * 5、获取当前的播放进度
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mediaPlayer;
    private int currentPosition;     //当前正在播放歌区的位置
    ArrayList<SongInfo> songInfos;
    private MusicUpdateListener musicUpdateListener;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isPause = false;    //用来确定歌曲是否是在暂停状态
    //三种播放模式
    public static final int ORDER_PLAY = 1;     //顺序
    public static final int RANDOM_PLAY = 2;    //随机
    public static final int SINGLE_PLAY = 3;    //单曲循环
    private int play_mode = ORDER_PLAY;         //默认为顺序播放

    public int getPlay_mode() {
        return play_mode;
    }

    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public boolean isPause() {
        return isPause;
    }

    public PlayService() {

    }

    //得到当前位置
    public int getCurrentPosition() {
        return currentPosition;
    }

    public class PlayBinder extends Binder {

        public PlayService getPlayService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);

        songInfos = MediaUtils.getSongInfos(this);
        executorService.execute(updateStatusRunnable);
    }

    private Random random = new Random();

    //MediaPlayer.OnCompletionListener接口实现的方法
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        switch (play_mode) {
            case ORDER_PLAY:
                next();
                break;
            case RANDOM_PLAY:
                play(random.nextInt(songInfos.size()));
                break;
            case SINGLE_PLAY:
                play(currentPosition);
                break;
            default:
                break;
        }
    }

    //MediaPlayer.OnErrorListener接口实现的方法
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //回收线程
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            executorService = null;
        }
    }

    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (musicUpdateListener != null && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    musicUpdateListener.onPublish(getCurrentProgress());
                }
                try {
                    Thread.sleep(500);  //控制刷新进度的速度
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //播放
    public void play(int position) {
        if (position >= 0 && position < songInfos.size()) {
            SongInfo songInfo = songInfos.get(position);
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(this, Uri.parse(songInfo.getUrl()));
                mediaPlayer.prepare();
                mediaPlayer.start();
                currentPosition = position;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (musicUpdateListener != null) {
                musicUpdateListener.onChange(currentPosition);
            }
        }

    }

    //暂停
    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    //下一首
    public void next() {
        if (currentPosition + 1 >= songInfos.size() - 1) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);
    }

    //上一首
    public void prev() {
        if (currentPosition - 1 < 0) {
            currentPosition = songInfos.size() - 1;
        } else {
            currentPosition--;
        }
        play(currentPosition);
    }

    //开始
    public void start() {
        //如果mediaPlayer不是正在播放的状态并且不等于空,开始播放
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();    //开始播放
        }
    }

    //判断音乐是否是正在播放
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    public int getCurrentProgress() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    //控制进度条调到哪
    public void seekTo(int mesc) {
        mediaPlayer.seekTo(mesc);
    }

    //更新状态的接口
    public interface MusicUpdateListener {
        public void onPublish(int progress);    //进度条更新

        public void onChange(int position);     //更新当前所在的位置
    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }
}
