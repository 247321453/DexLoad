package com.lody.plugin.bean;

/**
 * Created by lody  on 2015/4/3.
 */
public class LPluginBug {
    public Throwable error;
    public long errorTime;
    public Thread errorThread;
    public LActivityPlugin errorPlugin;
    public int processId;
}
