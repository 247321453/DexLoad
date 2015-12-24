package com.lody.plugin.bean;

/**
 * Created by Administrator on 2015/12/24.
 */
public interface IPlugin {
    public String getPluginPath();
    public boolean canUse();
    public LAPK from();
}
