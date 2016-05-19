package com.my.myplayer.tools;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.my.myplayer.R;
import com.my.myplayer.beans.SongInfo;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by dllo on 16/1/6.
 */
public class MediaUtils {

    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

    /**
     * @param context
     * @param Duration
     * @return
     */
    public static SongInfo getSongInfo(Context context, long Duration) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media._ID + " " + Duration, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        SongInfo songInfo = null;

        if (cursor.moveToNext()) {
            songInfo = new SongInfo();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));            //音乐ID
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));   //音乐标题
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)); //艺术家
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));   //专辑
            long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));   //专辑ID
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); //歌曲时长
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));         //文件大小
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));      //文件路径
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));    //是否为音乐
            if (isMusic != 0) {
                songInfo.setId(id);
                songInfo.setTitle(title);
                songInfo.setArtist(artist);
                songInfo.setAlbum(album);
                songInfo.setAlbumId(albumId);
                songInfo.setDuration(duration);
                songInfo.setSize(size);
                songInfo.setUrl(url);
            }
        }
        cursor.close();
        return songInfo;
    }

    /**
     * 用于从数据库中查询歌曲的信息,保存在List当中
     *
     * @param context
     * @return
     */
    public static long[] getSongInfoIds(Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.DURATION + ">=180000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        long[] ids = null;
        if (cursor != null) {
            ids = new long[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                ids[i] = cursor.getLong(0);
            }
        }
        cursor.close();
        return ids;
    }

    /**
     * 用于从数据库中查询歌曲的信息,保存在List当中
     */
    public static ArrayList<SongInfo> getSongInfos(Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DURATION + ">180000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        ArrayList<SongInfo> songInfos = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            SongInfo songInfo = new SongInfo();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));    //音乐ID
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));   //音乐标题(歌名)
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));   //艺术家
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));   //专辑
            long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));   //专辑ID
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); //时长
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)); //文件大小
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));  //文件路径
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));    //是否是音乐
            if (isMusic != 0) {
                songInfo.setId(id);
                songInfo.setTitle(title);
                songInfo.setArtist(artist);
                songInfo.setAlbum(album);
                songInfo.setAlbumId(albumId);
                songInfo.setDuration(duration);
                songInfo.setSize(size);
                songInfo.setUrl(url);
                songInfos.add(songInfo);
            }

        }
        cursor.close();
        return songInfos;
    }

    /**
     * 格式化时间,将毫秒转换为分秒:格式
     *
     * @param time
     * @return
     */
    public static String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }

    /**
     * 获取默认专辑图片
     *
     * @param context
     * @param small
     * @return
     */
    public static Bitmap getDefaultArtwork(Context context, boolean small) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if (small) {      //返回小图
            return BitmapFactory.decodeStream(context.getResources().openRawResource(R.mipmap.app_logo2), null, opts);
        }
        return BitmapFactory.decodeStream(context.getResources().openRawResource(R.mipmap.app_logo2), null, opts);
    }

    /**
     * 从文件当中获取专辑封面位图
     *
     * @param context
     * @param songid
     * @param albumid
     * @return
     */
    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bitmap = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("音乐指定一个songid或者albumid");
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }

            options.inSampleSize = 1;
            //只进行大小判断
            options.inJustDecodeBounds = true;
            //调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            //在800pixel的画面上显示,需要调用computeSampleSize得到图片缩放的比例
            options.inSampleSize = 100;
            //得到了缩放比例,现在开始读入Bitmap数据
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //根据options参数,减少所需要的内存
            bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //获取专辑封面位图对象
    public static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefalut, boolean small) {
        if (album_id < 0) {
            if (song_id < 0) {
                Bitmap bitmap = getArtworkFromFile(context, song_id, -1);
                if (bitmap != null) {
                    return bitmap;
                }
            }
            if (allowdefalut) {
                return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
        if (uri != null) {
            InputStream in = null;

            try {
                in = res.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //先制定原始大小
                options.inSampleSize = 1;
                //只进行大小判断
                options.inJustDecodeBounds = true;
                //调用此方法得到options得到图片的大小
                BitmapFactory.decodeStream(in, null, options);
                /* 我们的目标是在你的N pixel的画面上显示。所以需要调用computeSampleSize得到图片缩放的比例 */
                /* 这里的target为800时根据默认专辑图片大小决定的,800只是测试数字但是试验后发现完美的结合 */
                if (small) {
                    options.inSampleSize = computeSampleSize(options, 40);
                } else {
                    options.inSampleSize = computeSampleSize(options, 600);
                }
                //我们得到了缩放比例,现在开始读入Bitmap数据
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, options);
            } catch (FileNotFoundException e) {
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefalut) {
                            return getDefaultArtwork(context, small);
                        }
                    }
                } else if (allowdefalut) {
                    bm = getDefaultArtwork(context, small);
                }
                return bm;

            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**对图片进行
     * @param options
     * @param target
     * @return
     */
    private static int computeSampleSize(BitmapFactory.Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if (candidate == 0) {
            return 1;
        }
        if (candidate > 1) {
            if ((w > target) && (w / candidate) < target) {
                candidate -= 1;
            }
        }
        if (candidate > 1) {
            if ((h > target) && (h / candidate) < target) {
                candidate -= 1;
            }
        }
        return candidate;
    }

}
