package com.geopdfviewer.android;

import java.util.Iterator;
import java.util.Stack;

/**
 * 栈迭代器类
 * 用于返回一个复合迭代器，既能返回MapFrame又能返回MapItem
 *
 * @see com.geopdfviewer.android.MapComponent
 * @see com.geopdfviewer.android.MapFrame
 * @see com.geopdfviewer.android.MapItem
 *
 * @author  李正洋
 *
 * @since   1.6
 */
public class CompositeIterator implements Iterator {
    Stack stack = new Stack();

    public CompositeIterator(Iterator iterator) {
        stack.push(iterator);
    }

    @Override
    public boolean hasNext() {
        if (stack.empty())
            return false;
        else
        {
            Iterator iterator = (Iterator) stack.peek();
            if (!iterator.hasNext())
            {
                stack.pop();
                return hasNext();
            }
            else
            {
                return true;
            }
        }
    }

    @Override
    public Object next() {
        if (hasNext())
        {
            Iterator iterator = (Iterator) stack.peek();
            MapComponent component = (MapComponent) iterator.next();
            if (component instanceof MapFrame)
            {
                stack.push(component.createIterator());
            }
            return component;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
