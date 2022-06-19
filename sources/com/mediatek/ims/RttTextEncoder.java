package com.mediatek.ims;

import android.telephony.Rlog;
import java.io.IOException;

public class RttTextEncoder {
    private static final String LOG_TAG = "RttTextEncoder";
    private final byte B_00000011 = 3;
    private final byte B_00000111 = 7;
    private final byte B_00001111 = 15;
    private final byte B_00011100 = 28;
    private final byte B_00110000 = 48;
    private final byte B_00111100 = 60;
    private final byte B_00111111 = 63;
    private final byte B_10000000 = Byte.MIN_VALUE;
    private final byte B_11000000 = -64;
    private final byte B_11100000 = -32;
    private final byte B_11110000 = -16;
    private byte[] mRemaining = new byte[0];

    private String printBytes(byte[] bytes) {
        String ret = "";
        for (byte b : bytes) {
            ret = ret + Integer.toString(b & 255, 16) + " ";
        }
        return ret;
    }

    private char[] toUCS2(byte[] utf8Bytes) {
        CharList charList = new CharList();
        byte[] utf8Bytes2 = appendByteArray(this.mRemaining, utf8Bytes);
        clearRemaining();
        int i = 0;
        while (true) {
            if (i >= utf8Bytes2.length) {
                break;
            }
            try {
                byte b = utf8Bytes2[i];
                if (!isNotHead(b)) {
                    if (b > 0) {
                        charList.add((char) b);
                    } else if ((b & -16) == -16) {
                        if (checkIsRemaining(i + 1, utf8Bytes2)) {
                            break;
                        }
                        byte b2 = utf8Bytes2[i + 1];
                        if (isNotHead(b2)) {
                            i++;
                            if (checkIsRemaining(i + 1, utf8Bytes2)) {
                                break;
                            }
                            byte b3 = utf8Bytes2[i + 1];
                            if (isNotHead(b3)) {
                                i++;
                                if (checkIsRemaining(i + 1, utf8Bytes2)) {
                                    break;
                                }
                                byte b4 = utf8Bytes2[i + 1];
                                if (isNotHead(b4)) {
                                    i++;
                                    charList.add(makeChar(((b & 7) << 2) + ((b2 & 48) >> 4), ((b2 & 15) << 4) + ((b3 & 60) >> 2), ((b3 & 3) << 6) + (b4 & 63)));
                                    clearRemaining();
                                }
                            }
                        }
                    } else if ((b & -32) == -32) {
                        if (checkIsRemaining(i + 1, utf8Bytes2)) {
                            break;
                        }
                        byte b22 = utf8Bytes2[i + 1];
                        if (isNotHead(b22)) {
                            i++;
                            if (checkIsRemaining(i + 1, utf8Bytes2)) {
                                break;
                            }
                            byte b32 = utf8Bytes2[i + 1];
                            if (isNotHead(b32)) {
                                i++;
                                charList.add(makeChar(((b & 15) << 4) + ((b22 & 60) >> 2), ((b22 & 3) << 6) + (b32 & 63)));
                                clearRemaining();
                            }
                        }
                    } else if (checkIsRemaining(i + 1, utf8Bytes2)) {
                        break;
                    } else {
                        byte b23 = utf8Bytes2[i + 1];
                        if (isNotHead(b23)) {
                            i++;
                            charList.add(makeChar((b & 28) >> 2, ((b & 3) << 6) + (b23 & 63)));
                            clearRemaining();
                        }
                    }
                }
                i++;
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "IndexOutOfBoundsException: toUCS2");
            }
        }
        return charList.toArray();
    }

    private boolean isNotHead(byte b) {
        return (b & -64) == Byte.MIN_VALUE;
    }

    private int makeChar(int b1, int b2) {
        return (b1 << 8) + b2;
    }

    private int makeChar(int b1, int b2, int b3) {
        return (b1 << 16) + (b2 << 8) + b3;
    }

    private boolean checkIsRemaining(int index, byte[] utf8Bytes) {
        addRemaining(utf8Bytes[index - 1]);
        if (index >= utf8Bytes.length) {
            return true;
        }
        return false;
    }

    private void addRemaining(byte b) {
        this.mRemaining = appendByteArray(this.mRemaining, new byte[]{b});
    }

    private void clearRemaining() {
        this.mRemaining = new byte[0];
    }

    private byte[] appendByteArray(byte[] a, byte[] b) {
        if (a.length == 0) {
            return b;
        }
        if (b.length == 0) {
            return a;
        }
        byte[] byteArray = new byte[(a.length + b.length)];
        System.arraycopy(a, 0, byteArray, 0, a.length);
        System.arraycopy(b, 0, byteArray, a.length, b.length);
        return byteArray;
    }

    private class CharList {
        private char[] data;
        private int used;

        private CharList() {
            this.data = null;
            this.used = 0;
        }

        public void add(int c) {
            char[] cArr = this.data;
            if (cArr == null) {
                this.data = new char[16];
            } else {
                int i = this.used;
                if (i >= cArr.length) {
                    char[] temp = new char[(cArr.length * 2)];
                    System.arraycopy(cArr, 0, temp, 0, i);
                    this.data = temp;
                }
            }
            char[] tmp = Character.toChars(c);
            for (char c2 : tmp) {
                char[] cArr2 = this.data;
                int i2 = this.used;
                this.used = i2 + 1;
                cArr2[i2] = c2;
            }
        }

        public char[] toArray() {
            int i = this.used;
            char[] chars = new char[i];
            System.arraycopy(this.data, 0, chars, 0, i);
            return chars;
        }
    }

    public String getUnicodeFromUTF8(String utf8) {
        try {
            String text = new String(utf8);
            int length = text.length();
            if (length <= 0) {
                return null;
            }
            byte[] data = new byte[(length / 2)];
            for (int i = 0; i < length; i += 2) {
                data[i / 2] = (byte) ((Character.digit(text.charAt(i), 16) << 4) + Character.digit(text.charAt(i + 1), 16));
            }
            String decodeText = new String(toUCS2(data));
            Rlog.d(LOG_TAG, "Decode len = " + sensitiveEncode(String.valueOf(decodeText.length())) + ", textMessage = " + sensitiveEncode(printBytes(decodeText.getBytes())) + ", remain len: " + sensitiveEncode(String.valueOf(this.mRemaining.length)) + ", " + sensitiveEncode(printBytes(this.mRemaining)));
            String BOM = null;
            try {
                BOM = new String(new byte[]{-17, -69, -65}, "utf-8");
            } catch (IOException e) {
                Rlog.e(LOG_TAG, "Exception when transcode bom to string.");
            }
            if (decodeText.length() != 1 || !decodeText.equals(BOM) || this.mRemaining.length != 0) {
                return decodeText;
            }
            Rlog.d(LOG_TAG, "found BOM, ignore it");
            return null;
        } catch (Exception e2) {
            Rlog.e(LOG_TAG, "Exception: handleRttTextReceivedIndication");
            return null;
        }
    }

    private String sensitiveEncode(String msg) {
        return ImsServiceCallTracker.sensitiveEncode(msg);
    }
}
