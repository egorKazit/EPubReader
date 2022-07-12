package com.yk.common.utils;

import android.view.View;
import android.view.ViewParent;

import java.util.function.BiConsumer;

/**
 * Parent method caller
 */
public class ParentMethodCaller {

    /**
     * It recursively goes to parent until parent does not match @{parentClass}
     *
     * @param view          source view
     * @param parentClass   parent class
     * @param consumer      consumer on match
     * @param consumerValue consumer value
     * @param <T>           type of parent class
     */
    public static <T> void callConsumerOnParent(View view, Class<T> parentClass, BiConsumer<T, Object> consumer, Object consumerValue) {
        ViewParent parentView = view.getParent();
        do {
            if (parentView == null) {
                return;
            }
            parentView = parentView.getParent();

        } while (!(parentView.getClass().isAssignableFrom(parentClass)));
        parentClass.cast(parentView);
        consumer.accept(parentClass.cast(parentView), consumerValue);
    }

}
