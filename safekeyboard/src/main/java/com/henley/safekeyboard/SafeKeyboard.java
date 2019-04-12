package com.henley.safekeyboard;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 安全键盘
 *
 * @author Henley
 * @date 2018/6/13 18:41
 */
public class SafeKeyboard {

    private static final String TAG = "SafeKeyboard";
    private static final String NUMBERS = "0123456789";
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int ANIM_DURATION = 350;
    private static final int KEYCODE_SPACE = 32;
    private static final int KEYCODE_CLEAR = 10001;
    private static final int KEYCODE_NUMBER = 10002;
    private static final int KEYCODE_LETTER = 10003;
    private static final int KEYCODE_SYMBOL = 10004;

    private static final List<Integer> SPECIAL_KEYS = new ArrayList<Integer>() {
        {
            add(KEYCODE_SPACE);
            add(KEYCODE_CLEAR);
            add(KEYCODE_NUMBER);
            add(KEYCODE_LETTER);
            add(KEYCODE_SYMBOL);
            add(Keyboard.KEYCODE_SHIFT);
            add(Keyboard.KEYCODE_MODE_CHANGE);
            add(Keyboard.KEYCODE_CANCEL);
            add(Keyboard.KEYCODE_DONE);
            add(Keyboard.KEYCODE_DELETE);
            add(Keyboard.KEYCODE_ALT);
        }
    };

    private Context mContext;
    private EditText mEditText;
    private KeyboardView mKeyboardView;
    private LinearLayout mKeyboardContainer;
    @KeyboardType
    private int mKeyboardType;
    @KeyboardState
    private int mKeyboardState;
    @KeyboardType
    private int mSpecialType;
    private boolean isRandom;
    private boolean isUpperLetter;    // 是否大写字母
    private Keyboard mCurKeyboard;
    private InputMethodManager mInputMethodManager;
    private TranslateAnimation mShowAnimation;
    private TranslateAnimation mHideAnimation;
    private OnKeyboardStateChangeListener mStateChangeListener;
    private KeyboardView.OnKeyboardActionListener mKeyboardActionListener;
    private SparseArray<Keyboard> mKeyboards = new SparseArray<>();

    public static Builder newBuilder(Context context) {
        return Builder.with(context);
    }

    private SafeKeyboard(Builder builder) {
        this.mContext = builder.context;
        this.mEditText = builder.editText;
        this.mKeyboardView = builder.keyboardView;
        this.mKeyboardType = builder.keyboardType;
        this.mKeyboardState = builder.keyboardState;
        this.isRandom = builder.isRandom;
        this.isUpperLetter = builder.isUpperLetter;
        this.mStateChangeListener = builder.stateChangeListener;
        this.mKeyboardActionListener = builder.keyboardActionListener;
        this.mSpecialType = mKeyboardType;
        initKeyboardView(); // 初始化键盘
        initKeyboardContainer();
        setKeyboardState(mKeyboardState);
    }

