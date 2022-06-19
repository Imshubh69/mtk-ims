package androidx.localbroadcastmanager.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;
import java.util.HashMap;

@Deprecated
public final class LocalBroadcastManager {
    private static final boolean DEBUG = false;
    static final int MSG_EXEC_PENDING_BROADCASTS = 1;
    private static final String TAG = "LocalBroadcastManager";
    private static LocalBroadcastManager mInstance;
    private static final Object mLock = new Object();
    private final HashMap<String, ArrayList<ReceiverRecord>> mActions = new HashMap<>();
    private final Context mAppContext;
    private final Handler mHandler;
    private final ArrayList<BroadcastRecord> mPendingBroadcasts = new ArrayList<>();
    private final HashMap<BroadcastReceiver, ArrayList<ReceiverRecord>> mReceivers = new HashMap<>();

    private static final class ReceiverRecord {
        boolean broadcasting;
        boolean dead;
        final IntentFilter filter;
        final BroadcastReceiver receiver;

        ReceiverRecord(IntentFilter _filter, BroadcastReceiver _receiver) {
            this.filter = _filter;
            this.receiver = _receiver;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder(128);
            builder.append("Receiver{");
            builder.append(this.receiver);
            builder.append(" filter=");
            builder.append(this.filter);
            if (this.dead) {
                builder.append(" DEAD");
            }
            builder.append("}");
            return builder.toString();
        }
    }

    private static final class BroadcastRecord {
        final Intent intent;
        final ArrayList<ReceiverRecord> receivers;

        BroadcastRecord(Intent _intent, ArrayList<ReceiverRecord> _receivers) {
            this.intent = _intent;
            this.receivers = _receivers;
        }
    }

