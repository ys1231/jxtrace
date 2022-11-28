package com.iyue.jxtrace;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class IyuePackageInfo implements Serializable {

    private String title;
    private String pakageName;
    private Drawable  image;

    public IyuePackageInfo() {
    }

    public IyuePackageInfo(String title, String pakageName, Drawable imageid) {
        this.title = title;
        this.pakageName = pakageName;
        this.image = imageid;
    }

    @NonNull
    @Override
    public String toString() {
        return "title:"+title+" pakageName:"+pakageName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPakageName() {
        return pakageName;
    }

    public void setPakageName(String pakageName) {
        this.pakageName = pakageName;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }
}
