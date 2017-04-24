package com.cleartv.live;

import android.net.Uri;

/**
 * Created by Lipengfei on 2017/4/21.
 */

public class MovieBean {

    private String name;
    private String uri;

    public Uri getUri(){
        return Uri.parse(uri);
    }

    public String getName() {
        return name;
    }
}
