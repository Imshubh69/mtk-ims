package com.mediatek.ims.config.internal;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import com.mediatek.ims.config.ImsConfigContract;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public final class ImsConfigProvider extends ContentProvider {
    static final String AUTHORITY = "com.mediatek.ims.config.provider";
    private static final boolean DEBUG;
    static HashMap<Integer, Boolean> ECCAllowBroadcast = new HashMap<>();
    static HashMap<Integer, Boolean> ECCAllowSendCmd = new HashMap<>();
    static HashMap<Integer, String> LatestSimState = new HashMap<>();
    private static final String PROPERTY_IMSCONFIG_FORCE_NOTIFY = "vendor.ril.imsconfig.force.notify";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final String TAG = "ImsConfigProvider";
    /* access modifiers changed from: private */
    public final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(ImsConfigProvider.TAG, "[onReceive] action=" + intent.getAction());
            Context deviceContext = context.createDeviceProtectedStorageContext();
            if (!deviceContext.moveDatabaseFrom(context, "imsconfig.db")) {
                Log.wtf(ImsConfigProvider.TAG, "[onReceive] Failed to move database");
            }
            Log.d(ImsConfigProvider.TAG, "[onReceive] Move database successfully");
            ImsConfigProvider.this.mDatabaseHelper = new SqlDatabaseHelper(deviceContext);
            Log.d(ImsConfigProvider.TAG, "[onReceive] Create mDatabaseHelper again");
            context.unregisterReceiver(ImsConfigProvider.this.mBroadcastReceiver);
        }
    };
    private Context mContext;
    SqlDatabaseHelper mDatabaseHelper = null;

    static {
        boolean z = false;
        if (TextUtils.equals(Build.TYPE, "eng") || SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        DEBUG = z;
    }

    public boolean onCreate() {
        Context context = getContext();
        Context deviceContext = context.createDeviceProtectedStorageContext();
        if (UserManager.get(context).isUserUnlocked()) {
            if (!deviceContext.moveDatabaseFrom(context, "imsconfig.db")) {
                Log.wtf(TAG, "[onCreate] Failed to move database");
            }
            Log.d(TAG, "[onCreate] Move database successfully");
        } else {
            Log.d(TAG, "[onCreate] User locked, register receiver for BOOT_COMPLETED");
            context.registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
        }
        this.mDatabaseHelper = new SqlDatabaseHelper(deviceContext);
        this.mContext = getContext();
        return true;
    }

    public String getType(Uri uri) {
        return "vnd.android.cursor.item/imsconfig";
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Arguments args = new Arguments(3, uri, selection, selectionArgs);
        try {
            return this.mDatabaseHelper.getWritableDatabase().delete(args.table, args.selection, args.selectionArgs);
        } catch (SQLiteFullException e) {
            onDiskFull(e);
            return 0;
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        Arguments args = new Arguments(1, values, uri);
        long newId = 0;
        try {
            newId = this.mDatabaseHelper.getWritableDatabase().insertWithOnConflict(args.table, (String) null, values, 5);
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        }
        return prepareResultUri(args, newId);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.net.Uri prepareResultUri(com.mediatek.ims.config.internal.ImsConfigProvider.Arguments r4, long r5) {
        /*
            r3 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "content://com.mediatek.ims.config.provider/"
            r0.append(r1)
            java.lang.String r1 = r4.table
            r0.append(r1)
            java.lang.String r1 = "/"
            r0.append(r1)
            java.lang.String r1 = r4.phoneId
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            android.net.Uri r0 = android.net.Uri.parse(r0)
            java.lang.String r1 = r4.table
            int r2 = r1.hashCode()
            switch(r2) {
                case 45084740: goto L_0x003f;
                case 1412604243: goto L_0x0035;
                case 1545420144: goto L_0x002b;
                default: goto L_0x002a;
            }
        L_0x002a:
            goto L_0x0049
        L_0x002b:
            java.lang.String r2 = "tb_default"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x002a
            r1 = 0
            goto L_0x004a
        L_0x0035:
            java.lang.String r2 = "tb_master"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x002a
            r1 = 2
            goto L_0x004a
        L_0x003f:
            java.lang.String r2 = "tb_provision"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x002a
            r1 = 1
            goto L_0x004a
        L_0x0049:
            r1 = -1
        L_0x004a:
            switch(r1) {
                case 0: goto L_0x0052;
                case 1: goto L_0x0052;
                case 2: goto L_0x0052;
                default: goto L_0x004d;
            }
        L_0x004d:
            android.net.Uri r0 = android.content.ContentUris.withAppendedId(r0, r5)
            goto L_0x0059
        L_0x0052:
            java.lang.String r1 = r4.itemId
            android.net.Uri r0 = android.net.Uri.withAppendedPath(r0, r1)
        L_0x0059:
            java.lang.String r1 = r4.param
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x006c
            java.lang.String r1 = r4.param
            int r1 = java.lang.Integer.parseInt(r1)
            long r1 = (long) r1
            android.net.Uri r0 = android.content.ContentUris.withAppendedId(r0, r1)
        L_0x006c:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigProvider.prepareResultUri(com.mediatek.ims.config.internal.ImsConfigProvider$Arguments, long):android.net.Uri");
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        Arguments args = new Arguments(2, uri, values, selection, selectionArgs);
        try {
            count = this.mDatabaseHelper.getWritableDatabase().update(args.table, values, args.selection, args.selectionArgs);
            if (count > 0) {
                notifyChange(uri, args, values);
            }
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        }
        return count;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri uri2 = uri;
        String[] strArr = projection;
        int i = 0;
        Arguments args = new Arguments(0, uri2);
        SQLiteDatabase db = this.mDatabaseHelper.getReadableDatabase();
        Pattern pattern = Pattern.compile("[\\W]");
        int length = strArr.length;
        while (i < length) {
            String projectionArg = strArr[i];
            if (projectionArg.length() > 64) {
                Log.e(TAG, "Found column name that was too long (" + projectionArg.length() + " characters)");
                return null;
            } else if (pattern.matcher(projectionArg).find()) {
                Log.e(TAG, "Found invalid character in column name: " + projectionArg + ", uri: " + uri2);
                return null;
            } else {
                i++;
            }
        }
        return db.query(args.table, projection, args.selection, args.selectionArgs, (String) null, (String) null, (String) null);
    }

    public void shutdown() {
        super.shutdown();
        SqlDatabaseHelper sqlDatabaseHelper = this.mDatabaseHelper;
        if (sqlDatabaseHelper != null) {
            sqlDatabaseHelper.close();
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyChange(android.net.Uri r24, com.mediatek.ims.config.internal.ImsConfigProvider.Arguments r25, android.content.ContentValues r26) {
        /*
            r23 = this;
            r0 = r24
            r1 = r25
            r2 = r26
            r3 = -1
            r4 = 0
            r5 = 0
            r6 = 0
            java.lang.String r7 = ""
            java.lang.String r8 = r1.table
            int r9 = r8.hashCode()
            r10 = 0
            java.lang.Boolean r11 = java.lang.Boolean.valueOf(r10)
            switch(r9) {
                case -2133078972: goto L_0x004d;
                case -978591195: goto L_0x0043;
                case -321961281: goto L_0x0039;
                case 45084740: goto L_0x002f;
                case 1412604243: goto L_0x0025;
                case 1545420144: goto L_0x001b;
                default: goto L_0x001a;
            }
        L_0x001a:
            goto L_0x0057
        L_0x001b:
            java.lang.String r9 = "tb_default"
            boolean r8 = r8.equals(r9)
            if (r8 == 0) goto L_0x001a
            r8 = r10
            goto L_0x0058
        L_0x0025:
            java.lang.String r9 = "tb_master"
            boolean r8 = r8.equals(r9)
            if (r8 == 0) goto L_0x001a
            r8 = 1
            goto L_0x0058
        L_0x002f:
            java.lang.String r9 = "tb_provision"
            boolean r8 = r8.equals(r9)
            if (r8 == 0) goto L_0x001a
            r8 = 2
            goto L_0x0058
        L_0x0039:
            java.lang.String r9 = "tb_resource"
            boolean r8 = r8.equals(r9)
            if (r8 == 0) goto L_0x001a
            r8 = 5
            goto L_0x0058
        L_0x0043:
            java.lang.String r9 = "tb_feature"
            boolean r8 = r8.equals(r9)
            if (r8 == 0) goto L_0x001a
            r8 = 4
            goto L_0x0058
        L_0x004d:
            java.lang.String r9 = "tb_config_setting"
            boolean r8 = r8.equals(r9)
            if (r8 == 0) goto L_0x001a
            r8 = 3
            goto L_0x0058
        L_0x0057:
            r8 = -1
        L_0x0058:
            java.lang.String r13 = "item"
            java.lang.String r14 = "phone_id"
            java.lang.String r15 = " value: "
            java.lang.String r9 = "Update uri "
            java.lang.String r10 = "value"
            java.lang.String r12 = " on phone "
            r17 = r3
            java.lang.String r3 = "ImsConfigProvider"
            switch(r8) {
                case 0: goto L_0x024a;
                case 1: goto L_0x024a;
                case 2: goto L_0x024a;
                case 3: goto L_0x0216;
                case 4: goto L_0x0092;
                case 5: goto L_0x008f;
                default: goto L_0x006b;
            }
        L_0x006b:
            r18 = r4
            r19 = r5
            r5 = r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "Invalid table "
            r2.append(r4)
            java.lang.String r4 = r1.table
            r2.append(r4)
            java.lang.String r4 = " with uri "
            r2.append(r4)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r3, r2)
            return
        L_0x008f:
            r5 = r2
            goto L_0x0323
        L_0x0092:
            java.util.HashMap<java.lang.Integer, java.lang.String> r8 = LatestSimState
            r18 = r4
            java.lang.String r4 = r1.phoneId
            int r4 = java.lang.Integer.parseInt(r4)
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            java.lang.Object r4 = r8.get(r4)
            java.lang.String r4 = (java.lang.String) r4
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r19 = r5
            java.lang.String r5 = "getSimState() for checking whether broadcast phoneId: "
            r8.append(r5)
            java.lang.String r5 = r1.phoneId
            int r5 = java.lang.Integer.parseInt(r5)
            r8.append(r5)
            java.lang.String r5 = ", Sim state: "
            r8.append(r5)
            r8.append(r4)
            java.lang.String r5 = r8.toString()
            android.util.Log.d(r3, r5)
            java.lang.String r5 = r1.itemId
            int r5 = java.lang.Integer.parseInt(r5)
            java.lang.Integer r8 = r2.getAsInteger(r10)
            int r8 = r8.intValue()
            if (r4 != 0) goto L_0x00dc
            java.lang.String r4 = ""
        L_0x00dc:
            r20 = r6
            java.util.HashMap<java.lang.Integer, java.lang.Boolean> r6 = ECCAllowBroadcast
            r21 = r7
            java.lang.String r7 = r1.phoneId
            int r7 = java.lang.Integer.parseInt(r7)
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            java.lang.Object r6 = r6.get(r7)
            if (r6 != 0) goto L_0x00f6
            r6 = 0
            r18 = r19
            goto L_0x0144
        L_0x00f6:
            java.lang.String r6 = "ABSENT"
            boolean r6 = r4.equals(r6)
            if (r6 == 0) goto L_0x011a
            java.util.HashMap<java.lang.Integer, java.lang.Boolean> r6 = ECCAllowBroadcast
            java.lang.String r7 = r1.phoneId
            int r7 = java.lang.Integer.parseInt(r7)
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            java.lang.Object r6 = r6.get(r7)
            java.lang.Boolean r6 = (java.lang.Boolean) r6
            boolean r6 = r6.booleanValue()
            if (r6 == 0) goto L_0x011a
            if (r5 != 0) goto L_0x011a
            r6 = 1
            goto L_0x011b
        L_0x011a:
            r6 = 0
        L_0x011b:
            r7 = 1
            if (r8 != r7) goto L_0x0120
            r7 = 1
            goto L_0x0121
        L_0x0120:
            r7 = 0
        L_0x0121:
            if (r6 == 0) goto L_0x013e
            if (r7 != 0) goto L_0x013e
            r17 = r6
            java.util.HashMap<java.lang.Integer, java.lang.Boolean> r6 = ECCAllowBroadcast
            r18 = r7
            java.lang.String r7 = r1.phoneId
            int r7 = java.lang.Integer.parseInt(r7)
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            r6.put(r7, r11)
            java.lang.String r6 = "Sim absent but the calculated VoLTE is false, so no need broadcast"
            android.util.Log.d(r3, r6)
            goto L_0x0142
        L_0x013e:
            r17 = r6
            r18 = r7
        L_0x0142:
            r6 = r17
        L_0x0144:
            if (r5 != 0) goto L_0x0153
            java.lang.String r7 = "vendor.ril.imsconfig.force.notify"
            r2 = 0
            int r7 = android.os.SystemProperties.getInt(r7, r2)
            r2 = 1
            if (r7 != r2) goto L_0x0153
            r16 = 1
            goto L_0x0155
        L_0x0153:
            r16 = 0
        L_0x0155:
            r2 = r16
            boolean r7 = DEBUG
            if (r7 == 0) goto L_0x0174
            r16 = r15
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            r22 = r12
            java.lang.String r12 = "isForceNotify "
            r15.append(r12)
            r15.append(r2)
            java.lang.String r12 = r15.toString()
            android.util.Log.d(r3, r12)
            goto L_0x0178
        L_0x0174:
            r22 = r12
            r16 = r15
        L_0x0178:
            java.lang.String r12 = "READY"
            boolean r12 = r4.equals(r12)
            if (r12 != 0) goto L_0x019b
            java.lang.String r12 = "IMSI"
            boolean r12 = r4.equals(r12)
            if (r12 != 0) goto L_0x019b
            java.lang.String r12 = "LOADED"
            boolean r12 = r4.equals(r12)
            if (r12 != 0) goto L_0x019b
            java.lang.String r12 = "LOCKED"
            boolean r12 = r4.equals(r12)
            if (r12 == 0) goto L_0x0199
            goto L_0x019b
        L_0x0199:
            r12 = 0
            goto L_0x019c
        L_0x019b:
            r12 = 1
        L_0x019c:
            if (r12 != 0) goto L_0x01a9
            if (r6 == 0) goto L_0x01a2
            if (r18 != 0) goto L_0x01a9
        L_0x01a2:
            if (r2 == 0) goto L_0x01a5
            goto L_0x01a9
        L_0x01a5:
            r5 = r26
            goto L_0x0323
        L_0x01a9:
            android.content.Intent r15 = new android.content.Intent
            r17 = r2
            java.lang.String r2 = "com.android.intent.action.IMS_FEATURE_CHANGED"
            r15.<init>(r2)
            r2 = r15
            java.lang.String r15 = r1.phoneId
            int r15 = java.lang.Integer.parseInt(r15)
            r2.putExtra(r14, r15)
            r2.putExtra(r13, r5)
            r2.putExtra(r10, r8)
            r10 = 1
            r2.addFlags(r10)
            android.content.Context r10 = r23.getContext()
            r10.sendBroadcast(r2)
            android.content.Context r10 = r23.getContext()
            android.content.ContentResolver r10 = r10.getContentResolver()
            r13 = 0
            r10.notifyChange(r0, r13)
            java.util.HashMap<java.lang.Integer, java.lang.Boolean> r10 = ECCAllowBroadcast
            java.lang.String r13 = r1.phoneId
            int r13 = java.lang.Integer.parseInt(r13)
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)
            r10.put(r13, r11)
            if (r7 == 0) goto L_0x0212
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r9)
            r7.append(r0)
            r11 = r22
            r7.append(r11)
            java.lang.String r9 = r1.phoneId
            r7.append(r9)
            r15 = r16
            r7.append(r15)
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            android.util.Log.d(r3, r7)
            r5 = r26
            goto L_0x0323
        L_0x0212:
            r5 = r26
            goto L_0x0323
        L_0x0216:
            r18 = r4
            r19 = r5
            r20 = r6
            r21 = r7
            r11 = r12
            java.lang.String r2 = r1.itemId
            int r2 = java.lang.Integer.parseInt(r2)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "Don't Update uri "
            r4.append(r5)
            r4.append(r0)
            r4.append(r11)
            java.lang.String r5 = r1.phoneId
            r4.append(r5)
            java.lang.String r5 = " for CONFIG_SETTING"
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r3, r4)
            r5 = r26
            goto L_0x0323
        L_0x024a:
            r18 = r4
            r19 = r5
            r20 = r6
            r21 = r7
            r11 = r12
            java.lang.String r2 = r1.itemId
            int r2 = com.mediatek.ims.config.ImsConfigContract.configNameToId(r2)
            java.lang.String r4 = "mimetype_id"
            r5 = r26
            java.lang.Integer r4 = r5.getAsInteger(r4)
            int r4 = r4.intValue()
            java.lang.String r6 = "data"
            if (r4 != 0) goto L_0x0274
            java.lang.Integer r6 = r5.getAsInteger(r6)
            int r6 = r6.intValue()
            r7 = r21
            goto L_0x027d
        L_0x0274:
            r7 = 1
            if (r4 != r7) goto L_0x0306
            java.lang.String r7 = r5.getAsString(r6)
            r6 = r17
        L_0x027d:
            android.content.Intent r8 = new android.content.Intent
            java.lang.String r12 = "com.android.intent.action.IMS_CONFIG_CHANGED"
            r8.<init>(r12, r0)
            java.lang.String r12 = r1.phoneId
            int r12 = java.lang.Integer.parseInt(r12)
            r8.putExtra(r14, r12)
            r8.putExtra(r13, r2)
            r12 = 1
            r8.addFlags(r12)
            if (r4 != 0) goto L_0x029a
            r8.putExtra(r10, r6)
            goto L_0x029f
        L_0x029a:
            if (r4 != r12) goto L_0x029f
            r8.putExtra(r10, r7)
        L_0x029f:
            java.lang.String r10 = "mimetype"
            r8.putExtra(r10, r4)
            android.content.Context r10 = r23.getContext()
            r10.sendBroadcast(r8)
            android.content.Context r10 = r23.getContext()
            android.content.ContentResolver r10 = r10.getContentResolver()
            r12 = 0
            r10.notifyChange(r0, r12)
            if (r4 != 0) goto L_0x02de
            boolean r10 = DEBUG
            if (r10 == 0) goto L_0x02de
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            r10.append(r9)
            r10.append(r0)
            r10.append(r11)
            java.lang.String r9 = r1.phoneId
            r10.append(r9)
            r10.append(r15)
            r10.append(r6)
            java.lang.String r9 = r10.toString()
            android.util.Log.d(r3, r9)
            goto L_0x0323
        L_0x02de:
            r10 = 1
            if (r4 != r10) goto L_0x0323
            boolean r10 = DEBUG
            if (r10 == 0) goto L_0x0323
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            r10.append(r9)
            r10.append(r0)
            r10.append(r11)
            java.lang.String r9 = r1.phoneId
            r10.append(r9)
            r10.append(r15)
            r10.append(r7)
            java.lang.String r9 = r10.toString()
            android.util.Log.d(r3, r9)
            goto L_0x0323
        L_0x0306:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "Config "
            r6.append(r7)
            r6.append(r2)
            java.lang.String r7 = " not String or Integer, mimeType "
            r6.append(r7)
            r6.append(r4)
            java.lang.String r6 = r6.toString()
            android.util.Log.e(r3, r6)
        L_0x0323:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigProvider.notifyChange(android.net.Uri, com.mediatek.ims.config.internal.ImsConfigProvider$Arguments, android.content.ContentValues):void");
    }

    private static class Arguments {
        private static final int INDEX_ITEM_ID = 2;
        private static final int INDEX_PARAM_ID = 3;
        private static final int INDEX_PHONE_ID = 1;
        private static final int INDEX_TABLE = 0;
        public String itemId;
        public String param;
        public String phoneId;
        public String selection;
        public String[] selectionArgs;
        public String table;

        Arguments(int opMode, Uri uri, ContentValues cv, String selection2, String[] selectionArgs2) {
            this.table = null;
            this.phoneId = null;
            this.itemId = null;
            this.param = null;
            this.selection = null;
            this.selectionArgs = null;
            enforceValidUri(uri);
            String validTable = getValidTable(uri);
            this.table = validTable;
            parseContentValue(uri, validTable, opMode, cv);
            enforceOpMode(opMode, uri, cv, selection2, selectionArgs2);
            char c = 65535;
            switch (uri.getPathSegments().size()) {
                case 1:
                    this.selection = selection2;
                    this.selectionArgs = selectionArgs2;
                    if (opMode == 2 || opMode == 1) {
                        this.phoneId = cv.getAsString("phone_id");
                        return;
                    }
                    return;
                case 2:
                    String str = uri.getPathSegments().get(1);
                    this.phoneId = str;
                    String[] args = {str};
                    this.selection = "phone_id = ?";
                    if (!TextUtils.isEmpty(selection2)) {
                        this.selection += " AND " + selection2;
                        this.selectionArgs = join(args, selectionArgs2);
                        return;
                    }
                    this.selectionArgs = args;
                    return;
                case 3:
                    this.phoneId = uri.getPathSegments().get(1);
                    this.itemId = uri.getPathSegments().get(2);
                    String[] args2 = new String[2];
                    args2[0] = this.phoneId;
                    String str2 = this.table;
                    switch (str2.hashCode()) {
                        case -2133078972:
                            if (str2.equals(ImsConfigContract.TABLE_CONFIG_SETTING)) {
                                c = 0;
                                break;
                            }
                            break;
                        case -321961281:
                            if (str2.equals(ImsConfigContract.TABLE_RESOURCE)) {
                                c = 4;
                                break;
                            }
                            break;
                        case 45084740:
                            if (str2.equals(ImsConfigContract.TABLE_PROVISION)) {
                                c = 2;
                                break;
                            }
                            break;
                        case 1412604243:
                            if (str2.equals(ImsConfigContract.TABLE_MASTER)) {
                                c = 3;
                                break;
                            }
                            break;
                        case 1545420144:
                            if (str2.equals(ImsConfigContract.TABLE_DEFAULT)) {
                                c = 1;
                                break;
                            }
                            break;
                    }
                    switch (c) {
                        case 0:
                            args2[1] = this.itemId;
                            this.selection = "phone_id = ? AND setting_id = ?";
                            if (!TextUtils.isEmpty(selection2)) {
                                this.selection += " AND " + selection2;
                                this.selectionArgs = join(args2, selectionArgs2);
                                return;
                            }
                            this.selectionArgs = args2;
                            return;
                        case 1:
                        case 2:
                        case 3:
                            args2[1] = String.valueOf(ImsConfigContract.configNameToId(this.itemId));
                            this.selection = "phone_id = ? AND config_id = ?";
                            if (!TextUtils.isEmpty(selection2)) {
                                this.selection += " AND " + selection2;
                                this.selectionArgs = join(args2, selectionArgs2);
                                return;
                            }
                            this.selectionArgs = args2;
                            return;
                        case 4:
                            this.selection = "phone_id=? AND feature_id=?";
                            args2[1] = this.itemId;
                            if (!TextUtils.isEmpty(selection2)) {
                                this.selection += " AND " + selection2;
                                this.selectionArgs = join(args2, selectionArgs2);
                                return;
                            }
                            this.selectionArgs = args2;
                            return;
                        default:
                            return;
                    }
                case 4:
                    this.phoneId = uri.getPathSegments().get(1);
                    this.itemId = uri.getPathSegments().get(2);
                    String str3 = uri.getPathSegments().get(3);
                    this.param = str3;
                    String[] args3 = {this.phoneId, this.itemId, str3};
                    String str4 = this.table;
                    switch (str4.hashCode()) {
                        case -978591195:
                            if (str4.equals(ImsConfigContract.TABLE_FEATURE)) {
                                c = 0;
                                break;
                            }
                            break;
                    }
                    switch (c) {
                        case 0:
                            this.selection = "phone_id = ? AND feature_id = ? AND network_id = ?";
                            if (!TextUtils.isEmpty(selection2)) {
                                this.selection += " AND " + selection2;
                                this.selectionArgs = join(args3, selectionArgs2);
                                return;
                            }
                            this.selectionArgs = args3;
                            return;
                        default:
                            throw new IllegalArgumentException("Invalid URI: " + uri);
                    }
                default:
                    throw new IllegalArgumentException("Invalid URI: " + uri);
            }
        }

        Arguments(int opMode, Uri uri, String selection2, String[] selectionArgs2) {
            this(opMode, uri, (ContentValues) null, selection2, selectionArgs2);
        }

        Arguments(int opMode, ContentValues cv, Uri uri) {
            this(opMode, uri, cv, (String) null, (String[]) null);
        }

        Arguments(int opMode, Uri uri) {
            this(opMode, uri, (ContentValues) null, (String) null, (String[]) null);
        }

        private static String[] join(String[]... arrays) {
            List<String> output = new ArrayList<>();
            for (String[] array : arrays) {
                output.addAll(Arrays.asList(array));
            }
            return (String[]) output.toArray(new String[output.size()]);
        }

        private String getValidTable(Uri uri) {
            String table2 = uri.getPathSegments().get(0);
            enforceValidTable(table2);
            return table2;
        }

        private static void enforceOpMode(int opMode, Uri uri, ContentValues cv, String selection2, String[] selectionArgs2) {
        }

        private static void enforceValidTable(String table2) {
            if (!ImsConfigContract.Validator.isValidTable(table2)) {
                throw new IllegalArgumentException("Bad table: " + table2);
            }
        }

        private static void enforceValidUri(Uri uri) {
            if (uri == null) {
                throw new IllegalArgumentException("Bad request: null url");
            } else if (uri.getPathSegments().size() == 0) {
                throw new IllegalArgumentException("Operate on entire database is not supported");
            }
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0041, code lost:
            if (r10.equals(com.mediatek.ims.config.ImsConfigContract.TABLE_PROVISION) != false) goto L_0x0059;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void parseContentValue(android.net.Uri r9, java.lang.String r10, int r11, android.content.ContentValues r12) {
            /*
                r8 = this;
                if (r11 == 0) goto L_0x0206
                r0 = 3
                if (r11 != r0) goto L_0x0007
                goto L_0x0206
            L_0x0007:
                enforceValidTable(r10)
                java.lang.String r1 = "phone_id"
                java.lang.Integer r1 = r12.getAsInteger(r1)
                java.lang.String r1 = java.lang.String.valueOf(r1)
                r8.phoneId = r1
                boolean r1 = android.text.TextUtils.isEmpty(r1)
                if (r1 != 0) goto L_0x01ef
                r1 = 0
                r2 = -1
                int r3 = r10.hashCode()
                switch(r3) {
                    case -2133078972: goto L_0x004e;
                    case -978591195: goto L_0x0044;
                    case 45084740: goto L_0x003b;
                    case 1412604243: goto L_0x0031;
                    case 1545420144: goto L_0x0027;
                    default: goto L_0x0026;
                }
            L_0x0026:
                goto L_0x0058
            L_0x0027:
                java.lang.String r0 = "tb_default"
                boolean r0 = r10.equals(r0)
                if (r0 == 0) goto L_0x0026
                r0 = 2
                goto L_0x0059
            L_0x0031:
                java.lang.String r0 = "tb_master"
                boolean r0 = r10.equals(r0)
                if (r0 == 0) goto L_0x0026
                r0 = 4
                goto L_0x0059
            L_0x003b:
                java.lang.String r3 = "tb_provision"
                boolean r3 = r10.equals(r3)
                if (r3 == 0) goto L_0x0026
                goto L_0x0059
            L_0x0044:
                java.lang.String r0 = "tb_feature"
                boolean r0 = r10.equals(r0)
                if (r0 == 0) goto L_0x0026
                r0 = 1
                goto L_0x0059
            L_0x004e:
                java.lang.String r0 = "tb_config_setting"
                boolean r0 = r10.equals(r0)
                if (r0 == 0) goto L_0x0026
                r0 = 0
                goto L_0x0059
            L_0x0058:
                r0 = r2
            L_0x0059:
                java.lang.String r2 = "Invalid config id in cv: "
                java.lang.String r3 = "config_id"
                java.lang.String r4 = " with "
                switch(r0) {
                    case 0: goto L_0x01ba;
                    case 1: goto L_0x0125;
                    case 2: goto L_0x0064;
                    case 3: goto L_0x00ac;
                    case 4: goto L_0x00ac;
                    default: goto L_0x0062;
                }
            L_0x0062:
                goto L_0x01ee
            L_0x0064:
                java.lang.String r0 = "unit_id"
                boolean r5 = r12.containsKey(r0)
                if (r5 == 0) goto L_0x0098
                java.lang.Integer r0 = r12.getAsInteger(r0)
                int r0 = r0.intValue()
                boolean r5 = com.mediatek.ims.config.ImsConfigContract.Validator.isValidUnitId(r0)
                if (r5 == 0) goto L_0x007b
                goto L_0x0098
            L_0x007b:
                java.lang.IllegalArgumentException r2 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                java.lang.String r5 = "Invalid time unit in cv: "
                r3.append(r5)
                r3.append(r0)
                r3.append(r4)
                r3.append(r9)
                java.lang.String r3 = r3.toString()
                r2.<init>(r3)
                throw r2
            L_0x0098:
                java.lang.Integer r0 = r12.getAsInteger(r3)
                int r1 = r0.intValue()
                boolean r0 = com.mediatek.ims.config.ImsConfigContract.Validator.isValidConfigId(r1)
                if (r0 == 0) goto L_0x010a
                java.lang.String r0 = com.mediatek.ims.config.ImsConfigContract.configIdToName(r1)
                r8.itemId = r0
            L_0x00ac:
                java.lang.String r0 = "mimetype_id"
                java.lang.Integer r0 = r12.getAsInteger(r0)
                int r0 = r0.intValue()
                boolean r5 = com.mediatek.ims.config.ImsConfigContract.Validator.isValidMimeTypeId(r0)
                if (r5 == 0) goto L_0x00ed
                java.lang.Integer r3 = r12.getAsInteger(r3)
                int r1 = r3.intValue()
                boolean r3 = com.mediatek.ims.config.ImsConfigContract.Validator.isValidConfigId(r1)
                if (r3 == 0) goto L_0x00d2
                java.lang.String r2 = com.mediatek.ims.config.ImsConfigContract.configIdToName(r1)
                r8.itemId = r2
                goto L_0x01ee
            L_0x00d2:
                java.lang.IllegalArgumentException r3 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                r5.append(r2)
                r5.append(r1)
                r5.append(r4)
                r5.append(r9)
                java.lang.String r2 = r5.toString()
                r3.<init>(r2)
                throw r3
            L_0x00ed:
                java.lang.IllegalArgumentException r2 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                java.lang.String r5 = "Invalid mime type in cv: "
                r3.append(r5)
                r3.append(r0)
                r3.append(r4)
                r3.append(r9)
                java.lang.String r3 = r3.toString()
                r2.<init>(r3)
                throw r2
            L_0x010a:
                java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                r3.append(r2)
                r3.append(r1)
                r3.append(r4)
                r3.append(r9)
                java.lang.String r2 = r3.toString()
                r0.<init>(r2)
                throw r0
            L_0x0125:
                java.lang.String r0 = "feature_id"
                java.lang.Integer r0 = r12.getAsInteger(r0)
                int r0 = r0.intValue()
                boolean r2 = com.mediatek.ims.config.ImsConfigContract.Validator.isValidFeatureId(r0)
                if (r2 == 0) goto L_0x019d
                java.lang.String r2 = java.lang.String.valueOf(r0)
                r8.itemId = r2
                java.lang.String r2 = "network_id"
                java.lang.Integer r2 = r12.getAsInteger(r2)
                int r2 = r2.intValue()
                boolean r3 = com.mediatek.ims.config.ImsConfigContract.Validator.isValidNetwork(r2)
                if (r3 == 0) goto L_0x0180
                java.lang.String r3 = java.lang.String.valueOf(r2)
                r8.param = r3
                java.lang.String r3 = "value"
                java.lang.Integer r3 = r12.getAsInteger(r3)
                int r3 = r3.intValue()
                boolean r5 = com.mediatek.ims.config.ImsConfigContract.Validator.isValidFeatureValue(r3)
                if (r5 == 0) goto L_0x0163
                goto L_0x01ee
            L_0x0163:
                java.lang.IllegalArgumentException r5 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r6 = new java.lang.StringBuilder
                r6.<init>()
                java.lang.String r7 = "Invalid value in cv: "
                r6.append(r7)
                r6.append(r3)
                r6.append(r4)
                r6.append(r9)
                java.lang.String r4 = r6.toString()
                r5.<init>(r4)
                throw r5
            L_0x0180:
                java.lang.IllegalArgumentException r3 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                r5.<init>()
                java.lang.String r6 = "Invalid network in cv: "
                r5.append(r6)
                r5.append(r2)
                r5.append(r4)
                r5.append(r9)
                java.lang.String r4 = r5.toString()
                r3.<init>(r4)
                throw r3
            L_0x019d:
                java.lang.IllegalArgumentException r2 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                java.lang.String r5 = "Invalid feature id in cv: "
                r3.append(r5)
                r3.append(r0)
                r3.append(r4)
                r3.append(r9)
                java.lang.String r3 = r3.toString()
                r2.<init>(r3)
                throw r2
            L_0x01ba:
                java.lang.String r0 = "setting_id"
                java.lang.Integer r0 = r12.getAsInteger(r0)
                int r0 = r0.intValue()
                boolean r2 = com.mediatek.ims.config.ImsConfigContract.Validator.isValidSettingId(r0)
                if (r2 == 0) goto L_0x01d1
                java.lang.String r2 = java.lang.String.valueOf(r0)
                r8.itemId = r2
                goto L_0x01ee
            L_0x01d1:
                java.lang.IllegalArgumentException r2 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                java.lang.String r5 = "Invalid setting id in cv: "
                r3.append(r5)
                r3.append(r0)
                r3.append(r4)
                r3.append(r9)
                java.lang.String r3 = r3.toString()
                r2.<init>(r3)
                throw r2
            L_0x01ee:
                return
            L_0x01ef:
                java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "Expect phone id in cv with "
                r1.append(r2)
                r1.append(r9)
                java.lang.String r1 = r1.toString()
                r0.<init>(r1)
                throw r0
            L_0x0206:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigProvider.Arguments.parseContentValue(android.net.Uri, java.lang.String, int, android.content.ContentValues):void");
        }
    }

    private static class OperationMode {
        static final int MODE_DELETE = 3;
        static final int MODE_INSERT = 1;
        static final int MODE_QUERY = 0;
        static final int MODE_UPDATE = 2;

        private OperationMode() {
        }
    }

    private void onDiskFull(SQLiteFullException e) {
        Log.e(TAG, "Disk full, all write operations will be ignored", e);
    }
}
