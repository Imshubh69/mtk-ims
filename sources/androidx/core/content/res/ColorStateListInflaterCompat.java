package androidx.core.content.res;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class ColorStateListInflaterCompat {
    private static final ThreadLocal<TypedValue> sTempTypedValue = new ThreadLocal<>();

    private ColorStateListInflaterCompat() {
    }

    public static ColorStateList inflate(Resources resources, int resId, Resources.Theme theme) {
        try {
            return createFromXml(resources, resources.getXml(resId), theme);
        } catch (Exception e) {
            Log.e("CSLCompat", "Failed to inflate ColorStateList.", e);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0012  */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0017  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.content.res.ColorStateList createFromXml(android.content.res.Resources r4, org.xmlpull.v1.XmlPullParser r5, android.content.res.Resources.Theme r6) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            android.util.AttributeSet r0 = android.util.Xml.asAttributeSet(r5)
        L_0x0004:
            int r1 = r5.next()
            r2 = r1
            r3 = 2
            if (r1 == r3) goto L_0x0010
            r1 = 1
            if (r2 == r1) goto L_0x0010
            goto L_0x0004
        L_0x0010:
            if (r2 != r3) goto L_0x0017
            android.content.res.ColorStateList r1 = createFromXmlInner(r4, r5, r0, r6)
            return r1
        L_0x0017:
            org.xmlpull.v1.XmlPullParserException r1 = new org.xmlpull.v1.XmlPullParserException
            java.lang.String r3 = "No start tag found"
            r1.<init>(r3)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.core.content.res.ColorStateListInflaterCompat.createFromXml(android.content.res.Resources, org.xmlpull.v1.XmlPullParser, android.content.res.Resources$Theme):android.content.res.ColorStateList");
    }

    public static ColorStateList createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        String name = parser.getName();
        if (name.equals("selector")) {
            return inflate(r, parser, attrs, theme);
        }
        throw new XmlPullParserException(parser.getPositionDescription() + ": invalid color state list tag " + name);
    }

    /* JADX WARNING: type inference failed for: r4v6, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static android.content.res.ColorStateList inflate(android.content.res.Resources r19, org.xmlpull.v1.XmlPullParser r20, android.util.AttributeSet r21, android.content.res.Resources.Theme r22) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            r1 = r19
            r2 = r21
            r3 = r22
            int r0 = r20.getDepth()
            r4 = 1
            int r5 = r0 + 1
            r0 = 20
            int[][] r0 = new int[r0][]
            int r6 = r0.length
            int[] r6 = new int[r6]
            r7 = 0
            r8 = r7
            r7 = r6
            r6 = r0
        L_0x0018:
            int r0 = r20.next()
            r9 = r0
            if (r0 == r4) goto L_0x00f1
            int r0 = r20.getDepth()
            r11 = r0
            if (r0 >= r5) goto L_0x0029
            r0 = 3
            if (r9 == r0) goto L_0x00f1
        L_0x0029:
            r0 = 2
            if (r9 != r0) goto L_0x00ea
            if (r11 > r5) goto L_0x00ea
            java.lang.String r0 = r20.getName()
            java.lang.String r12 = "item"
            boolean r0 = r0.equals(r12)
            if (r0 != 0) goto L_0x003c
            goto L_0x00ea
        L_0x003c:
            int[] r0 = androidx.core.C0008R.styleable.ColorStateListItem
            android.content.res.TypedArray r12 = obtainAttributes(r1, r3, r2, r0)
            int r0 = androidx.core.C0008R.styleable.ColorStateListItem_android_color
            r13 = -1
            int r14 = r12.getResourceId(r0, r13)
            r15 = -65281(0xffffffffffff00ff, float:NaN)
            if (r14 == r13) goto L_0x0069
            boolean r0 = isColorInt(r1, r14)
            if (r0 != 0) goto L_0x0069
            android.content.res.XmlResourceParser r0 = r1.getXml(r14)     // Catch:{ Exception -> 0x0061 }
            android.content.res.ColorStateList r0 = createFromXml(r1, r0, r3)     // Catch:{ Exception -> 0x0061 }
            int r0 = r0.getDefaultColor()     // Catch:{ Exception -> 0x0061 }
            goto L_0x0068
        L_0x0061:
            r0 = move-exception
            int r13 = androidx.core.C0008R.styleable.ColorStateListItem_android_color
            int r0 = r12.getColor(r13, r15)
        L_0x0068:
            goto L_0x006f
        L_0x0069:
            int r0 = androidx.core.C0008R.styleable.ColorStateListItem_android_color
            int r0 = r12.getColor(r0, r15)
        L_0x006f:
            r13 = 1065353216(0x3f800000, float:1.0)
            int r15 = androidx.core.C0008R.styleable.ColorStateListItem_android_alpha
            boolean r15 = r12.hasValue(r15)
            if (r15 == 0) goto L_0x0080
            int r15 = androidx.core.C0008R.styleable.ColorStateListItem_android_alpha
            float r13 = r12.getFloat(r15, r13)
            goto L_0x008e
        L_0x0080:
            int r15 = androidx.core.C0008R.styleable.ColorStateListItem_alpha
            boolean r15 = r12.hasValue(r15)
            if (r15 == 0) goto L_0x008e
            int r15 = androidx.core.C0008R.styleable.ColorStateListItem_alpha
            float r13 = r12.getFloat(r15, r13)
        L_0x008e:
            r12.recycle()
            r15 = 0
            int r4 = r21.getAttributeCount()
            int[] r10 = new int[r4]
            r16 = 0
            r1 = r16
        L_0x009c:
            if (r1 >= r4) goto L_0x00cb
            int r3 = r2.getAttributeNameResource(r1)
            r16 = r4
            r4 = 16843173(0x10101a5, float:2.3694738E-38)
            if (r3 == r4) goto L_0x00c4
            r4 = 16843551(0x101031f, float:2.3695797E-38)
            if (r3 == r4) goto L_0x00c4
            int r4 = androidx.core.C0008R.attr.alpha
            if (r3 == r4) goto L_0x00c4
            int r4 = r15 + 1
            r17 = r4
            r4 = 0
            boolean r18 = r2.getAttributeBooleanValue(r1, r4)
            if (r18 == 0) goto L_0x00bf
            r4 = r3
            goto L_0x00c0
        L_0x00bf:
            int r4 = -r3
        L_0x00c0:
            r10[r15] = r4
            r15 = r17
        L_0x00c4:
            int r1 = r1 + 1
            r3 = r22
            r4 = r16
            goto L_0x009c
        L_0x00cb:
            r16 = r4
            int[] r1 = android.util.StateSet.trimStateSet(r10, r15)
            int r3 = modulateColorAlpha(r0, r13)
            int[] r7 = androidx.core.content.res.GrowingArrayUtils.append((int[]) r7, (int) r8, (int) r3)
            java.lang.Object[] r4 = androidx.core.content.res.GrowingArrayUtils.append((T[]) r6, (int) r8, r1)
            r6 = r4
            int[][] r6 = (int[][]) r6
            int r8 = r8 + 1
            r4 = 1
            r1 = r19
            r3 = r22
            goto L_0x0018
        L_0x00ea:
            r4 = 1
            r1 = r19
            r3 = r22
            goto L_0x0018
        L_0x00f1:
            int[] r0 = new int[r8]
            int[][] r1 = new int[r8][]
            r3 = 0
            java.lang.System.arraycopy(r7, r3, r0, r3, r8)
            java.lang.System.arraycopy(r6, r3, r1, r3, r8)
            android.content.res.ColorStateList r3 = new android.content.res.ColorStateList
            r3.<init>(r1, r0)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.core.content.res.ColorStateListInflaterCompat.inflate(android.content.res.Resources, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, android.content.res.Resources$Theme):android.content.res.ColorStateList");
    }

    private static boolean isColorInt(Resources r, int resId) {
        TypedValue value = getTypedValue();
        r.getValue(resId, value, true);
        if (value.type < 28 || value.type > 31) {
            return false;
        }
        return true;
    }

    private static TypedValue getTypedValue() {
        ThreadLocal<TypedValue> threadLocal = sTempTypedValue;
        TypedValue tv = threadLocal.get();
        if (tv != null) {
            return tv;
        }
        TypedValue tv2 = new TypedValue();
        threadLocal.set(tv2);
        return tv2;
    }

    private static TypedArray obtainAttributes(Resources res, Resources.Theme theme, AttributeSet set, int[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    }

    private static int modulateColorAlpha(int color, float alphaMod) {
        return (16777215 & color) | (Math.round(((float) Color.alpha(color)) * alphaMod) << 24);
    }
}
