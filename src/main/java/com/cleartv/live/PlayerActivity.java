package com.cleartv.live;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Locale;

import static com.cleartv.live.Utils.getBeanListFromJson;

/**
 * An activity that plays media using {@link SimpleExoPlayer}.
 */
public class PlayerActivity extends Activity {

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    private static AdaptiveMediaSourceEventListener adaptiveMediaSourceEventListener =
            new AdaptiveMediaSourceEventListener() {
                @Override
                public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {

                }

                @Override
                public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

                }

                @Override
                public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

                }

                @Override
                public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {

                }

                @Override
                public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {

                }

                @Override
                public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {

                }
            };

    private static ExtractorMediaSource.EventListener eventListener =
            new ExtractorMediaSource.EventListener() {
                @Override
                public void onLoadError(IOException error) {

                }
            };

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private static Handler mainHandler;
    private SimpleExoPlayerView simpleExoPlayerView;

    private static DataSource.Factory mediaDataSourceFactory;
    private static SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;

    private boolean shouldAutoPlay;


    //    private RecyclerView channel_recycler_view;
    private ListView channel_list_view;
    private View channel_list;

    public String type = "Live";
    public String data;
    public static String languageCode = "en-US";
    public static String mainPagePrefix = "http://192.168.18.101/openvod/now";

    private ArrayList<Channel> mChannels;
    private TextView channel_list_title;
    private TextView channel_id;

    private String channelID = "";
    private long historyChannelNum = 0;

    private static final int DELAYMILLIS = 5000;

    // Activity lifecycle
    private static final int MSG_CHECK_CHANNEL_ID = 1001;
    private static final int MSG_HIDE_CHANNELH = 1002;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CHECK_CHANNEL_ID:
                    int id = Integer.parseInt(channelID);
                    if (id != channel_list_view.getSelectedItemPosition()) {
                        if (id < mChannels.size())
                            channel_list_view.setSelection(id);
                        else {
                            channel_id.setAlpha(0f);
                            channel_id.setText(channel_list_view.getSelectedItemPosition()+"");
                            Utils.showToast(PlayerActivity.this, R.string.invalid_channel);
                        }
                    } else {
                        channel_id.setAlpha(0f);
                    }
                    break;
                case MSG_HIDE_CHANNELH:
                    channel_list.setAlpha(0f);
                    channel_id.setAlpha(0f);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shouldAutoPlay = true;
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        setContentView(R.layout.activity_player);

        initView();
    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        shouldAutoPlay = true;
        setIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePlayer();
        } else {
            Utils.showToast(this, R.string.storage_permission_denied);
            finish();
        }
    }

    // Activity input

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_0:
                case KeyEvent.KEYCODE_1:
                case KeyEvent.KEYCODE_2:
                case KeyEvent.KEYCODE_3:
                case KeyEvent.KEYCODE_4:
                case KeyEvent.KEYCODE_5:
                case KeyEvent.KEYCODE_6:
                case KeyEvent.KEYCODE_7:
                case KeyEvent.KEYCODE_8:
                case KeyEvent.KEYCODE_9:
                    insertChannelIndex(event.getKeyCode() - KeyEvent.KEYCODE_0);
                    break;
                case KeyEvent.KEYCODE_CHANNEL_UP:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_PAGE_UP:
                    event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP);
                    if (channel_list_view.getSelectedItemPosition() == 0)
                        channel_list_view.setSelection(mChannels.size() - 1);
                    KeyDpadUp();
                    break;
                case KeyEvent.KEYCODE_CHANNEL_DOWN:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_PAGE_DOWN:
                    event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN);
                    if (channel_list_view.getSelectedItemPosition() == mChannels.size() - 1)
                        channel_list_view.setSelection(0);
                    KeyDpadDown();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN);
                    KeyDpadLeft();
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP);
                    KeyDpadRight();
                    break;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    mHandler.removeCallbacksAndMessages(null);
                    if (channel_list.getAlpha() > 0) {
                        channel_list.setAlpha(0f);
                    } else {
                        channel_list.setAlpha(1f);
                        channel_id.setAlpha(1f);
                        mHandler.sendEmptyMessageDelayed(MSG_HIDE_CHANNELH, DELAYMILLIS);
                    }
                    channel_list.requestFocus();
                    KeyDpadCenter();
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (channel_list.getAlpha() > 0) {
                        channel_list.setAlpha(0f);
                        return true;
                    }
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    private void KeyDpadCenter() {
        Log.e("lpf", "KeyDpadCenter");
    }

    private void KeyDpadRight() {
        Log.e("lpf", "KeyDpadRight");
    }

    private void KeyDpadLeft() {
        Log.e("lpf", "KeyDpadLeft");
    }

    private void KeyDpadDown() {
        Log.e("lpf", "KeyDpadDown");
    }

    private void KeyDpadUp() {
        Log.e("lpf", "KeyDpadUp");
    }

    private void insertChannelIndex(int i) {
        Log.e("lpf", "sertChannelIndex：" + i);
        if (System.currentTimeMillis() - historyChannelNum > DELAYMILLIS) {
            channelID = i + "";
            historyChannelNum = System.currentTimeMillis();
        } else if (channelID.length() < 3) {
            channelID += i;
            historyChannelNum = System.currentTimeMillis();
        } else {
            return;
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_CHANNEL_ID, DELAYMILLIS);
        channel_id.setAlpha(1f);
        channel_id.setText(channelID);
    }

    private void setLanguage() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Configuration config = getResources().getConfiguration();
        if(languageCode.startsWith("zh")){
            config.locale = Locale.CHINA;
        }else{
            config.locale = Locale.ENGLISH;
        }
        getResources().updateConfiguration(config, dm);
    }

    private void initView() {
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        channel_list_view = (ListView) findViewById(R.id.channel_list_view);
//        channel_recycler_view = (RecyclerView) findViewById(R.id.channel_recycler_view);
//        channel_recycler_view.setLayoutManager(new LinearLayoutManager(this));//这里用线性显示 类似于listview
        channel_list_title = (TextView) findViewById(R.id.channel_list_title);
        channel_list = findViewById(R.id.channel_list);
        channel_id = (TextView) findViewById(R.id.channel_id);
    }

    private void initData() {
        type = "Live";
        languageCode = "en-US";
        data = Utils.getStringformAssets(this, "Live_29.json");
        mainPagePrefix = "http://192.168.18.101/openvod/now";

//        type = "Single";
//        languageCode = "en-US";
//        data = "http://192.168.18.101/openvod/now/Video/resource/fengkuangdongwucheng.mp4";
//        mainPagePrefix = "http://192.168.18.101/openvod/now";

//        type = "List";
//        languageCode = "en-US";
//        data = Utils.getStringformAssets(this, "movie_list.json");
//        mainPagePrefix = "http://192.168.18.101/openvod/now";

        if(getIntent().hasExtra("type"))
            type = getIntent().getStringExtra("type");
        if(getIntent().hasExtra("languageCode"))
            languageCode = getIntent().getStringExtra("languageCode");
        if(getIntent().hasExtra("data"))
            data = getIntent().getStringExtra("data");
        if(getIntent().hasExtra("mainPagePrefix"))
            mainPagePrefix = getIntent().getStringExtra("mainPagePrefix");

        setLanguage();

        switch (type){
            case "Live":
                initLivePlayer();
                break;
            case "Single":
                initSinglePlayer();
                break;
            case "List":
                initListPlayer();
                break;
            case "Input":
                initInputPlayer();
                break;
            default:
                initLivePlayer();
                break;
        }

    }

    private void initInputPlayer() {
        simpleExoPlayerView.setUseController(true);

    }

    private void initListPlayer() {
        simpleExoPlayerView.setUseController(true);
        ArrayList<MovieBean> movieBeans = Utils.getBeanListFromJson(data,MovieBean.class);
        MediaSource[] mediaSources = new MediaSource[movieBeans.size()];
        for(int i = 0;i< movieBeans.size();i++){
            mediaSources[i] = buildMediaSource(movieBeans.get(i).getUri(), null);
        }
        MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                : new ConcatenatingMediaSource(mediaSources);
        player.prepare(mediaSource);

    }

    private void initSinglePlayer() {
        simpleExoPlayerView.setUseController(true);
        MediaSource mediaSource = buildMediaSource(Uri.parse(data), null);
        player.prepare(mediaSource);
    }

    private void initLivePlayer() {

        channel_list.setAlpha(1f);
        channel_id.setAlpha(1f);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_CHANNELH, DELAYMILLIS);
        channel_id.setVisibility(View.VISIBLE);
        channel_list.setVisibility(View.VISIBLE);
        simpleExoPlayerView.setUseController(false);

        mChannels = getBeanListFromJson(Utils.getValueByKey(data, "ChannelList"), Channel.class);
        if(mChannels == null || mChannels.size()<1){
            return;
        }
        channel_list_title.setText(String.format(getResources().getString(R.string.channel_list),0,mChannels.size()-1));

