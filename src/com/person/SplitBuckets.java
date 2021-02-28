package com.person;

public class SplitBuckets {
    private int partitionId;
    // 等同url
    private String uid;
    public SplitBuckets(String uid, int partitionId) {
        this.uid = uid;
        this.partitionId = partitionId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

}
