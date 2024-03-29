package com.zonekey.ijkplayer_view.view;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zonekey.ijkplayer_view.R;
import com.zonekey.ijkplayer_view.adapter.IjkDefinitionAdapter;
import com.zonekey.ijkplayer_view.bean.VideoDefinition;
import com.zonekey.ijkplayer_view.utils.IjkAnimUtils;
import com.zonekey.ijkplayer_view.utils.SystemVoiceUtils;
import com.zonekey.ijkplayer_view.widget.IjkVideoView;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by xu.wang
 * Date on  2018/9/5 14:09:07.
 *
 * @Desc
 */
public class IjkMainPageView extends RelativeLayout implements View.OnClickListener {
    private final String TAG = "IjkPlayerView";
    private IjkVideoView ijkVideoView;

    private ImageView iv_nodata;
    private ProgressBar pb;

    public static final int IJK_PLAYING = 1001; //播放
    public static final int IJK_LOADING = 1002; //加载
    public static final int IJK_NODATA = 1003;  //播放失败
    public static final int IJK_START = 1004;   //未开始播放的状态

    public static final int FULL_SCREEN = 111;  //全屏
    public static final int SHRINK_SCREEN = 112;    //非全屏
    private int mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private RelativeLayout rl_ctrl;
    private ImageView iv_start;
    private ImageView iv_fullscreen;
    private int mCurState = IJK_NODATA;
    private int mCurScreen = SHRINK_SCREEN;
    private ViewGroup mParentView;  //点击全屏时记录全屏时的父布局...
    private AppCompatActivity mActivity; //记录当前Activity;用于获取当前Activity的window
    private RelativeLayout rl_loading;
    public int mDefinitionPos = 0;  //播放当前位置的流
    private ImageView iv_replay;
    private String currentUrl;

    public interface OnIjkPlayerClickListener {
        void onClick();
    }

    public OnIjkPlayerClickListener mClickListener;

    public void setOnIjkPlayerClickListener(OnIjkPlayerClickListener listener) {
        this.mClickListener = listener;
    }


    public IjkMainPageView(Context context) {
        this(context, null);
    }

