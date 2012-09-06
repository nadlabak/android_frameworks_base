/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.text.method;

import android.os.SystemProperties;
import android.text.*;
import android.text.method.TextKeyListener.Capitalize;
import android.util.SparseArray;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;

import java.lang.Character;
import java.lang.System;
import java.util.Locale;

/**
 * This is the standard key listener for alphabetic input on qwerty
 * keyboards.  You should generally not need to instantiate this yourself;
 * TextKeyListener will do it for you.
 */
public class QwertyKeyListener extends BaseKeyListener {

    private static int mLastKey = -1;
    private static int mCharToReplace = -1;
    private static int mRepeatCount = 0;
    private static boolean mAlreadyReplaced = false;
    private static long mLastPressTime = 0;
    private static long multiPressTimeout = 500;
    private static String mLang;

    private static QwertyKeyListener[] sInstance =
        new QwertyKeyListener[Capitalize.values().length * 2];

    public QwertyKeyListener(Capitalize cap, boolean autotext) {
        mAutoCap = cap;
        mAutoText = autotext;
    }

    /**
     * Returns a new or existing instance with the specified capitalization
     * and correction properties.
     */
    public static QwertyKeyListener getInstance(boolean autotext,
                                              Capitalize cap) {
        int off = cap.ordinal() * 2 + (autotext ? 1 : 0);

        if (sInstance[off] == null) {
            sInstance[off] = new QwertyKeyListener(cap, autotext);
        }
        mLang = SystemProperties.get("persist.sys.keypad_multipress_l", "auto");
        if (mLang.equals("auto")) {
            mLang = Locale.getDefault().getLanguage();
        }
        updateMPSets();
        updateCyrillicMPSets();
        return sInstance[off];
    }
    private static SparseArray<String> MP_SETS = new SparseArray<String>();

