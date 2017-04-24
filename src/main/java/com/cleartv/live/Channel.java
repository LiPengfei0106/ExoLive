package com.cleartv.live;

import com.google.android.exoplayer2.source.MediaSource;

import java.util.Map;

/**
 * Created by Lipengfei on 2017/4/18.
 */

public class Channel {
    /**
     * {
     "Src": "http://192.168.18.235/live/CCTV1HD/CCTV1HD.m3u8",
     "ChannelNum": 1,
     "ViewID": 29,
     "ChannelPicURL": "http://mres.cleartv.cn/default/c6cfbb28669a8547a11c244e73a6d371_14848204102.png",
     "ChannelPicURLRelatvie": "/Main/resource/c6cfbb28669a8547a11c244e73a6d371_14848204102.png",
     "SrcBackup": null,
     "ChannelName": {
     "zh-CN": "CCTV1HD",
     "en-US": "CCTV1HD"
     },
     "ID": 49,
     "ChannelPicSize": 2033
     }
     */
    private String Src;
    private int ChannelNum;
    private int ViewID;
    private String ChannelPicURL;
    private String ChannelPicURLRelatvie;
    private String SrcBackup;
    private Map<String ,String> ChannelName;
    private int ID;
    private long ChannelPicSize;

    private MediaSource mediaSource;

    public MediaSource getMediaSource() {
        return mediaSource;
    }

    public void setMediaSource(MediaSource mediaSource) {
        this.mediaSource = mediaSource;
    }

    public String getSrc() {
        return Src;
    }

    public int getChannelNum() {
        return ChannelNum;
    }

    public int getViewID() {
        return ViewID;
    }

    public String getChannelPicURL() {
        return ChannelPicURL;
    }

    public String getChannelPicURLRelatvie() {
        return PlayerActivity.mainPagePrefix + ChannelPicURLRelatvie;
    }

    public String getSrcBackup() {
        return SrcBackup;
    }

    public Map<String, String> getChannelName() {
        return ChannelName;
    }

    public int getID() {
        return ID;
    }

    public long getChannelPicSize() {
        return ChannelPicSize;
    }
}
