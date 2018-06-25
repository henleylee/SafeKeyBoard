package com.liyunlong.safekeyboard;

import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * @author liyunlong
 * @date 2018/6/15 13:58
 */
public class KeyboardEditTextTouchListener implements View.OnTouchListener {

    private int mKeyboardType;
    private SafeKeyboard mSafeKeyboard;

    public KeyboardEditTextTouchListener(SafeKeyboard safeKeyboard, @KeyboardType int keyboardType) {
        this.mSafeKeyboard = safeKeyboard;
        this.mKeyboardType = keyboardType;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mSafeKeyboard != null && event.getAction() == MotionEvent.ACTION_UP) {
            mSafeKeyboard.setKeyboardType(mKeyboardType);
            if (!mSafeKeyboard.isShowing()) {
                mSafeKeyboard.show((EditText) v);
            } else {
                mSafeKeyboard.setEditText((EditText) v);
            }
        }
        return false;
    }

}