    private static void updateCyrillicMPSets() {
        String currentKeycharmap = SystemProperties.get("hw.keyboards.0.devname", "0");
        MP_SETS.delete('\u002e');
        if (currentKeycharmap.contains("russian2")) {
            MP_SETS.delete('\u0444');
            MP_SETS.delete('\u0424');
            MP_SETS.delete('\u0437');
            MP_SETS.delete('\u0417');
            MP_SETS.delete('\u0446');
            MP_SETS.delete('\u0426');
            MP_SETS.delete('\u0448');
            MP_SETS.delete('\u0428');
            MP_SETS.delete('\u0430');
            MP_SETS.delete('\u0410');
            MP_SETS.delete('\u0435');
            MP_SETS.delete('\u0415');
            MP_SETS.delete('\u041e');
            MP_SETS.delete('\u043e');
            MP_SETS.delete('\u044c');
            MP_SETS.delete('\u042c');
            MP_SETS.delete('\u043f');
            MP_SETS.delete('\u041f');
            MP_SETS.put('\u0439', "\u0446");
            MP_SETS.put('\u0419', "\u0426");
            MP_SETS.put('\u044f', "\u0447");
            MP_SETS.put('\u042f', "\u0427");
            MP_SETS.put('\u0445', "\u044a");
            MP_SETS.put('\u0425', "\u042a");
            MP_SETS.put('\u002e', "\u002c");
            MP_SETS.put('\u044e', "\u003f");
            MP_SETS.put('\u042e', "\u003f");
            MP_SETS.put('\u0436', "\u044d");
            MP_SETS.put('\u0416', "\u042d");
        } else if (currentKeycharmap.contains("russian")) {
            MP_SETS.delete('\u0436');
            MP_SETS.delete('\u0416');
            MP_SETS.delete('\u0446');
            MP_SETS.delete('\u0426');
            MP_SETS.delete('\u0448');
            MP_SETS.delete('\u0428');
            MP_SETS.delete('\u0430');
            MP_SETS.delete('\u0410');
            MP_SETS.delete('\u0435');
            MP_SETS.delete('\u0415');
            MP_SETS.delete('\u041e');
            MP_SETS.delete('\u043e');
            MP_SETS.delete('\u044c');
            MP_SETS.delete('\u042c');
            MP_SETS.delete('\u043f');
            MP_SETS.delete('\u041f');
            MP_SETS.put('\u0439', "\u0446");
            MP_SETS.put('\u0419', "\u0426");
            MP_SETS.put('\u0444', "\u044b");
            MP_SETS.put('\u0424', "\u042b");
            MP_SETS.put('\u044f', "\u0447");
            MP_SETS.put('\u042f', "\u0427");
            MP_SETS.put('\u0445', "\u044a");
            MP_SETS.put('\u0425', "\u042a");
            MP_SETS.put('\u0437', "\u003f");
            MP_SETS.put('\u0417', "\u003f");
            MP_SETS.put('\u044e', "\u002c");
            MP_SETS.put('\u042e', "\u002c");
        } else if (currentKeycharmap.contains("cyrillic")) {
            MP_SETS.delete('\u0436');
            MP_SETS.delete('\u0416');
            MP_SETS.delete('\u0444');
            MP_SETS.delete('\u0424');
            MP_SETS.delete('\u0439');
            MP_SETS.delete('\u0419');
            MP_SETS.delete('\u044f');
            MP_SETS.delete('\u042f');
            MP_SETS.delete('\u0445');
            MP_SETS.delete('\u0425');
            MP_SETS.delete('\u044e');
            MP_SETS.delete('\u042e');
            MP_SETS.delete('\u043f');
            MP_SETS.delete('\u041f');
            MP_SETS.put('\u0446', "\u0447");
            MP_SETS.put('\u0426', "\u0427");
            MP_SETS.put('\u0448', "\u0449");
            MP_SETS.put('\u0428', "\u0429");
            MP_SETS.put('\u0430', "\u044f");
            MP_SETS.put('\u0410', "\u042f");
            MP_SETS.put('\u0435', "\u044d");
            MP_SETS.put('\u0415', "\u042d");
            MP_SETS.put('\u0437', "\u0436");
            MP_SETS.put('\u0417', "\u0416");
            MP_SETS.put('\u041e', "\u0401");
            MP_SETS.put('\u043e', "\u0451");
            MP_SETS.put('\u044c', "\u044a");
            MP_SETS.put('\u042c', "\u042a");
        } else if (currentKeycharmap.contains("bulgarian")) {
            MP_SETS.delete('\u0437');
            MP_SETS.delete('\u0417');
            MP_SETS.delete('\u0448');
            MP_SETS.delete('\u0428');
            MP_SETS.delete('\u0430');
            MP_SETS.delete('\u0410');
            MP_SETS.delete('\u0435');
            MP_SETS.delete('\u0415');
            MP_SETS.delete('\u0436');
            MP_SETS.delete('\u0416');
            MP_SETS.delete('\u0444');
            MP_SETS.delete('\u0424');
            MP_SETS.delete('\u0439');
            MP_SETS.delete('\u0419');
            MP_SETS.delete('\u0445');
            MP_SETS.delete('\u0425');
            MP_SETS.delete('\u044e');
            MP_SETS.delete('\u042e');
            MP_SETS.put('\u044c', "\u045d"); //ь-ѝ (x)
            MP_SETS.put('\u042c', "\u040d"); //Ь-Ѝ (X)
            MP_SETS.put('\u044f', "\u044e"); //я-ю (q)
            MP_SETS.put('\u042f', "\u042e"); //Я-Ю (Q)
            MP_SETS.put('\u0446', "\u0447"); //ц-ч (c)
            MP_SETS.put('\u0426', "\u0427"); //Ц-Ч (C)
            MP_SETS.put('\u043e', "\u0448"); //о-ш (o)
            MP_SETS.put('\u041e', "\u0428"); //О-Ш (O)
            MP_SETS.put('\u043f', "\u0449"); //п-щ (p)
            MP_SETS.put('\u041f', "\u0429"); //П-Щ (P)
        }
    }

