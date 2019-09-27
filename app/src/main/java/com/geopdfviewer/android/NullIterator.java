package com.geopdfviewer.android;

import java.util.Iterator;

/**
 * 空迭代器类
 * 用于返回一个空迭代器
 *
 * @author  李正洋
 *
 * @since   1.6
 *
 */
public class NullIterator implements Iterator {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
