package com.geopdfviewer.android;

/**
 * 按钮命令接口
 *
 * @author  李正洋
 *
 * @since   1.6
 */
public interface BtCommand {
    public void on();
    public void process();
    public void off();
}