    private static void updateMPSets() {
        MP_SETS.clear();
        if (mLang.equals("cs")) {
            MP_SETS.put('A', "\u00C1");
            MP_SETS.put('C', "\u010C");
            MP_SETS.put('D', "\u010E");
            MP_SETS.put('E', "\u00C9\u011A");
            MP_SETS.put('I', "\u00CD");
            MP_SETS.put('N', "\u0147");
            MP_SETS.put('O', "\u00D3");
            MP_SETS.put('R', "\u0158");
            MP_SETS.put('S', "\u0160");
            MP_SETS.put('T', "\u0164");
            MP_SETS.put('U', "\u016E\u00DA");
            MP_SETS.put('Y', "\u00DD");
            MP_SETS.put('Z', "\u017D");
            MP_SETS.put('a', "\u00E1");
            MP_SETS.put('c', "\u010D");
            MP_SETS.put('d', "\u010F");
            MP_SETS.put('e', "\u00E9\u011B");
            MP_SETS.put('i', "\u00ED");
            MP_SETS.put('n', "\u0148");
            MP_SETS.put('o', "\u00F3");
            MP_SETS.put('r', "\u0159");
            MP_SETS.put('s', "\u0161");
            MP_SETS.put('t', "\u0165");
            MP_SETS.put('u', "\u016F\u00FA");
            MP_SETS.put('y', "\u00FD");
            MP_SETS.put('z', "\u017E");
        } else if (mLang.equals("da")) {
            MP_SETS.put('A', "\u00C5\u00C6");
            MP_SETS.put('E', "\u00C9");
            MP_SETS.put('O', "\u00D8");
            MP_SETS.put('a', "\u00E5\u00E6");
            MP_SETS.put('e', "\u00E9");
            MP_SETS.put('o', "\u00F8");
        } else if (mLang.equals("de")) {
            MP_SETS.put('A', "\u00C4");
            MP_SETS.put('O', "\u00D6");
            MP_SETS.put('U', "\u00DC");
            MP_SETS.put('a', "\u00E4");
            MP_SETS.put('o', "\u00F6");
            MP_SETS.put('s', "\u00DF");
            MP_SETS.put('u', "\u00FC");
        } else if (mLang.equals("de2")) {
            MP_SETS.put('A', "\u00C4");
            MP_SETS.put('O', "\u00D6");
            MP_SETS.put('U', "\u00DC");
            MP_SETS.put('a', "\u00E4");
            MP_SETS.put('o', "\u00F6");
            MP_SETS.put('b', "\u00DF");
            MP_SETS.put('u', "\u00FC");
        } else if (mLang.equals("es")) {
            MP_SETS.put('A', "\u00C1");
            MP_SETS.put('E', "\u00C9");
            MP_SETS.put('I', "\u00CD");
            MP_SETS.put('N', "\u00D1");
            MP_SETS.put('O', "\u00D3");
            MP_SETS.put('U', "\u00DA\u00DC");
            MP_SETS.put('a', "\u00E1");
            MP_SETS.put('e', "\u00E9");
            MP_SETS.put('i', "\u00ED");
            MP_SETS.put('n', "\u00F1");
            MP_SETS.put('o', "\u00F3");
            MP_SETS.put('u', "\u00FA\u00FC");
            MP_SETS.put('?', "\u00bf");
            MP_SETS.put('!', "\u00a1");
        } else if (mLang.equals("fi")) {
            MP_SETS.put('A', "\u00C4\u00C5");
            MP_SETS.put('O', "\u00D6");
            MP_SETS.put('a', "\u00E4\u00E5");
            MP_SETS.put('o', "\u00F6");
        } else if (mLang.equals("fr")) {
            MP_SETS.put('A', "\u00C0\u00C2\u00C6");
            MP_SETS.put('C', "\u00C7");
            MP_SETS.put('E', "\u00C9\u00C8\u00CA\u00CB");
            MP_SETS.put('I', "\u00CF\u00CE");
            MP_SETS.put('O', "\u00D4\u0152");
            MP_SETS.put('U', "\u00D9\u00DB");
            MP_SETS.put('Y', "\u0178");
            MP_SETS.put('a', "\u00E0\u00E2\u00E6");
            MP_SETS.put('c', "\u00E7");
            MP_SETS.put('e', "\u00E9\u00E8\u00EA\u00EB");
            MP_SETS.put('i', "\u00EF\u00EE");
            MP_SETS.put('o', "\u00F4\u0153");
            MP_SETS.put('u', "\u00F9\u00FB");
            MP_SETS.put('y', "\u00FF");
        } else if (mLang.equals("hu")) {
            MP_SETS.put('A', "\u00C1");
            MP_SETS.put('E', "\u00C9");
            MP_SETS.put('I', "\u00CD");
            MP_SETS.put('O', "\u00D6\u00D3\u0150");
            MP_SETS.put('U', "\u00DC\u00DA\u0170");
            MP_SETS.put('a', "\u00E1");
            MP_SETS.put('e', "\u00E9");
            MP_SETS.put('i', "\u00ED");
            MP_SETS.put('o', "\u00F6\u00F3\u0151");
            MP_SETS.put('u', "\u00FC\u00FA\u0171");
        } else if (mLang.equals("it")) {
            MP_SETS.put('A', "\u00C0");
            MP_SETS.put('E', "\u00C8\u00C9");
            MP_SETS.put('I', "\u00CC");
            MP_SETS.put('O', "\u00D2\u00D3");
            MP_SETS.put('U', "\u00D9");
            MP_SETS.put('a', "\u00E0");
            MP_SETS.put('e', "\u00E8\u00E9");
            MP_SETS.put('i', "\u00EC");
            MP_SETS.put('o', "\u00F2\u00F3");
            MP_SETS.put('u', "\u00F9");
        } else if (mLang.equals("lt")) {
            MP_SETS.put('A', "\u0104");
            MP_SETS.put('C', "\u010C");
            MP_SETS.put('E', "\u0118\u0116");
            MP_SETS.put('I', "\u012E");
            MP_SETS.put('S', "\u0160");
            MP_SETS.put('U', "\u0172\u016A");
            MP_SETS.put('Z', "\u017D");
            MP_SETS.put('a', "\u0105");
            MP_SETS.put('c', "\u010D");
            MP_SETS.put('e', "\u0119\u0117");
            MP_SETS.put('i', "\u012F");
            MP_SETS.put('s', "\u0161");
            MP_SETS.put('u', "\u0173\u016B");
            MP_SETS.put('z', "\u017E");
        } else if (mLang.equals("nl")) {
            MP_SETS.put('E', "\u00C9\u00CB");
            MP_SETS.put('I', "\u00CF");
            MP_SETS.put('O', "\u00D3\u00D6");
            MP_SETS.put('U', "\u00DC");
            MP_SETS.put('e', "\u00E9\u00EB");
            MP_SETS.put('i', "\u00EF");
            MP_SETS.put('o', "\u00F3\u00F6");
            MP_SETS.put('u', "\u00FC");
        } else if (mLang.equals("no")) {
            MP_SETS.put('A', "\u00C5\u00C6\u00C2");
            MP_SETS.put('E', "\u00C9\u00C8\u00CA");
            MP_SETS.put('O', "\u00D8\u00D3\u00D2\u00D4");
            MP_SETS.put('a', "\u00E5\u00E6\u00E2");
            MP_SETS.put('e', "\u00E9\u00E8\u00EA\u00EB");
            MP_SETS.put('o', "\u00F8\u00F3\u00F2\u00F4");
        } else if (mLang.equals("pl")) {
            MP_SETS.put('A', "\u0104");
            MP_SETS.put('C', "\u0106");
            MP_SETS.put('E', "\u0118");
            MP_SETS.put('L', "\u0141");
            MP_SETS.put('N', "\u0143");
            MP_SETS.put('O', "\u00D3");
            MP_SETS.put('S', "\u015A");
            MP_SETS.put('X', "\u0179");
            MP_SETS.put('Z', "\u017B\u0179");
            MP_SETS.put('a', "\u0105");
            MP_SETS.put('c', "\u0107");
            MP_SETS.put('e', "\u0119");
            MP_SETS.put('l', "\u0142");
            MP_SETS.put('n', "\u0144");
            MP_SETS.put('o', "\u00F3");
            MP_SETS.put('s', "\u015B");
            MP_SETS.put('x', "\u017A");
            MP_SETS.put('z', "\u017C\u017A");
        } else if (mLang.equals("pt")) {
            MP_SETS.put('A', "\u00C3\u00C1\u00C2\u00C0");
            MP_SETS.put('C', "\u00C7");
            MP_SETS.put('E', "\u00C9\u00CA");
            MP_SETS.put('I', "\u00CD");
            MP_SETS.put('O', "\u00D5\u00D3\u00D4");
            MP_SETS.put('U', "\u00DA\u00DC");
            MP_SETS.put('a', "\u00E3\u00E1\u00E2\u00E0");
            MP_SETS.put('c', "\u00E7");
            MP_SETS.put('e', "\u00E9\u00EA");
            MP_SETS.put('i', "\u00ED");
            MP_SETS.put('o', "\u00F5\u00F3\u00F4");
            MP_SETS.put('u', "\u00FA\u00FC");
        } else if (mLang.equals("ro")) {
            MP_SETS.put('A', "\u0102\u00C2");
            MP_SETS.put('I', "\u00CE");
            MP_SETS.put('S', "\u0218");
            MP_SETS.put('T', "\u0162");
            MP_SETS.put('a', "\u0103\u00E2");
            MP_SETS.put('i', "\u00EE");
            MP_SETS.put('s', "\u0219");
            MP_SETS.put('t', "\u0163");
        } else if (mLang.equals("sk")) {
            MP_SETS.put('A', "\u00C1\u00C4");
            MP_SETS.put('C', "\u010C");
            MP_SETS.put('D', "\u010E");
            MP_SETS.put('E', "\u00C9");
            MP_SETS.put('I', "\u00CD");
            MP_SETS.put('L', "\u013D\u0139");
            MP_SETS.put('N', "\u0147");
            MP_SETS.put('O', "\u00D3");
            MP_SETS.put('R', "\u0154");
            MP_SETS.put('S', "\u0160");
            MP_SETS.put('T', "\u0164");
            MP_SETS.put('U', "\u00DA");
            MP_SETS.put('Y', "\u00DD");
            MP_SETS.put('Z', "\u017D");
            MP_SETS.put('a', "\u00E1\u00E4");
            MP_SETS.put('c', "\u010D");
            MP_SETS.put('d', "\u010F");
            MP_SETS.put('e', "\u00E9");
            MP_SETS.put('i', "\u00ED");
            MP_SETS.put('l', "\u013E\u013A");
            MP_SETS.put('n', "\u0148");
            MP_SETS.put('o', "\u00F3");
            MP_SETS.put('r', "\u0155");
            MP_SETS.put('s', "\u0161");
            MP_SETS.put('t', "\u0165");
            MP_SETS.put('u', "\u00FA");
            MP_SETS.put('y', "\u00FD");
            MP_SETS.put('z', "\u017E");
        } else if (mLang.equals("sl")) {
            MP_SETS.put('c', "\u010D");
            MP_SETS.put('s', "\u0161");
            MP_SETS.put('z', "\u017E");
            MP_SETS.put('C', "\u010C");
            MP_SETS.put('S', "\u0160");
            MP_SETS.put('Z', "\u017D");
        } else if (mLang.equals("sv")) {
            MP_SETS.put('A', "\u00C4\u00C5");
            MP_SETS.put('E', "\u00C9");
            MP_SETS.put('O', "\u00D6");
            MP_SETS.put('a', "\u00E4\u00E5");
            MP_SETS.put('e', "\u00E9");
            MP_SETS.put('o', "\u00F6");
        }
    }