//        channel_recycler_view.setAdapter(mAdapter);
//        channel_recycler_view.requestFocus();
        channel_list_view.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mChannels.size();
            }

            @Override
            public Channel getItem(int position) {
                return mChannels.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = View.inflate(PlayerActivity.this, R.layout.channel_item, null);
                    holder = new ViewHolder();
                    holder.tv_channel_name = (TextView) convertView.findViewById(R.id.tv_channel_name);
                    holder.tv_channel_num = (TextView) convertView.findViewById(R.id.tv_channel_num);
                    holder.sdv_channel_pic = (SimpleDraweeView) convertView.findViewById(R.id.sdv_channel_pic);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.sdv_channel_pic.setImageURI(Uri.parse(mChannels.get(position).getChannelPicURLRelatvie()));
                holder.tv_channel_name.setText(mChannels.get(position).getChannelName().get(PlayerActivity.languageCode));
//                holder.tv_channel_num.setText(mChannels.get(position).getChannelNum()+"");
                holder.tv_channel_num.setText(position + "");
                return convertView;
            }

            class ViewHolder {
                public TextView tv_channel_num, tv_channel_name;
                public SimpleDraweeView sdv_channel_pic;
            }
        });
        channel_list_view.requestFocus();
        channel_list_view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                channelID = mChannels.get(position).getChannelNum();
                channelID = "" + position;
                channel_id.setText(channelID);
                channel_list_title.setText(String.format(getResources().getString(R.string.channel_list),position,mChannels.size()-1));
//                play(mChannels.get(position).getSrc());
                play(mChannels.get(position));
                channel_id.setAlpha(1f);
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessageDelayed(MSG_HIDE_CHANNELH, DELAYMILLIS);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        channel_list_view.setSelection(0);
        channel_id.setText("0");
    }

    public void play(Channel channel) {
//        MediaSource mediaSource = new HlsMediaSource(Uri.parse(url), mediaDataSourceFactory, mainHandler, eventLogger);
        if(channel.getMediaSource()==null)
            channel.setMediaSource(buildMediaSource(Uri.parse(channel.getSrc()), null));
        player.prepare(channel.getMediaSource());
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, adaptiveMediaSourceEventListener);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, adaptiveMediaSourceEventListener);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, adaptiveMediaSourceEventListener);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventListener);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private void initializePlayer() {
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl());
        simpleExoPlayerView.setPlayer(player);
        player.setPlayWhenReady(shouldAutoPlay);

        initData();
    }

    private void releasePlayer() {
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            player.release();
            player = null;
            trackSelector = null;
        }
    }


    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((DemoApplication) getApplication())
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

}
