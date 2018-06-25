# SafeKeyBoard-master —— Android自定义安全键盘

## 1. 介绍 ##
Android自定义安全键盘用来替代系统键盘，可以指定随机键盘。

## 2. 顺序键盘 ##
#### 2.1 字母键盘： ####
![](/screenshots/keyboard_normal_letter.jpg)
#### 2.2 数字键盘： ####
![](/screenshots/keyboard_normal_number.jpg)
![](/screenshots/keyboard_normal_number_x.jpg)
![](/screenshots/keyboard_normal_number_point.jpg)
![](/screenshots/keyboard_normal_number_abc.jpg)
#### 2.3 字母数字键盘： ####
![](/screenshots/keyboard_normal_letter_number.jpg)
#### 2.4 符号键盘： ####
![](/screenshots/keyboard_normal_symbol.jpg)

## 3. 随机键盘 ##
#### 3.1 字母键盘： ####
![](/screenshots/keyboard_random_letter.jpg)
#### 3.2 数字键盘： ####
![](/screenshots/keyboard_random_number.jpg)
![](/screenshots/keyboard_random_number_x.jpg)
![](/screenshots/keyboard_random_number_point.jpg)
![](/screenshots/keyboard_random_number_abc.jpg)
#### 3.3 字母数字键盘： ####
![](/screenshots/keyboard_random_letter_number.jpg)
#### 3.4 符号键盘： ####
![](/screenshots/keyboard_random_symbol.jpg)

## 4. 使用 ##
```java
        SafeKeyboard safeKeyboard = SafeKeyboard.newBuilder(this)
                .setRandom(false)                               // 设置是否为随机键盘
                .setUpperLetter(false)                          // 设置是否为大写字母(只有默认为字母键盘时才有效)
                .setEditText(editText)                          // 设置EditText
                .setKeyboardView(keyboardView)                  // 设置KeyboardView
                .setKeyboardType(KeyboardType.TYPE_LETTER)      // 设置键盘类型
                .setKeyboardState(KeyboardState.STATE_SHOW)     // 设置键盘状态
                .build();                                       // 构建SafeKeyboard

        editText.setOnTouchListener(new KeyboardEditTextTouchListener(safeKeyboard, KeyboardType.TYPE_LETTER));
```



