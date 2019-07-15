package com.bignox.ijkplayerdemo;

import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.zonekey.ijkplayer_view.IjkPlayerManager;
import com.zonekey.ijkplayer_view.video.VideoMediaController;
import com.zonekey.ijkplayer_view.view.IjkPlayerView;
import com.zonekey.ijkplayer_view.widget.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private IjkVideoView ijkVideoView;
    private PowerManager.WakeLock wakeLock;
    private VideoMediaController mVideoController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();
    }

    private void initializeView() {
        IjkPlayerManager.init();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);   //启用屏幕常亮功能
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        wakeLock.acquire();

        ijkVideoView = findViewById(R.id.ijkplayerview);

        mVideoController = new VideoMediaController();
        ijkVideoView.setMediaController(mVideoController);
        ijkVideoView.setOnPreparedListener(mOnPreparedListener);
        ijkVideoView.setOnErrorListener(mOnErrorListener);
        ijkVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoController.setOnVideoControllerClickListener(mOnVideoControllerClickListener);

//        String exhibitionUrl = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
        String  exhibitionUrl = "http://www.w3school.com.cn/i/movie.mp4";
        ijkVideoView.setVideoPath(exhibitionUrl);
        ijkVideoView.requestFocus();
        ijkVideoView.start();
    }


    IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mVideoController.setPreparedState();
            Log.d(TAG, "onPreparedListener");
        }
    };

    IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            mVideoController.setErrorState();
            Log.e(TAG, "framework error + " + i + " impel error + " + i1);
            return false;
        }
    };
    IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            mVideoController.setCompleteState();
            Log.d(TAG, "OnCompletionListener");
        }
    };

    VideoMediaController.OnVideoControllerClickListener mOnVideoControllerClickListener = new VideoMediaController.OnVideoControllerClickListener() {
        @Override
        public void clickScreenChange(int screenState) {

        }

        @Override
        public void back() {

        }

        @Override
        public void downloadCache() {

        }

        @Override
        public void share() {

        }

        @Override
        public void danmukuStateChange(boolean isShow) {

        }
    };
}