    /**
     * 初始化键盘
     */
    private void initKeyboardView() {
        mKeyboardView.setEnabled(true);
        mKeyboardView.setPreviewEnabled(false); // 关闭键盘按键预览效果，如果按键过小可能会比较适用
        mKeyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener(this)); // 设置键盘事件
        Keyboard keyboard = getKeyboard();
        if (keyboard != null) {
            if (isUpperLetter) {
                if (mKeyboardType == KeyboardType.TYPE_LETTER || mKeyboardType == KeyboardType.TYPE_LETTER_NUMBER) {
                    changeLetterKey(keyboard);
                } else {
                    isUpperLetter = false;
                }
            }
            setKeyboard(keyboard); // 键盘关联Keyboard对象
        }
    }

    /**
     * 初始化键盘容器
     */
    private void initKeyboardContainer() {
        ViewGroup parent = (ViewGroup) mKeyboardView.getParent();
        Drawable background = mKeyboardView.getBackground();
        ViewGroup.LayoutParams params = mKeyboardView.getLayoutParams();
        mKeyboardContainer = new LinearLayout(mContext);
        mKeyboardContainer.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        mKeyboardContainer.setOrientation(LinearLayout.VERTICAL);
        View header = LayoutInflater.from(mContext).inflate(R.layout.layout_keycoard_header, null);
        header.findViewById(R.id.keyboard_header_complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });
        ViewCompat.setBackground(header, background);
        mKeyboardContainer.addView(header);
        if (parent != null) {
            int viewIndex = 0;
            int count = parent.getChildCount();
            for (int index = 0; index < count; index++) { // 遍历显示数据的View的父View的Child
                if (mKeyboardView == parent.getChildAt(index)) {
                    viewIndex = index; // 获取要显示的View在父View中的位置
                    break;
                }
            }
            parent.removeViewAt(viewIndex);
            mKeyboardContainer.addView(mKeyboardView);
            parent.addView(mKeyboardContainer, viewIndex, params);
        }
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardContainer.setVisibility(View.GONE);
    }

    public EditText getEditText() {
        return mEditText;
    }

    public KeyboardView getKeyboardView() {
        return mKeyboardView;
    }

    public int getKeyboardType() {
        return mKeyboardType;
    }

    public Keyboard getCurKeyboard() {
        return mCurKeyboard;
    }

    public void setEditText(EditText editText) {
        if (this.mEditText == editText) {
            return;
        }
        this.mEditText = requireNonNull(editText, "The editText cannot be null.");
        hideSystemKeyBoard();
        setShowSoftInputOnFocus();
    }

    /**
     * 设置键盘状态
     *
     * @see KeyboardType
     */
    public void setKeyboardType(@KeyboardType int keyboardType) {
        if (this.mKeyboardType == keyboardType) {
            return;
        }
        this.mKeyboardType = keyboardType;
        this.mSpecialType = mKeyboardType;
        Keyboard keyboard = getKeyboard();
        if (keyboard != null) {
            setKeyboard(keyboard); // 键盘关联Keyboard对象
        }

    }

    /**
     * 设置键盘类型
     *
     * @see KeyboardState
     */
    public void setKeyboardState(@KeyboardState int keyboardState) {
        this.mKeyboardState = keyboardState;
        if (mKeyboardState == KeyboardState.STATE_SHOW) {
            showKeyboard();
        } else if (mKeyboardState == KeyboardState.STATE_HIDE) {
            hideKeyboard();
        }
    }

    /**
     * 键盘关联{@link Keyboard}对象
     */
    public void setKeyboard(@NonNull Keyboard keyboard) {
        this.mCurKeyboard = requireNonNull(keyboard, "The keyboard cannot be null.");
        if (isRandom) {
            randomByKeyboardType();
        }
        mKeyboardView.setKeyboard(mCurKeyboard);
    }

    /**
     * 判断键盘是否正在显示
     */
    public boolean isShowing() {
        return mKeyboardState == KeyboardState.STATE_SHOW;
    }

    /**
     * 显示键盘
     */
    public void show() {
        show(mEditText);
    }

    /**
     * 显示键盘
     */
    public void show(EditText editText) {
        if (mEditText == editText && this.mKeyboardState == KeyboardState.STATE_SHOW) {
            return;
        }
        this.mEditText = requireNonNull(editText, "The editText cannot be null.");
        showKeyboard();
    }

    /**
     * 隐藏键盘
     */
    public void hide() {
        if (this.mKeyboardState == KeyboardState.STATE_HIDE) {
            return;
        }
        hideKeyboard();
    }

    /**
     * 显示键盘
     */
    private void showKeyboard() {
        this.mKeyboardState = KeyboardState.STATE_SHOW;
        hideSystemKeyBoard();
        setShowSoftInputOnFocus();
        if (mShowAnimation == null) {
            mShowAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0.0f
            );
            mShowAnimation.setDuration(ANIM_DURATION);
            mShowAnimation.setAnimationListener(new SimpleAnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mKeyboardView.setVisibility(View.VISIBLE);
                    mKeyboardContainer.setVisibility(View.VISIBLE);
                }
            });
        }
        mKeyboardContainer.startAnimation(mShowAnimation);
        if (mStateChangeListener != null) {
            mStateChangeListener.onboardStateChange(mKeyboardState, mEditText);
        }
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        this.mKeyboardState = KeyboardState.STATE_HIDE;
        hideSystemKeyBoard();
        if (mHideAnimation == null) {
            mHideAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 1.0f
            );
            mHideAnimation.setDuration(ANIM_DURATION);
            mHideAnimation.setAnimationListener(new SimpleAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    mKeyboardView.setVisibility(View.GONE);
                    mKeyboardContainer.setVisibility(View.GONE);
                }
            });
        }
        mKeyboardContainer.startAnimation(mHideAnimation);
        if (mStateChangeListener != null) {
            mStateChangeListener.onboardStateChange(mKeyboardState, mEditText);
        }
    }

    private void hideSystemKeyBoard() {
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (mInputMethodManager != null) {
            mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

    private void setShowSoftInputOnFocus() {
        try {
            Class<EditText> clazz = EditText.class;
            Method method = clazz.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(mEditText, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Keyboard getKeyboard() {
        Keyboard keyboard = mKeyboards.get(mKeyboardType);
        if (keyboard != null) {
            return keyboard;
        }
        switch (mKeyboardType) {
            case KeyboardType.TYPE_NUMBER: // 数字(左下角为空)
                keyboard = new Keyboard(mContext, R.xml.keyboard_number);
                break;
            case KeyboardType.TYPE_NUMBER_POINT: // 数字(左下角为点)
                keyboard = new Keyboard(mContext, R.xml.keyboard_number_point);
                break;
            case KeyboardType.TYPE_NUMBER_X: // 数字(左下角为X)
                keyboard = new Keyboard(mContext, R.xml.keyboard_number_x);
                break;
            case KeyboardType.TYPE_NUMBER_ABC: // 数字(左下角为ABC)
                keyboard = new Keyboard(mContext, R.xml.keyboard_number_abc);
                break;
            case KeyboardType.TYPE_LETTER: // 字母(左下角为123，右下角下角为#+=)
                keyboard = new Keyboard(mContext, R.xml.keyboard_letter);
                break;
            case KeyboardType.TYPE_LETTER_NUMBER: // 字母(上面为数字，左下角为123，右下角下角为#+=)
                keyboard = new Keyboard(mContext, R.xml.keyboard_letter_number);
                break;
            case KeyboardType.TYPE_SYMBOL: // 符号(左下角为123，右下角下角为ABC)
                keyboard = new Keyboard(mContext, R.xml.keyboard_symbol);
                break;
            default: // 不识别的键盘类型
                break;
        }
        if (keyboard != null) {
            mKeyboards.put(mKeyboardType, keyboard);
        }
        return keyboard;
    }

    /**
     * 字母键盘大小写切换
     */
    private void changeLetterKey(Keyboard keyboard) {
        List<Keyboard.Key> keyList = keyboard.getKeys();
        for (Keyboard.Key key : keyList) {
            if (key.label != null && isLetter(key.label.toString())) {
                // LowerCase->a~z:97~122; UpperCase->A~Z:65~90
                if (isUpperLetter) { // 小写切换为大写
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                } else { // 大写切换为小写
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
            } else if (key.codes[0] == Keyboard.KEYCODE_SHIFT) {
                if (isUpperLetter) { // 小写切换为大写
                    key.icon = ContextCompat.getDrawable(mContext, R.drawable.icon_keyboard_shift_uppercase);
                } else { // 大写切换为小写
                    key.icon = ContextCompat.getDrawable(mContext, R.drawable.icon_keyboard_shift_lowercase);
                }
            }
        }
    }

    private void randomByKeyboardType() {
        if (mKeyboardType == KeyboardType.TYPE_NUMBER
                || mKeyboardType == KeyboardType.TYPE_NUMBER_X
                || mKeyboardType == KeyboardType.TYPE_NUMBER_ABC
                || mKeyboardType == KeyboardType.TYPE_NUMBER_POINT) {
            randomKeyboard(RandomType.TYPE_NUMBER);
        } else if (mKeyboardType == KeyboardType.TYPE_LETTER) {
            randomKeyboard(RandomType.TYPE_LETTER);
        } else if (mKeyboardType == KeyboardType.TYPE_LETTER_NUMBER) {
            randomKeyboard(RandomType.TYPE_NUMBER);
            randomKeyboard(RandomType.TYPE_LETTER);
        } else if (mKeyboardType == KeyboardType.TYPE_SYMBOL) {
            randomKeyboard(RandomType.TYPE_SYMBOL);
        }
    }

    /**
     * 随机键盘按键
     */
    private void randomKeyboard(@RandomType int randomType) {
        List<Keyboard.Key> keyList = mCurKeyboard.getKeys();
        List<Integer> indexList = new ArrayList<>();
        List<KeyModel> randomList = new ArrayList<>();
        int size = keyList.size();
        for (int index = 0; index < size; index++) {
            Keyboard.Key key = keyList.get(index);
            if (key.label == null || isSpecialKey(key.codes[0])) {
                continue;
            }
            if (randomType == RandomType.TYPE_NUMBER && !isNumber(key.label.toString())) {
                continue;
            } else if (randomType == RandomType.TYPE_LETTER && !isLetter(key.label.toString())) {
                continue;
            }
            indexList.add(index);
            randomList.add(new KeyModel(key));
        }
        Collections.shuffle(randomList);
        int indexSize = indexList.size();
        for (int index = 0; index < indexSize; index++) {
            Integer keyIndex = indexList.get(index);
            KeyModel keyModel = randomList.get(index);
            Keyboard.Key key = keyList.get(keyIndex);
            keyModel.resetKey(key);
        }
        indexList.clear();
        randomList.clear();
    }

    /**
     * 判断是否是字母数字
     */
    private boolean isNumber(String number) {
        return NUMBERS.contains(number);
    }

    /**
     * 判断是否是字母
     */
    private boolean isLetter(String letter) {
        return LETTERS.contains(letter.toUpperCase());
    }

    /**
     * 判断是否是特殊键
     */
    private boolean isSpecialKey(int keyCode) {
        return SPECIAL_KEYS.contains(keyCode);
    }

    private static <T> T requireNonNull(T object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
        return object;
    }

    public static class Builder {


        private Context context;
        private EditText editText;
        private KeyboardView keyboardView;
        @KeyboardState
        private int keyboardState = KeyboardState.STATE_HIDE;
        @KeyboardType
        private int keyboardType = KeyboardType.TYPE_NUMBER;
        private boolean isRandom = false;
        private boolean isUpperLetter = false;    // 是否大写
        private OnKeyboardStateChangeListener stateChangeListener;
        private KeyboardView.OnKeyboardActionListener keyboardActionListener;

        public static Builder with(Context context) {
            return new Builder(context);
        }

        private Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置{@link EditText}
         */
        public Builder setEditText(EditText editText) {
            this.editText = requireNonNull(editText, "The editText cannot be null.");
            return this;
        }

        /**
         * 设置{@link KeyboardView}
         */
        public Builder setKeyboardView(KeyboardView keyboardView) {
            this.keyboardView = requireNonNull(keyboardView, "The keyboardView cannot be null.");
            return this;
        }

        /**
         * 设置键盘类型
         *
         * @see KeyboardType
         */
        public Builder setKeyboardType(@KeyboardType int keyboardType) {
            this.keyboardType = keyboardType;
            return this;
        }

        /**
         * 设置键盘状态
         *
         * @see KeyboardState
         */
        public Builder setKeyboardState(@KeyboardState int keyboardState) {
            this.keyboardState = keyboardState;
            return this;
        }

        /**
         * 设置是否为随机键盘
         */
        public Builder setRandom(boolean isRandom) {
            this.isRandom = isRandom;
            return this;
        }

        /**
         * 设置是否为大写字母(只有默认为字母键盘时才有效)
         *
         * @see KeyboardType
         */
        public Builder setUpperLetter(boolean isUpperLetter) {
            this.isUpperLetter = isUpperLetter;
            return this;
        }

        /**
         * 设置键盘状态改变监听
         */
        public Builder setOnKeyboardStateChangeListener(OnKeyboardStateChangeListener listener) {
            this.stateChangeListener = listener;
            return this;
        }

        /**
         * 设置键盘事件监听
         */
        public Builder setOnKeyboardActionListener(KeyboardView.OnKeyboardActionListener listener) {
            this.keyboardActionListener = listener;
            return this;
        }

        /**
         * 构建SafeKeyboard
         */
        public SafeKeyboard build() {
            if (context == null) {
                throw new IllegalArgumentException("The context is null.");
            }
            if (editText == null) {
                throw new IllegalArgumentException("The editText is null.");
            }
            if (keyboardView == null) {
                throw new IllegalArgumentException("The keyboardView is null.");
            }
            return new SafeKeyboard(this);
        }

    }

    private static class KeyModel {

        private int[] codes;
        private CharSequence label;
        private Drawable icon;
        private Drawable iconPreview;

        KeyModel(Keyboard.Key key) {
            this.codes = key.codes;
            this.label = key.label;
            this.icon = key.icon;
            this.iconPreview = key.iconPreview;
        }

        void resetKey(Keyboard.Key key) {
            key.codes = this.codes;
            key.label = this.label;
            key.icon = this.icon;
            key.iconPreview = this.iconPreview;
        }

    }

    private static class OnKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

        private WeakReference<SafeKeyboard> reference;

        OnKeyboardActionListener(SafeKeyboard safeKeyboard) {
            this.reference = new WeakReference<>(safeKeyboard);
        }

        @Override
        public void onPress(int primaryCode) {
            SafeKeyboard safeKeyboard = reference.get();
            if (safeKeyboard == null || safeKeyboard.mKeyboardView == null) {
                return;
            }
            safeKeyboard.mKeyboardView.setPreviewEnabled(false);
            if (safeKeyboard.mKeyboardActionListener != null) {
                safeKeyboard.mKeyboardActionListener.onPress(primaryCode);
            }
        }

        @Override
        public void onRelease(int primaryCode) {
            SafeKeyboard safeKeyboard = reference.get();
            if (safeKeyboard == null || safeKeyboard.mKeyboardView == null) {
                return;
            }
            if (safeKeyboard.mKeyboardActionListener != null) {
                safeKeyboard.mKeyboardActionListener.onRelease(primaryCode);
            }
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Log.i(TAG, "onKey--" + primaryCode);
            SafeKeyboard safeKeyboard = reference.get();
            if (safeKeyboard == null || safeKeyboard.mEditText == null) {
                return;
            }
            Keyboard keyboard = null;
            Editable editable = safeKeyboard.mEditText.getText();
            int start = safeKeyboard.mEditText.getSelectionStart();
            if (primaryCode == Keyboard.KEYCODE_CANCEL) { // 收起
                safeKeyboard.hideKeyboard();
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) { // 回退
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {// 大小写切换
                keyboard = safeKeyboard.mCurKeyboard;
                safeKeyboard.isUpperLetter = !safeKeyboard.isUpperLetter; // 大小写切换
                safeKeyboard.changeLetterKey(keyboard);
            } else if (primaryCode == Keyboard.KEYCODE_DONE) {// 完成 and 下一个
//                if (mKeyboardView.getRightType() == 4) {
//                    hideKeyboardLayout();
//                    if (inputOver != null)
//                        inputOver.inputHasOver(mKeyboardView.getRightType(), mEditText);
//                } else if (mKeyboardView.getRightType() == 5) {
//                    // 下一个监听
//                    if (inputOver != null)
//                        inputOver.inputHasOver(mKeyboardView.getRightType(), mEditText);
//                }
            } else if (primaryCode == KEYCODE_CLEAR) { // 清除键
                if (editable != null && editable.length() > 0) {
                    editable.clear();
                }
            } else if (primaryCode == KEYCODE_NUMBER) { // 转换为数字键盘
                if (safeKeyboard.mSpecialType == KeyboardType.TYPE_NUMBER) {
                    safeKeyboard.mKeyboardType = KeyboardType.TYPE_NUMBER;
                } else if (safeKeyboard.mSpecialType == KeyboardType.TYPE_NUMBER_POINT) {
                    safeKeyboard.mKeyboardType = KeyboardType.TYPE_NUMBER_POINT;
                } else if (safeKeyboard.mSpecialType == KeyboardType.TYPE_NUMBER_X) {
                    safeKeyboard.mKeyboardType = KeyboardType.TYPE_NUMBER_X;
                } else {
                    safeKeyboard.mKeyboardType = KeyboardType.TYPE_NUMBER_ABC;
                }
                keyboard = safeKeyboard.getKeyboard();
            } else if (primaryCode == KEYCODE_LETTER) { // 转换为字母键盘
                if (safeKeyboard.mSpecialType == KeyboardType.TYPE_LETTER_NUMBER) {
                    safeKeyboard.mKeyboardType = KeyboardType.TYPE_LETTER_NUMBER;
                } else {
                    safeKeyboard.mKeyboardType = KeyboardType.TYPE_LETTER;
                }
                keyboard = safeKeyboard.getKeyboard();
                if (safeKeyboard.isUpperLetter) { // 如果是大写，则转换为小写
                    safeKeyboard.isUpperLetter = false; // 大小写切换
                    safeKeyboard.changeLetterKey(keyboard);
                }
            } else if (primaryCode == KEYCODE_SYMBOL) { // 转换为字符键盘
                safeKeyboard.mKeyboardType = KeyboardType.TYPE_SYMBOL;
                keyboard = safeKeyboard.getKeyboard();
            } else {
                editable.insert(start, Character.toString((char) primaryCode));
            }
            if (keyboard != null) {
                safeKeyboard.setKeyboard(keyboard); // 键盘关联Keyboard对象
            }
            if (safeKeyboard.mKeyboardActionListener != null) {
                safeKeyboard.mKeyboardActionListener.onKey(primaryCode, keyCodes);
            }
        }

        @Override
        public void onText(CharSequence text) {
            SafeKeyboard safeKeyboard = reference.get();
            if (safeKeyboard == null || safeKeyboard.mKeyboardView == null) {
                return;
            }
            if (safeKeyboard.mKeyboardActionListener != null) {
                safeKeyboard.mKeyboardActionListener.onText(text);
            }
        }

        @Override
        public void swipeLeft() {
            SafeKeyboard safeKeyboard = reference.get();
            if (safeKeyboard == null || safeKeyboard.mKeyboardView == null) {
                return;
            }
            if (safeKeyboard.mKeyboardActionListener != null) {
                safeKeyboard.mKeyboardActionListener.swipeLeft();
            }
        }

        @Override
        public void swipeRight() {
            SafeKeyboard safeKeyboard = reference.get();
            if (safeKeyboard == null || safeKeyboard.mKeyboardView == null) {
                return;
            }
            if (safeKeyboard.mKeyboardActionListener != null) {
                safeKeyboard.mKeyboardActionListener.swipeRight();
            }
        }

        @Override
        public void swipeDown() {
            SafeKeyboard safeKeyboard = reference.get();
            if (safeKeyboard == null || safeKeyboard.mKeyboardView == null) {
                return;
            }
            if (safeKeyboard.mKeyboardActionListener != null) {
                safeKeyboard.mKeyboardActionListener.swipeDown();
            }
        }

        @Override
        public void swipeUp() {
            SafeKeyboard safeKeyboard = reference.get();
            if (safeKeyboard == null || safeKeyboard.mKeyboardView == null) {
                return;
            }
            if (safeKeyboard.mKeyboardActionListener != null) {
                safeKeyboard.mKeyboardActionListener.swipeUp();
            }
        }
    }

    private static class SimpleAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}