    public IjkMainPageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkMainPageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialView();
    }

    private void initialView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_ijkplayer_mainpage, this, false);
        ijkVideoView = view.findViewById(R.id.ijk_view_ijk_videoview);
        iv_nodata = view.findViewById(R.id.iv_view_ijk_nodata);
        pb = view.findViewById(R.id.pb_view_ijk);
        rl_loading = view.findViewById(R.id.rl_view_ijk_loading);
        rl_ctrl = view.findViewById(R.id.rl_view_ijk_control);
        iv_start = view.findViewById(R.id.iv_view_ijk_start);
        iv_fullscreen = view.findViewById(R.id.iv_view_ijk_fullscreen);
        iv_replay = view.findViewById(R.id.iv_view_ijk_replay);

        initialData();
        addView(view);
    }

    public void init(AppCompatActivity activity) {
        this.mActivity = activity;
        mOrientation = mActivity.getResources().getConfiguration().orientation;
    }

    private void initialData() {
        iv_start.setOnClickListener(this);
        iv_fullscreen.setOnClickListener(this);
        iv_replay.setOnClickListener(this);
        rl_ctrl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        ijkVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        setState(IJK_PLAYING);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
//                        setState(IJK_LOADING);
                        Log.e(TAG, "Media_info_buffering_end");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        setState(IJK_PLAYING);
                        break;
                    case IMediaPlayer.MEDIA_INFO_TIMED_TEXT_ERROR:
                    case IMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    case IMediaPlayer.MEDIA_ERROR_TIMED_OUT:
                    case IMediaPlayer.MEDIA_ERROR_IO:
                    case IMediaPlayer.MEDIA_ERROR_MALFORMED:
                    case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED:
//                    case IMediaPlayer.MEDIA_INFO_UNSUPPORT_EXCEPTION:
                    case IMediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        setState(IJK_NODATA);
                        break;
                }
                return false;
            }
        });
        ijkVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                setState(IJK_NODATA);
                return false;
            }
        });
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) mClickListener.onClick();
                if (rl_ctrl.getVisibility() == View.VISIBLE) {
                    rl_ctrl.setVisibility(View.GONE);
                    IjkAnimUtils.menuAnim(0, rl_ctrl.getMeasuredHeight(), 1.0f, 0.3f, rl_ctrl, getContext(), 300);
                } else {
                    rl_ctrl.setVisibility(View.VISIBLE);
                    IjkAnimUtils.menuAnim(rl_ctrl.getMeasuredHeight(), 0, 0.3f, 1.0f, rl_ctrl, getContext(), 300);
                }
            }
        });
    }

    /**
     * 设置加载状态
     *
     * @param state
     */
    public void setState(int state) {
        if (state == IJK_LOADING) {
            mCurState = IJK_LOADING;
            if (rl_loading.getVisibility() != View.VISIBLE) rl_loading.setVisibility(View.VISIBLE);
            if (pb.getVisibility() != View.VISIBLE) pb.setVisibility(View.VISIBLE);
            if (iv_nodata.getVisibility() != View.GONE) iv_nodata.setVisibility(View.GONE);
        } else if (state == IJK_NODATA) {
            mCurState = IJK_NODATA;
            if (rl_loading.getVisibility() != View.VISIBLE) rl_loading.setVisibility(View.VISIBLE);
            if (pb.getVisibility() != View.GONE) pb.setVisibility(View.GONE);
            if (iv_nodata.getVisibility() != View.VISIBLE) iv_nodata.setVisibility(View.VISIBLE);
        } else if (state == IJK_PLAYING) {
            mCurState = IJK_PLAYING;
            if (rl_loading.getVisibility() != View.GONE) rl_loading.setVisibility(View.GONE);
            if (pb.getVisibility() != View.GONE) pb.setVisibility(View.GONE);
            if (iv_nodata.getVisibility() != View.GONE) iv_nodata.setVisibility(View.GONE);
        } else if (state == IJK_START) {
            mCurState = IJK_START;
            if (rl_loading.getVisibility() != View.GONE) rl_loading.setVisibility(View.GONE);
            if (pb.getVisibility() != View.GONE) pb.setVisibility(View.GONE);
            if (iv_nodata.getVisibility() != View.GONE) iv_nodata.setVisibility(View.GONE);
        }
    }

    /**
     * 设置标清,高清等情况,,,
     *
     * @param urls
     */
    public void setVideoPath(List<VideoDefinition> urls) {
        if (urls == null || urls.size() == 0) {
            setState(IJK_NODATA);
            return;
        }
        setState(IJK_LOADING);
        setVideoPath(urls.get(mDefinitionPos).getUrl());
    }

    public void setVideoPath(String url) {
        this.currentUrl = url;
        if (TextUtils.isEmpty(currentUrl)) {
            setState(IJK_NODATA);
            return;
        }
        setState(IJK_LOADING);
        Log.i(TAG, "play url = " + url);
        ijkVideoView.setVideoPath(url);
    }


    /**
     * ANDROID VERSION_CODE 14 以上有效,(调用了TextureView的 getBitmap()方法)
     *
     * @return
     */
    public Bitmap getBitmap() {
        return ijkVideoView.getBitmap();
    }

    public void startPlay() {
        if (TextUtils.isEmpty(currentUrl)) {
            return;
        }
        iv_start.setImageResource(R.drawable.select_ijkplayer_pause);
        ijkVideoView.start();
    }

    public void stopPlay() {
        iv_start.setImageResource(R.drawable.select_ijkplayer_play);
        if (ijkVideoView.isPlaying()) {
            ijkVideoView.stopBackgroundPlay();
            ijkVideoView.stopPlayback();
        }
    }

    public void stopBackGroundAudio() {
        ijkVideoView.stopBackgroundPlay();
    }

    public void release() {
        ijkVideoView.release(true);
    }

    public void pausePlay() {
        iv_start.setImageResource(R.drawable.select_ijkplayer_play);
        stopPlay();
    }

    public boolean isPlaying() {
        if (ijkVideoView != null && ijkVideoView.isPlaying()) return true;
        return false;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_view_ijk_start) {
            if (ijkVideoView.isPlaying()) {
                pausePlay();
            } else {
                startPlay();
            }
        } else if (i == R.id.iv_view_ijk_fullscreen) {
            changeScreenState();
        } else if (i == R.id.iv_view_ijk_replay) {
            if (isPlaying()) {
                stopPlay();
            }
            setVideoPath(currentUrl);
            startPlay();
        }
    }

    /**
     * 改变屏幕状态
     */
    private void changeScreenState() {
        if (mActivity == null) {
            Log.e("IjkPlayerView", "还没有设置当前的BaseActivity");
            return;
        }
        if (mCurScreen == SHRINK_SCREEN) {  //记录非全屏下的状态,将要执行全屏
            if (mOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else if (mCurScreen == FULL_SCREEN) {
            if (mOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        }

        if (mCurScreen == FULL_SCREEN) {
            mCurScreen = SHRINK_SCREEN;
            iv_fullscreen.setImageResource(R.drawable.select_ijkplayer_fullscreen);
            ((ViewGroup) this.getParent()).removeView(this);
            mParentView.addView(this);
        } else if (mCurScreen == SHRINK_SCREEN) {
            mCurScreen = FULL_SCREEN;
            iv_fullscreen.setImageResource(R.drawable.select_ijkplayer_shinkscreen);
            mParentView = ((ViewGroup) this.getParent());
            mParentView.removeView(this);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mActivity.getWindow().addContentView(this, layoutParams);
        }
    }

    public void resume() {
        setVideoPath(currentUrl);
        startPlay();
    }

    /**
     * 当前是不是全屏
     *
     * @return
     */
    public boolean isFullScreen() {
        if (mCurScreen == FULL_SCREEN) {
            return true;
        } else {
            return false;
        }
    }

    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        ijkVideoView.setOnPreparedListener(l);
    }

    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        ijkVideoView.setOnCompletionListener(l);
    }

    public void setShinkScreen() {
        changeScreenState();
    }
}
