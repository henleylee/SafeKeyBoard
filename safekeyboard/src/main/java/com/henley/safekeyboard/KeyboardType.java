package com.henley.safekeyboard;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 键盘类型
 *
 * @author Henley
 * @date 2018/6/14 10:01
 */
@IntDef({
        KeyboardType.TYPE_NUMBER,
        KeyboardType.TYPE_NUMBER_POINT,
        KeyboardType.TYPE_NUMBER_X,
        KeyboardType.TYPE_NUMBER_ABC,
        KeyboardType.TYPE_LETTER,
        KeyboardType.TYPE_LETTER_NUMBER,
        KeyboardType.TYPE_SYMBOL,
})
@Retention(RetentionPolicy.SOURCE)
public @interface KeyboardType {

    /**
     * 数字(左下角为空)
     */
    int TYPE_NUMBER = 1;
    /**
     * 数字(左下角为点)
     */
    int TYPE_NUMBER_POINT = 2;
    /**
     * 数字(左下角为X)
     */
    int TYPE_NUMBER_X = 3;
    /**
     * 数字(左下角为ABC)
     */
    int TYPE_NUMBER_ABC = 4;
    /**
     * 字母(左下角为123，右下角下角为#+=)
     */
    int TYPE_LETTER = 5;
    /**
     * 字母(上面为数字，左下角为123，右下角下角为#+=)
     */
    int TYPE_LETTER_NUMBER = 6;
    /**
     * 符号(左下角为123，右下角下角为ABC)
     */
    int TYPE_SYMBOL = 7;
}
