package main.model;

import lombok.Data;

@Data
public class Content {
    private String id;
    private String viedeoUrl;
    private String picUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getViedeoUrl() {
        return viedeoUrl;
    }

    public void setViedeoUrl(String viedeoUrl) {
        this.viedeoUrl = viedeoUrl;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