    public static LocalBroadcastManager getInstance(Context context) {
        LocalBroadcastManager localBroadcastManager;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new LocalBroadcastManager(context.getApplicationContext());
            }
            localBroadcastManager = mInstance;
        }
        return localBroadcastManager;
    }

    private LocalBroadcastManager(Context context) {
        this.mAppContext = context;
        this.mHandler = new Handler(context.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        LocalBroadcastManager.this.executePendingBroadcasts();
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }
            }
        };
    }

    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        synchronized (this.mReceivers) {
            ReceiverRecord entry = new ReceiverRecord(filter, receiver);
            ArrayList<ReceiverRecord> filters = this.mReceivers.get(receiver);
            if (filters == null) {
                filters = new ArrayList<>(1);
                this.mReceivers.put(receiver, filters);
            }
            filters.add(entry);
            for (int i = 0; i < filter.countActions(); i++) {
                String action = filter.getAction(i);
                ArrayList<ReceiverRecord> entries = this.mActions.get(action);
                if (entries == null) {
                    entries = new ArrayList<>(1);
                    this.mActions.put(action, entries);
                }
                entries.add(entry);
            }
        }
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        synchronized (this.mReceivers) {
            ArrayList<ReceiverRecord> filters = this.mReceivers.remove(receiver);
            if (filters != null) {
                for (int i = filters.size() - 1; i >= 0; i--) {
                    ReceiverRecord filter = filters.get(i);
                    filter.dead = true;
                    for (int j = 0; j < filter.filter.countActions(); j++) {
                        String action = filter.filter.getAction(j);
                        ArrayList<ReceiverRecord> receivers = this.mActions.get(action);
                        if (receivers != null) {
                            for (int k = receivers.size() - 1; k >= 0; k--) {
                                ReceiverRecord rec = receivers.get(k);
                                if (rec.receiver == receiver) {
                                    rec.dead = true;
                                    receivers.remove(k);
                                }
                            }
                            if (receivers.size() <= 0) {
                                this.mActions.remove(action);
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0168, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x016d, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean sendBroadcast(android.content.Intent r19) {
        /*
            r18 = this;
            r1 = r18
            r2 = r19
            java.util.HashMap<android.content.BroadcastReceiver, java.util.ArrayList<androidx.localbroadcastmanager.content.LocalBroadcastManager$ReceiverRecord>> r3 = r1.mReceivers
            monitor-enter(r3)
            java.lang.String r5 = r19.getAction()     // Catch:{ all -> 0x016f }
            android.content.Context r0 = r1.mAppContext     // Catch:{ all -> 0x016f }
            android.content.ContentResolver r0 = r0.getContentResolver()     // Catch:{ all -> 0x016f }
            java.lang.String r0 = r2.resolveTypeIfNeeded(r0)     // Catch:{ all -> 0x016f }
            android.net.Uri r8 = r19.getData()     // Catch:{ all -> 0x016f }
            java.lang.String r4 = r19.getScheme()     // Catch:{ all -> 0x016f }
            r11 = r4
            java.util.Set r9 = r19.getCategories()     // Catch:{ all -> 0x016f }
            int r4 = r19.getFlags()     // Catch:{ all -> 0x016f }
            r4 = r4 & 8
            if (r4 == 0) goto L_0x002d
            r4 = 1
            goto L_0x002e
        L_0x002d:
            r4 = 0
        L_0x002e:
            r14 = r4
            if (r14 == 0) goto L_0x0057
            java.lang.String r4 = "LocalBroadcastManager"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x016f }
            r6.<init>()     // Catch:{ all -> 0x016f }
            java.lang.String r7 = "Resolving type "
            r6.append(r7)     // Catch:{ all -> 0x016f }
            r6.append(r0)     // Catch:{ all -> 0x016f }
            java.lang.String r7 = " scheme "
            r6.append(r7)     // Catch:{ all -> 0x016f }
            r6.append(r11)     // Catch:{ all -> 0x016f }
            java.lang.String r7 = " of intent "
            r6.append(r7)     // Catch:{ all -> 0x016f }
            r6.append(r2)     // Catch:{ all -> 0x016f }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x016f }
            android.util.Log.v(r4, r6)     // Catch:{ all -> 0x016f }
        L_0x0057:
            java.util.HashMap<java.lang.String, java.util.ArrayList<androidx.localbroadcastmanager.content.LocalBroadcastManager$ReceiverRecord>> r4 = r1.mActions     // Catch:{ all -> 0x016f }
            java.lang.String r6 = r19.getAction()     // Catch:{ all -> 0x016f }
            java.lang.Object r4 = r4.get(r6)     // Catch:{ all -> 0x016f }
            java.util.ArrayList r4 = (java.util.ArrayList) r4     // Catch:{ all -> 0x016f }
            r15 = r4
            if (r15 == 0) goto L_0x016a
            if (r14 == 0) goto L_0x007e
            java.lang.String r4 = "LocalBroadcastManager"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x016f }
            r6.<init>()     // Catch:{ all -> 0x016f }
            java.lang.String r7 = "Action list: "
            r6.append(r7)     // Catch:{ all -> 0x016f }
            r6.append(r15)     // Catch:{ all -> 0x016f }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x016f }
            android.util.Log.v(r4, r6)     // Catch:{ all -> 0x016f }
        L_0x007e:
            r4 = 0
            r6 = 0
            r10 = r4
            r7 = r6
        L_0x0082:
            int r4 = r15.size()     // Catch:{ all -> 0x016f }
            if (r7 >= r4) goto L_0x0135
            java.lang.Object r4 = r15.get(r7)     // Catch:{ all -> 0x016f }
            androidx.localbroadcastmanager.content.LocalBroadcastManager$ReceiverRecord r4 = (androidx.localbroadcastmanager.content.LocalBroadcastManager.ReceiverRecord) r4     // Catch:{ all -> 0x016f }
            r6 = r4
            if (r14 == 0) goto L_0x00a9
            java.lang.String r4 = "LocalBroadcastManager"
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x016f }
            r12.<init>()     // Catch:{ all -> 0x016f }
            java.lang.String r13 = "Matching against filter "
            r12.append(r13)     // Catch:{ all -> 0x016f }
            android.content.IntentFilter r13 = r6.filter     // Catch:{ all -> 0x016f }
            r12.append(r13)     // Catch:{ all -> 0x016f }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x016f }
            android.util.Log.v(r4, r12)     // Catch:{ all -> 0x016f }
        L_0x00a9:
            boolean r4 = r6.broadcasting     // Catch:{ all -> 0x016f }
            if (r4 == 0) goto L_0x00c4
            if (r14 == 0) goto L_0x00bd
            java.lang.String r4 = "LocalBroadcastManager"
            java.lang.String r12 = "  Filter's target already added"
            android.util.Log.v(r4, r12)     // Catch:{ all -> 0x016f }
            r17 = r0
            r16 = r7
            r0 = r10
            goto L_0x012e
        L_0x00bd:
            r17 = r0
            r16 = r7
            r0 = r10
            goto L_0x012e
        L_0x00c4:
            android.content.IntentFilter r4 = r6.filter     // Catch:{ all -> 0x016f }
            java.lang.String r12 = "LocalBroadcastManager"
            r13 = r6
            r6 = r0
            r16 = r7
            r7 = r11
            r17 = r0
            r0 = r10
            r10 = r12
            int r4 = r4.match(r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x016f }
            if (r4 < 0) goto L_0x0104
            if (r14 == 0) goto L_0x00f3
            java.lang.String r6 = "LocalBroadcastManager"
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x016f }
            r7.<init>()     // Catch:{ all -> 0x016f }
            java.lang.String r10 = "  Filter matched!  match=0x"
            r7.append(r10)     // Catch:{ all -> 0x016f }
            java.lang.String r10 = java.lang.Integer.toHexString(r4)     // Catch:{ all -> 0x016f }
            r7.append(r10)     // Catch:{ all -> 0x016f }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x016f }
            android.util.Log.v(r6, r7)     // Catch:{ all -> 0x016f }
        L_0x00f3:
            if (r0 != 0) goto L_0x00fc
            java.util.ArrayList r6 = new java.util.ArrayList     // Catch:{ all -> 0x016f }
            r6.<init>()     // Catch:{ all -> 0x016f }
            r10 = r6
            goto L_0x00fd
        L_0x00fc:
            r10 = r0
        L_0x00fd:
            r10.add(r13)     // Catch:{ all -> 0x016f }
            r0 = 1
            r13.broadcasting = r0     // Catch:{ all -> 0x016f }
            goto L_0x012f
        L_0x0104:
            if (r14 == 0) goto L_0x012e
            switch(r4) {
                case -4: goto L_0x0115;
                case -3: goto L_0x0112;
                case -2: goto L_0x010f;
                case -1: goto L_0x010c;
                default: goto L_0x0109;
            }     // Catch:{ all -> 0x016f }
        L_0x0109:
            java.lang.String r6 = "unknown reason"
            goto L_0x0118
        L_0x010c:
            java.lang.String r6 = "type"
            goto L_0x0118
        L_0x010f:
            java.lang.String r6 = "data"
            goto L_0x0118
        L_0x0112:
            java.lang.String r6 = "action"
            goto L_0x0118
        L_0x0115:
            java.lang.String r6 = "category"
        L_0x0118:
            java.lang.String r7 = "LocalBroadcastManager"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x016f }
            r10.<init>()     // Catch:{ all -> 0x016f }
            java.lang.String r12 = "  Filter did not match: "
            r10.append(r12)     // Catch:{ all -> 0x016f }
            r10.append(r6)     // Catch:{ all -> 0x016f }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x016f }
            android.util.Log.v(r7, r10)     // Catch:{ all -> 0x016f }
        L_0x012e:
            r10 = r0
        L_0x012f:
            int r7 = r16 + 1
            r0 = r17
            goto L_0x0082
        L_0x0135:
            r17 = r0
            r16 = r7
            r0 = r10
            if (r0 == 0) goto L_0x016c
            r4 = 0
        L_0x013d:
            int r6 = r0.size()     // Catch:{ all -> 0x016f }
            if (r4 >= r6) goto L_0x014f
            java.lang.Object r6 = r0.get(r4)     // Catch:{ all -> 0x016f }
            androidx.localbroadcastmanager.content.LocalBroadcastManager$ReceiverRecord r6 = (androidx.localbroadcastmanager.content.LocalBroadcastManager.ReceiverRecord) r6     // Catch:{ all -> 0x016f }
            r7 = 0
            r6.broadcasting = r7     // Catch:{ all -> 0x016f }
            int r4 = r4 + 1
            goto L_0x013d
        L_0x014f:
            java.util.ArrayList<androidx.localbroadcastmanager.content.LocalBroadcastManager$BroadcastRecord> r4 = r1.mPendingBroadcasts     // Catch:{ all -> 0x016f }
            androidx.localbroadcastmanager.content.LocalBroadcastManager$BroadcastRecord r6 = new androidx.localbroadcastmanager.content.LocalBroadcastManager$BroadcastRecord     // Catch:{ all -> 0x016f }
            r6.<init>(r2, r0)     // Catch:{ all -> 0x016f }
            r4.add(r6)     // Catch:{ all -> 0x016f }
            android.os.Handler r4 = r1.mHandler     // Catch:{ all -> 0x016f }
            r6 = 1
            boolean r4 = r4.hasMessages(r6)     // Catch:{ all -> 0x016f }
            if (r4 != 0) goto L_0x0167
            android.os.Handler r4 = r1.mHandler     // Catch:{ all -> 0x016f }
            r4.sendEmptyMessage(r6)     // Catch:{ all -> 0x016f }
        L_0x0167:
            monitor-exit(r3)     // Catch:{ all -> 0x016f }
            r3 = 1
            return r3
        L_0x016a:
            r17 = r0
        L_0x016c:
            monitor-exit(r3)     // Catch:{ all -> 0x016f }
            r0 = 0
            return r0
        L_0x016f:
            r0 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x016f }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.localbroadcastmanager.content.LocalBroadcastManager.sendBroadcast(android.content.Intent):boolean");
    }

    public void sendBroadcastSync(Intent intent) {
        if (sendBroadcast(intent)) {
            executePendingBroadcasts();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001c, code lost:
        if (r0 >= r2.length) goto L_0x0000;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        r1 = r2[r0];
        r3 = r1.receivers.size();
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0027, code lost:
        if (r4 >= r3) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
        r5 = r1.receivers.get(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0033, code lost:
        if (r5.dead != false) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0035, code lost:
        r5.receiver.onReceive(r9.mAppContext, r1.intent);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003e, code lost:
        r4 = r4 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0041, code lost:
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001a, code lost:
        r0 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void executePendingBroadcasts() {
        /*
            r9 = this;
        L_0x0000:
            java.util.HashMap<android.content.BroadcastReceiver, java.util.ArrayList<androidx.localbroadcastmanager.content.LocalBroadcastManager$ReceiverRecord>> r0 = r9.mReceivers
            monitor-enter(r0)
            java.util.ArrayList<androidx.localbroadcastmanager.content.LocalBroadcastManager$BroadcastRecord> r1 = r9.mPendingBroadcasts     // Catch:{ all -> 0x0045 }
            int r1 = r1.size()     // Catch:{ all -> 0x0045 }
            if (r1 > 0) goto L_0x000d
            monitor-exit(r0)     // Catch:{ all -> 0x0045 }
            return
        L_0x000d:
            androidx.localbroadcastmanager.content.LocalBroadcastManager$BroadcastRecord[] r2 = new androidx.localbroadcastmanager.content.LocalBroadcastManager.BroadcastRecord[r1]     // Catch:{ all -> 0x0045 }
            java.util.ArrayList<androidx.localbroadcastmanager.content.LocalBroadcastManager$BroadcastRecord> r3 = r9.mPendingBroadcasts     // Catch:{ all -> 0x0045 }
            r3.toArray(r2)     // Catch:{ all -> 0x0045 }
            java.util.ArrayList<androidx.localbroadcastmanager.content.LocalBroadcastManager$BroadcastRecord> r3 = r9.mPendingBroadcasts     // Catch:{ all -> 0x0045 }
            r3.clear()     // Catch:{ all -> 0x0045 }
            monitor-exit(r0)     // Catch:{ all -> 0x0045 }
            r0 = 0
        L_0x001b:
            int r1 = r2.length
            if (r0 >= r1) goto L_0x0044
            r1 = r2[r0]
            java.util.ArrayList<androidx.localbroadcastmanager.content.LocalBroadcastManager$ReceiverRecord> r3 = r1.receivers
            int r3 = r3.size()
            r4 = 0
        L_0x0027:
            if (r4 >= r3) goto L_0x0041
            java.util.ArrayList<androidx.localbroadcastmanager.content.LocalBroadcastManager$ReceiverRecord> r5 = r1.receivers
            java.lang.Object r5 = r5.get(r4)
            androidx.localbroadcastmanager.content.LocalBroadcastManager$ReceiverRecord r5 = (androidx.localbroadcastmanager.content.LocalBroadcastManager.ReceiverRecord) r5
            boolean r6 = r5.dead
            if (r6 != 0) goto L_0x003e
            android.content.BroadcastReceiver r6 = r5.receiver
            android.content.Context r7 = r9.mAppContext
            android.content.Intent r8 = r1.intent
            r6.onReceive(r7, r8)
        L_0x003e:
            int r4 = r4 + 1
            goto L_0x0027
        L_0x0041:
            int r0 = r0 + 1
            goto L_0x001b
        L_0x0044:
            goto L_0x0000
        L_0x0045:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0045 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.localbroadcastmanager.content.LocalBroadcastManager.executePendingBroadcasts():void");
    }
}
