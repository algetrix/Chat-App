package com.example.lapitchat;

public class Friends {
    public String date;
    public String name;
    public String thumb_image;

    public Friends(String date)
    {
        this.date = date;
        //this.name = name;
        //this.thumb_image = thumb_image;

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