    public int getInputType() {
        return makeTextContentType(mAutoCap, mAutoText);
    }
    
    public boolean onKeyDown(View view, Editable content,
                             int keyCode, KeyEvent event) {
        int selStart, selEnd;
        int pref = 0;

        if (view != null) {
            pref = TextKeyListener.getInstance().getPrefs(view.getContext());
        }

        {
            int a = Selection.getSelectionStart(content);
            int b = Selection.getSelectionEnd(content);

            selStart = Math.min(a, b);
            selEnd = Math.max(a, b);

            if (selStart < 0 || selEnd < 0) {
                selStart = selEnd = 0;
                Selection.setSelection(content, 0, 0);
            }
        }

        int activeStart = content.getSpanStart(TextKeyListener.ACTIVE);
        int activeEnd = content.getSpanEnd(TextKeyListener.ACTIVE);

        // QWERTY keyboard normal case

        int i = event.getUnicodeChar(getMetaState(content));
        int k = event.getKeyCode();
        multiPressTimeout = SystemProperties.getInt("persist.sys.keypad_multipress_t", 500);
        String lang = SystemProperties.get("persist.sys.keypad_multipress_l", "auto");
        if (lang.equals("auto")) {
            lang = Locale.getDefault().getLanguage();
        }
        if (!lang.equals(mLang)) {
            mLang = lang;
            updateMPSets();
        }

        int count = event.getRepeatCount();

        if (count == 0 && k == KeyEvent.KEYCODE_SWITCH_CHARSET) {
            updateCyrillicMPSets();
            return true;
        }

        if (count == 0 && i != 0 && !KeyEvent.isModifierKey(k)) {
            long currTime = System.currentTimeMillis();
            if (k == mLastKey && multiPressTimeout >= currTime - mLastPressTime && selStart > 0) {
                mRepeatCount++;
                if (!mAlreadyReplaced) {
                    mCharToReplace = content.charAt(selStart - 1);
                }
                String set = MP_SETS.get(mCharToReplace);
                if (set != null) {
                    if (mRepeatCount > set.length()) {
                        mRepeatCount = 0;
                        content.replace(selEnd - 1, selEnd,
                                new String(Character.toChars(mCharToReplace)));
                    } else {
                        content.replace(selEnd - 1, selEnd, String.valueOf(set.charAt(mRepeatCount-1)));
                    }
                    mLastPressTime = currTime;
                    mLastKey = k;
                    mAlreadyReplaced = true;
                    adjustMetaAfterKeypress(content);
                    return true;
                }
            } else {
                mRepeatCount = 0;
            }
            mLastPressTime = currTime;
            mLastKey = k;
            mAlreadyReplaced = false;
            mCharToReplace = i;
        }

        if (count > 0 && selStart == selEnd && selStart > 0) {
            char c = content.charAt(selStart - 1);
            String set = PICKER_SETS.get(i);
            if (count > 1 && set != null &&
                    (c == set.charAt(0) || c == Character.toUpperCase(set.charAt(0)))) {
                adjustMetaAfterKeypress(content);
                return true;
            } else if (view != null && (c == i || c == Character.toUpperCase(i))) {
                if (showCharacterPicker(view, content, c, false, count)) {
                    adjustMetaAfterKeypress(content);
                    return true;
                }
            }
        }

        if (i == KeyCharacterMap.PICKER_DIALOG_INPUT) {
            if (view != null) {
                showCharacterPicker(view, content,
                                    KeyCharacterMap.PICKER_DIALOG_INPUT, true, 1);
            }
            resetMetaState(content);
            return true;
        }

        if (i == KeyCharacterMap.DOT_WWW_INPUT || i == KeyCharacterMap.DOT_COM_INPUT) {
            content.replace(selStart, selEnd, selStart == 0 ? "www." : ".com");
            adjustMetaAfterKeypress(content);
            return true;
        }

        if (i == KeyCharacterMap.HEX_INPUT) {
            int start;

            if (selStart == selEnd) {
                start = selEnd;

                while (start > 0 && selEnd - start < 4 &&
                       Character.digit(content.charAt(start - 1), 16) >= 0) {
                    start--;
                }
            } else {
                start = selStart;
            }

            int ch = -1;
            try {
                String hex = TextUtils.substring(content, start, selEnd);
                ch = Integer.parseInt(hex, 16);
            } catch (NumberFormatException nfe) { }

            if (ch >= 0) {
                selStart = start;
                Selection.setSelection(content, selStart, selEnd);
                i = ch;
            } else {
                i = 0;
            }
        }

        if (i != 0) {
            boolean dead = false;

            if ((i & KeyCharacterMap.COMBINING_ACCENT) != 0) {
                dead = true;
                i = i & KeyCharacterMap.COMBINING_ACCENT_MASK;
            }

            if (activeStart == selStart && activeEnd == selEnd) {
                boolean replace = false;

                if (selEnd - selStart - 1 == 0) {
                    char accent = content.charAt(selStart);
                    int composed = event.getDeadChar(accent, i);

                    if (composed != 0) {
                        i = composed;
                        replace = true;
                    }
                }

                if (!replace) {
                    Selection.setSelection(content, selEnd);
                    content.removeSpan(TextKeyListener.ACTIVE);
                    selStart = selEnd;
                }
            }

            if ((pref & TextKeyListener.AUTO_CAP) != 0 &&
                Character.isLowerCase(i) && 
                TextKeyListener.shouldCap(mAutoCap, content, selStart)) {
                int where = content.getSpanEnd(TextKeyListener.CAPPED);
                int flags = content.getSpanFlags(TextKeyListener.CAPPED);

                if (where == selStart && (((flags >> 16) & 0xFFFF) == i)) {
                    content.removeSpan(TextKeyListener.CAPPED);
                } else {
                    flags = i << 16;
                    i = Character.toUpperCase(i);

                    if (selStart == 0)
                        content.setSpan(TextKeyListener.CAPPED, 0, 0,
                                        Spannable.SPAN_MARK_MARK | flags);
                    else
                        content.setSpan(TextKeyListener.CAPPED,
                                        selStart - 1, selStart,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE |
                                        flags);
                }
            }

            if (selStart != selEnd) {
                Selection.setSelection(content, selEnd);
            }
            content.setSpan(OLD_SEL_START, selStart, selStart,
                            Spannable.SPAN_MARK_MARK);

            content.replace(selStart, selEnd, String.valueOf((char) i));

            int oldStart = content.getSpanStart(OLD_SEL_START);
            selEnd = Selection.getSelectionEnd(content);

            if (oldStart < selEnd) {
                content.setSpan(TextKeyListener.LAST_TYPED,
                                oldStart, selEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (dead) {
                    Selection.setSelection(content, oldStart, selEnd);
                    content.setSpan(TextKeyListener.ACTIVE, oldStart, selEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            adjustMetaAfterKeypress(content);

            // potentially do autotext replacement if the character
            // that was typed was an autotext terminator

            if ((pref & TextKeyListener.AUTO_TEXT) != 0 && mAutoText &&
                (i == ' ' || i == '\t' || i == '\n' ||
                 i == ',' || i == '.' || i == '!' || i == '?' ||
                 i == '"' || Character.getType(i) == Character.END_PUNCTUATION) &&
                 content.getSpanEnd(TextKeyListener.INHIBIT_REPLACEMENT)
                     != oldStart) {
                int x;

                for (x = oldStart; x > 0; x--) {
                    char c = content.charAt(x - 1);
                    if (c != '\'' && !Character.isLetter(c)) {
                        break;
                    }
                }

                String rep = getReplacement(content, x, oldStart, view);

                if (rep != null) {
                    Replaced[] repl = content.getSpans(0, content.length(),
                                                     Replaced.class);
                    for (int a = 0; a < repl.length; a++)
                        content.removeSpan(repl[a]);

                    char[] orig = new char[oldStart - x];
                    TextUtils.getChars(content, x, oldStart, orig, 0);

                    content.setSpan(new Replaced(orig), x, oldStart,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    content.replace(x, oldStart, rep);
                }
            }

            // Replace two spaces by a period and a space.

            if ((pref & TextKeyListener.AUTO_PERIOD) != 0 && mAutoText) {
                selEnd = Selection.getSelectionEnd(content);
                if (selEnd - 3 >= 0) {
                    if (content.charAt(selEnd - 1) == ' ' &&
                        content.charAt(selEnd - 2) == ' ') {
                        char c = content.charAt(selEnd - 3);

                        for (int j = selEnd - 3; j > 0; j--) {
                            if (c == '"' ||
                                Character.getType(c) == Character.END_PUNCTUATION) {
                                c = content.charAt(j - 1);
                            } else {
                                break;
                            }
                        }

                        if (Character.isLetter(c) || Character.isDigit(c)) {
                            content.replace(selEnd - 2, selEnd - 1, ".");
                        }
                    }
                }
            }

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DEL && selStart == selEnd) {
            // special backspace case for undoing autotext

            int consider = 1;

            // if backspacing over the last typed character,
            // it undoes the autotext prior to that character
            // (unless the character typed was newline, in which
            // case this behavior would be confusing)

            if (content.getSpanEnd(TextKeyListener.LAST_TYPED) == selStart) {
                if (content.charAt(selStart - 1) != '\n')
                    consider = 2;
            }

            Replaced[] repl = content.getSpans(selStart - consider, selStart,
                                             Replaced.class);

            if (repl.length > 0) {
                int st = content.getSpanStart(repl[0]);
                int en = content.getSpanEnd(repl[0]);
                String old = new String(repl[0].mText);

                content.removeSpan(repl[0]);

                // only cancel the autocomplete if the cursor is at the end of
                // the replaced span (or after it, because the user is
                // backspacing over the space after the word, not the word
                // itself).
                if (selStart >= en) {
                    content.setSpan(TextKeyListener.INHIBIT_REPLACEMENT,
                                    en, en, Spannable.SPAN_POINT_POINT);
                    content.replace(st, en, old);

                    en = content.getSpanStart(TextKeyListener.INHIBIT_REPLACEMENT);
                    if (en - 1 >= 0) {
                        content.setSpan(TextKeyListener.INHIBIT_REPLACEMENT,
                                        en - 1, en,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        content.removeSpan(TextKeyListener.INHIBIT_REPLACEMENT);
                    }
                    adjustMetaAfterKeypress(content);
                } else {
                    adjustMetaAfterKeypress(content);
                    return super.onKeyDown(view, content, keyCode, event);
                }

                return true;
            }
        }

        return super.onKeyDown(view, content, keyCode, event);
    }

    private String getReplacement(CharSequence src, int start, int end,
                                  View view) {
        int len = end - start;
        boolean changecase = false;
        
        String replacement = AutoText.get(src, start, end, view);
        
        if (replacement == null) {
            String key = TextUtils.substring(src, start, end).toLowerCase();
            replacement = AutoText.get(key, 0, end - start, view);
            changecase = true;

            if (replacement == null)
                return null;
        }
        
        int caps = 0;

        if (changecase) {
            for (int j = start; j < end; j++) {
                if (Character.isUpperCase(src.charAt(j)))
                    caps++;
            }
        }

        String out;

        if (caps == 0)
            out = replacement;
        else if (caps == 1)
            out = toTitleCase(replacement);
        else if (caps == len)
            out = replacement.toUpperCase();
        else
            out = toTitleCase(replacement);

        if (out.length() == len &&
            TextUtils.regionMatches(src, start, out, 0, len))
            return null;

        return out;
    }

    /**
     * Marks the specified region of <code>content</code> as having
     * contained <code>original</code> prior to AutoText replacement.
     * Call this method when you have done or are about to do an
     * AutoText-style replacement on a region of text and want to let
     * the same mechanism (the user pressing DEL immediately after the
     * change) undo the replacement.
     *
     * @param content the Editable text where the replacement was made
     * @param start the start of the replaced region
     * @param end the end of the replaced region; the location of the cursor
     * @param original the text to be restored if the user presses DEL
     */
    public static void markAsReplaced(Spannable content, int start, int end,
                                      String original) {
        Replaced[] repl = content.getSpans(0, content.length(), Replaced.class);
        for (int a = 0; a < repl.length; a++) {
            content.removeSpan(repl[a]);
        }

        int len = original.length();
        char[] orig = new char[len];
        original.getChars(0, len, orig, 0);

        content.setSpan(new Replaced(orig), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static SparseArray<String> PICKER_SETS =
                        new SparseArray<String>();
    static {
        PICKER_SETS.put('A', "\u00C0\u00C1\u00C2\u00C4\u00C6\u00C3\u00C5\u0104\u0100");
        PICKER_SETS.put('C', "\u00C7\u0106\u010C");
        PICKER_SETS.put('D', "\u010E");
        PICKER_SETS.put('E', "\u00C8\u00C9\u00CA\u00CB\u0118\u011A\u0112");
        PICKER_SETS.put('G', "\u011E");
        PICKER_SETS.put('L', "\u0141\u013d\u0139");
        PICKER_SETS.put('I', "\u00CC\u00CD\u00CE\u00CF\u012A\u0130");
        PICKER_SETS.put('N', "\u00D1\u0143\u0147");
        PICKER_SETS.put('O', "\u00D8\u0152\u00D5\u00D2\u00D3\u00D4\u00D6\u014C");
        PICKER_SETS.put('R', "\u0158");
        PICKER_SETS.put('S', "\u015A\u0160\u015E\u0218");
        PICKER_SETS.put('T', "\u0164\u0162");
        PICKER_SETS.put('U', "\u00D9\u00DA\u00DB\u00DC\u016E\u016A");
        PICKER_SETS.put('Y', "\u00DD\u0178");
        PICKER_SETS.put('Z', "\u0179\u017B\u017D");
        PICKER_SETS.put('a', "\u00E0\u00E1\u00E2\u00E4\u00E6\u00E3\u00E5\u0105\u0101");
        PICKER_SETS.put('c', "\u00E7\u0107\u010D");
        PICKER_SETS.put('d', "\u010F");
        PICKER_SETS.put('e', "\u00E8\u00E9\u00EA\u00EB\u0119\u011B\u0113");
        PICKER_SETS.put('g', "\u011F");
        PICKER_SETS.put('i', "\u00EC\u00ED\u00EE\u00EF\u012B\u0131");
        PICKER_SETS.put('l', "\u0142\u013E\u013A");
        PICKER_SETS.put('n', "\u00F1\u0144\u0148");
        PICKER_SETS.put('o', "\u00F8\u0153\u00F5\u00F2\u00F3\u00F4\u00F6\u014D");
        PICKER_SETS.put('r', "\u0159\u0155");
        PICKER_SETS.put('s', "\u00A7\u00DF\u015B\u0161\u015F\u0219");
        PICKER_SETS.put('t', "\u0165\u0163");
        PICKER_SETS.put('u', "\u00F9\u00FA\u00FB\u00FC\u016F\u016B");
        PICKER_SETS.put('y', "\u00FD\u00FF");
        PICKER_SETS.put('z', "\u017A\u017C\u017E");
        PICKER_SETS.put(KeyCharacterMap.PICKER_DIALOG_INPUT,
                             "\u2026\u00A5\u2022\u00AE\u00A9\u00B1[]{}<>`$\\|");
        PICKER_SETS.put('/', "\\");

        // From packages/inputmethods/LatinIME/res/xml/kbd_symbols.xml

        PICKER_SETS.put('1', "\u00b9\u00bd\u2153\u00bc\u215b");
        PICKER_SETS.put('2', "\u00b2\u2154");
        PICKER_SETS.put('3', "\u00b3\u00be\u215c");
        PICKER_SETS.put('4', "\u2074");
        PICKER_SETS.put('5', "\u215d");
        PICKER_SETS.put('7', "\u215e");
        PICKER_SETS.put('0', "\u207f\u2205");
        PICKER_SETS.put('$', "\u00a2\u00a3\u20ac\u00a5\u20a3\u20a4\u20b1");
        PICKER_SETS.put('%', "\u2030");
        PICKER_SETS.put('*', "\u2020\u2021");
        PICKER_SETS.put('-', "\u2013\u2014");
        PICKER_SETS.put('+', "\u00b1");
        PICKER_SETS.put('(', "[{<");
        PICKER_SETS.put(')', "]}>");
        PICKER_SETS.put('!', "\u00a1");
        PICKER_SETS.put('"', "\u201c\u201d\u00ab\u00bb\u02dd");
        PICKER_SETS.put('?', "\u00bf");
        PICKER_SETS.put(',', "\u201a\u201e");

        // From packages/inputmethods/LatinIME/res/xml/kbd_symbols_shift.xml

        PICKER_SETS.put('=', "\u2260\u2248\u221e");
        PICKER_SETS.put('<', "\u2264\u00ab\u2039");
        PICKER_SETS.put('>', "\u2265\u00bb\u203a");
    };

    private boolean showCharacterPicker(View view, Editable content, char c,
                                        boolean insert, int count) {
        String set = PICKER_SETS.get(c);
        if (set == null) {
            return false;
        }

        if (count == 1) {
            if (set.length() == 1) {
                int selEnd = Selection.getSelectionEnd(content);
                if (insert || selEnd == 0) {
                    content.insert(selEnd, set);
                } else {
                    content.replace(selEnd - 1, selEnd, set);
                }
            } else {
                new CharacterPickerDialog(view.getContext(),
                                      view, content, set, insert).show();
            }
        }

        return true;
    }

    private static String toTitleCase(String src) {
        return Character.toUpperCase(src.charAt(0)) + src.substring(1);
    }

    /* package */ static class Replaced implements NoCopySpan
    {
        public Replaced(char[] text) {
            mText = text;
        }

        private char[] mText;
    }

    private Capitalize mAutoCap;
    private boolean mAutoText;
}

