package net.mabako.steamgifts.data;

import java.io.Serializable;

/**
 * Created by mabako on 06.02.2016.
 */
public class Image implements Serializable {
    private String url, title;

    public Image(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
