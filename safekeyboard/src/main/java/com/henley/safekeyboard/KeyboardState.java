package com.henley.safekeyboard;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 键盘状态
 *
 * @author Henley
 * @date 2018/6/14 10:14
 */
@IntDef({
        KeyboardState.STATE_SHOW,
        KeyboardState.STATE_HIDE
})
@Retention(RetentionPolicy.SOURCE)
public @interface KeyboardState {

    /**
     * 显示
     */
    int STATE_SHOW = 1;
    /**
     * 隐藏
     */
    int STATE_HIDE = 2;
}
