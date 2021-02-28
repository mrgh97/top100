package com.person;

public class Url {
    private String url;
    private Integer nums;

    public Url(String url, int nums) {
        this.url = url;
        this.nums = nums;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getNums() {
        return nums;
    }

    public void setNums(Integer nums) {
        this.nums = nums;
    }
}
