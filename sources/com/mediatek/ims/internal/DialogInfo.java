package com.mediatek.ims.internal;

import java.util.LinkedList;
import java.util.List;

public class DialogInfo {
    private List<Dialog> mDialogs = new LinkedList();

    /* access modifiers changed from: package-private */
    public boolean addDialog(Dialog dialog) {
        return this.mDialogs.add(dialog);
    }

    /* access modifiers changed from: package-private */
    public List<Dialog> getDialogs() {
        return this.mDialogs;
    }

    public static class Dialog {
        private int mDialogId;
        private boolean mExclusive = true;
        private Local mLocal = new Local();
        private String mState = "";

        public Dialog(int dialogId, boolean exclusive, String state, Local local) {
            this.mDialogId = dialogId;
            this.mExclusive = exclusive;
            this.mState = state;
            this.mLocal = local;
        }

        /* access modifiers changed from: package-private */
        public int getDialogId() {
            return this.mDialogId;
        }

        /* access modifiers changed from: package-private */
        public boolean getExclusive() {
            return this.mExclusive;
        }

        /* access modifiers changed from: package-private */
        public String getState() {
            return this.mState;
        }

        /* access modifiers changed from: package-private */
        public Local getLocal() {
            return this.mLocal;
        }

        /* access modifiers changed from: package-private */
        public String getIdentity() {
            return this.mLocal.getIdentity();
        }

        /* access modifiers changed from: package-private */
        public String getTargetUri() {
            return this.mLocal.getTargetUri();
        }

        /* access modifiers changed from: package-private */
        public List<MediaAttribute> getMediaAttributes() {
            return this.mLocal.getMediaAttributes();
        }

        /* access modifiers changed from: package-private */
        public String getPname() {
            return this.mLocal.getPname();
        }

        /* access modifiers changed from: package-private */
        public String getPval() {
            return this.mLocal.getPval();
        }
    }

    public static class Local {
        private String mIdentity = "";
        private List<MediaAttribute> mMediaAttributes = new LinkedList();
        private Param mParam = new Param();
        private String mTargetUri = "";

        public void setIdentity(String identity) {
            this.mIdentity = identity;
        }

        /* access modifiers changed from: package-private */
        public String getIdentity() {
            return this.mIdentity;
        }

        public void setTargetUri(String targetUri) {
            this.mTargetUri = targetUri;
        }

        /* access modifiers changed from: package-private */
        public String getTargetUri() {
            return this.mTargetUri;
        }

        /* access modifiers changed from: package-private */
        public boolean addMediaAttribute(MediaAttribute mediaAttribute) {
            return this.mMediaAttributes.add(mediaAttribute);
        }

        /* access modifiers changed from: package-private */
        public List<MediaAttribute> getMediaAttributes() {
            return this.mMediaAttributes;
        }

        /* access modifiers changed from: package-private */
        public void setParam(Param param) {
            this.mParam = param;
        }

        /* access modifiers changed from: package-private */
        public String getPname() {
            return this.mParam.getPname();
        }

        /* access modifiers changed from: package-private */
        public String getPval() {
            return this.mParam.getPval();
        }
    }

    public static class MediaAttribute {
        private String mMediaDirection = "";
        private String mMediaType = "";
        private boolean mPort0 = false;

        public MediaAttribute(String mediaType, String mediaDirection, boolean port0) {
            this.mMediaType = mediaType;
            this.mMediaDirection = mediaDirection;
            this.mPort0 = port0;
        }

        /* access modifiers changed from: package-private */
        public String getMediaType() {
            return this.mMediaType;
        }

        /* access modifiers changed from: package-private */
        public String getMediaDirection() {
            return this.mMediaDirection;
        }

        /* access modifiers changed from: package-private */
        public boolean isDowngradedVideo() {
            return this.mPort0;
        }
    }

    public static class Param {
        private String mPnam = "";
        private String mPval = "";

        public Param() {
        }

        public Param(String pname, String pval) {
            this.mPnam = pname;
            this.mPval = pval;
        }

        /* access modifiers changed from: package-private */
        public String getPname() {
            return this.mPnam;
        }

        /* access modifiers changed from: package-private */
        public String getPval() {
            return this.mPval;
        }
    }
}
