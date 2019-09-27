package com.geopdfviewer.android;

/**
 * 键值对类
 * 用于存储键值对
 *
 * 现在可用hashmap，map等集合类进行存储
 *
 *
 * @author 李正洋
 *
 * @since   1.4
 */
public class KeyAndValue {
    private String key;
    private String value;

    public KeyAndValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
