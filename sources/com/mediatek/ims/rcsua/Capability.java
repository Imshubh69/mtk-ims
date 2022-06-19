package com.mediatek.ims.rcsua;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Capability implements Parcelable {
    public static final Parcelable.Creator<Capability> CREATOR = new Parcelable.Creator<Capability>() {
        public Capability createFromParcel(Parcel in) {
            return new Capability(in.readString());
        }

        public Capability[] newArray(int size) {
            return new Capability[size];
        }
    };
    private final String IARI_CHATBOT = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.chatbot";
    private final String IARI_CHATBOT_SA = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.chatbot.sa";
    private final String IARI_FT_HTTP = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.fthttp";
    private final String IARI_GEO_PULL = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopull";
    private final String IARI_GEO_PULLFT = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopullft";
    private final String IARI_GEO_PUSH = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopush";
    private final String IARI_GEO_SMS = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geosms";
    private final String ICSI_CPM_FILETRANSFER = "urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.filetransfer";
    private final String ICSI_CPM_LARGE_MSG = "urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.largemsg";
    private final String ICSI_CPM_MSG = "urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.msg";
    private final String ICSI_CPM_SESSION = "urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.session";
    private final String SVC_3GPP_IARI = "+g.3gpp.iari-ref";
    private final String SVC_3GPP_ICSI = "+g.3gpp.icsi-ref";
    private final String SVC_GSMA_BOTVERSION = "+g.gsma.rcs.botversion";
    private final String SVC_GSMA_CALLCOMPOSER = "+g.gsma.callcomposer";
    private final String SVC_IMDN_AGGREGATION = "imdn-aggregation";
    private LinkedHashMap<String, TagValueList> features = new LinkedHashMap<>();

    public Capability() {
        initialize((String) null);
    }

    public Capability(String features2) {
        initialize(features2);
    }

    public Capability(long features2) {
        initialize(features2);
    }

    public Capability add(Capability features2) {
        addFeature(features2);
        return this;
    }

    public Capability remove(Capability features2) {
        removeFeature(features2);
        return this;
    }

    public Capability add(String features2) {
        addFeature(new Capability(features2));
        return this;
    }

    public Capability remove(String features2) {
        removeFeature(new Capability(features2));
        return this;
    }

    public Capability update(String features2) {
        initialize(features2);
        return this;
    }

    public TagValueList get(String tagName) {
        return this.features.get(tagName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0015  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean contains(java.lang.String r7) {
        /*
            r6 = this;
            com.mediatek.ims.rcsua.Capability r0 = new com.mediatek.ims.rcsua.Capability
            r0.<init>((java.lang.String) r7)
            java.util.LinkedHashMap<java.lang.String, com.mediatek.ims.rcsua.Capability$TagValueList> r1 = r0.features
            java.util.Set r1 = r1.entrySet()
            java.util.Iterator r1 = r1.iterator()
        L_0x000f:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x0043
            java.lang.Object r2 = r1.next()
            java.util.Map$Entry r2 = (java.util.Map.Entry) r2
            java.util.LinkedHashMap<java.lang.String, com.mediatek.ims.rcsua.Capability$TagValueList> r3 = r6.features
            java.lang.Object r4 = r2.getKey()
            boolean r3 = r3.containsKey(r4)
            r4 = 0
            if (r3 != 0) goto L_0x0029
            return r4
        L_0x0029:
            java.util.LinkedHashMap<java.lang.String, com.mediatek.ims.rcsua.Capability$TagValueList> r3 = r6.features
            java.lang.Object r5 = r2.getKey()
            java.lang.Object r3 = r3.get(r5)
            com.mediatek.ims.rcsua.Capability$TagValueList r3 = (com.mediatek.ims.rcsua.Capability.TagValueList) r3
            java.lang.Object r5 = r2.getValue()
            com.mediatek.ims.rcsua.Capability$TagValueList r5 = (com.mediatek.ims.rcsua.Capability.TagValueList) r5
            boolean r5 = r3.contains(r5)
            if (r5 != 0) goto L_0x0042
            return r4
        L_0x0042:
            goto L_0x000f
        L_0x0043:
            r1 = 1
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.rcsua.Capability.contains(java.lang.String):boolean");
    }

    public Set<String> toFeatureTags() {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, TagValueList> entry : this.features.entrySet()) {
            if (entry.getValue().size() > 0) {
                for (String tag : entry.getValue().getTagValues()) {
                    sb.append(entry.getKey());
                    sb.append("=\"");
                    sb.append(tag);
                    sb.append("\"");
                    result.add(sb.toString());
                }
            } else {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(toString());
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.features.size() > 0) {
            for (Map.Entry<String, TagValueList> entry : this.features.entrySet()) {
                if (builder.length() > 0) {
                    builder.append(';');
                }
                builder.append(entry.getKey());
                if (entry.getValue().size() > 0) {
                    String value = entry.getValue().toString();
                    builder.append('=');
                    builder.append(value);
                }
            }
        }
        return builder.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Capability)) {
            return false;
        }
        if (((Capability) obj).features.hashCode() == this.features.hashCode()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.features.hashCode();
    }

    public long toNumeric() {
        long result = 0;
        if (this.features.size() > 0) {
            for (Map.Entry<String, TagValueList> entry : this.features.entrySet()) {
                String key = entry.getKey();
                TagValueList tags = entry.getValue();
                char c = 65535;
                switch (key.hashCode()) {
                    case -1070519682:
                        if (key.equals("+g.3gpp.icsi-ref")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 177374275:
                        if (key.equals("imdn-aggregation")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1212994329:
                        if (key.equals("+g.gsma.rcs.botversion")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1380893018:
                        if (key.equals("+g.gsma.callcomposer")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1420811101:
                        if (key.equals("+g.3gpp.iari-ref")) {
                            c = 1;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        Iterator it = tags.values.iterator();
                        while (it.hasNext()) {
                            String value = (String) it.next();
                            if ("urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.msg".equalsIgnoreCase(value)) {
                                result |= 4;
                            } else if ("urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.session".equalsIgnoreCase(value)) {
                                result |= 1;
                            } else if ("urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.largemsg".equalsIgnoreCase(value)) {
                                result |= 8;
                            } else if ("urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.filetransfer".equalsIgnoreCase(value)) {
                                result |= 2;
                            }
                        }
                        break;
                    case 1:
                        Iterator it2 = tags.values.iterator();
                        while (it2.hasNext()) {
                            String value2 = (String) it2.next();
                            if ("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopush".equalsIgnoreCase(value2)) {
                                result |= 16;
                            } else if ("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopull".equalsIgnoreCase(value2)) {
                                result |= 32;
                            } else if ("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopullft".equalsIgnoreCase(value2)) {
                                result |= 64;
                            } else if ("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geosms".equalsIgnoreCase(value2)) {
                                result |= 256;
                            } else if ("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.fthttp".equalsIgnoreCase(value2)) {
                                result |= 512;
                            } else if ("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.chatbot".equalsIgnoreCase(value2)) {
                                result |= 2048;
                            } else if ("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.chatbot.sa".equalsIgnoreCase(value2)) {
                                result |= 4096;
                            }
                        }
                        break;
                    case 2:
                        result |= 128;
                        break;
                    case 3:
                        result |= 1024;
                        break;
                    case 4:
                        if (tags.size() != 1) {
                            break;
                        } else {
                            Iterator it3 = tags.values.iterator();
                            while (true) {
                                if (it3.hasNext()) {
                                    if ("#1".equals((String) it3.next())) {
                                        result |= 8192;
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                }
            }
        }
        return result;
    }

    private void initialize(long features2) {
        StringBuilder ICSI = new StringBuilder("+g.3gpp.icsi-ref=\"");
        StringBuilder IARI = new StringBuilder("+g.3gpp.iari-ref=\"");
        if ((1 & features2) > 0) {
            ICSI.append("urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.session,");
        }
        if ((2 & features2) > 0) {
            ICSI.append("urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.filetransfer,");
        }
        if ((4 & features2) > 0) {
            ICSI.append("urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.msg,");
        }
        if ((8 & features2) > 0) {
            ICSI.append("urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.largemsg,");
        }
        if ((16 & features2) > 0) {
            IARI.append("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopush,");
        }
        if ((32 & features2) > 0) {
            IARI.append("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopull,");
        }
        if ((features2 & 64) > 0) {
            IARI.append("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopullft,");
        }
        if ((256 & features2) > 0) {
            IARI.append("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geosms,");
        }
        if ((512 & features2) > 0) {
            IARI.append("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.fthttp,");
        }
        if ((2048 & features2) > 0) {
            IARI.append("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.chatbot,");
        }
        if ((4096 & features2) > 0) {
            IARI.append("urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.chatbot.sa,");
        }
        StringBuilder sb = new StringBuilder();
        if (ICSI.charAt(ICSI.length() - 1) == ',') {
            ICSI.setCharAt(ICSI.length() - 1, '\"');
            sb.append(ICSI);
            sb.append(';');
        }
        if (IARI.charAt(IARI.length() - 1) == ',') {
            IARI.setCharAt(IARI.length() - 1, '\"');
            sb.append(IARI);
            sb.append(';');
        }
        if ((64 & features2) > 0) {
            sb.append("imdn-aggregation");
            sb.append(';');
        }
        if ((1024 & features2) > 0) {
            sb.append("+g.gsma.callcomposer");
            sb.append(';');
        }
        if ((8192 & features2) > 0) {
            sb.append("+g.gsma.rcs.botversion");
            sb.append("=");
            sb.append("\"#1\"");
            sb.append(";");
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ';') {
            sb.deleteCharAt(sb.length() - 1);
        }
        initialize(sb.toString());
    }

    private void initialize(String features2) {
        if (features2 == null) {
            features2 = "";
        }
        this.features.clear();
        if (features2.length() > 0) {
            for (String feature : features2.split(";")) {
                String[] nameValue = feature.split("=", 2);
                if (nameValue.length < 2) {
                    this.features.put(feature, new TagValueList(""));
                } else {
                    String name = nameValue[0];
                    String value = nameValue[1];
                    if (value.indexOf(34) != 0 || value.lastIndexOf(34) != value.length() - 1) {
                        throw new RuntimeException("Invalid feature tag value list format");
                    } else if (this.features.containsKey(name)) {
                        this.features.get(name).add(value);
                    } else {
                        this.features.put(name, new TagValueList(value));
                    }
                }
            }
        }
    }

    private void addFeature(Capability capability) {
        if (capability.features.size() > 0) {
            for (Map.Entry<String, TagValueList> entry : capability.features.entrySet()) {
                String key = entry.getKey();
                TagValueList value = entry.getValue();
                if (this.features.containsKey(key)) {
                    this.features.get(key).add(value);
                } else {
                    this.features.put(key, value);
                }
            }
        }
    }

    private void removeFeature(Capability capability) {
        if (capability.features.size() > 0) {
            for (Map.Entry<String, TagValueList> entry : capability.features.entrySet()) {
                String key = entry.getKey();
                TagValueList tags = entry.getValue();
                if (this.features.containsKey(key)) {
                    this.features.get(key).remove(tags);
                }
            }
        }
    }

    public static class TagValueList implements Parcelable {
        public static final Parcelable.Creator<TagValueList> CREATOR = new Parcelable.Creator<TagValueList>() {
            public TagValueList createFromParcel(Parcel in) {
                return new TagValueList(in.readString());
            }

            public TagValueList[] newArray(int size) {
                return new TagValueList[size];
            }
        };
        private int hashCode = 0;
        /* access modifiers changed from: private */
        public LinkedHashSet<String> values = new LinkedHashSet<>();

        TagValueList(String values2) {
            initialize(values2);
        }

        public void add(TagValueList values2) {
            addValues(values2);
        }

        public void remove(TagValueList values2) {
            removeValues(values2);
        }

        public void add(String values2) {
            add(new TagValueList(values2));
        }

        public void remove(String values2) {
            remove(new TagValueList(values2));
        }

        public boolean contains(TagValueList otherValue) {
            Iterator it = otherValue.values.iterator();
            while (it.hasNext()) {
                if (!this.values.contains((String) it.next())) {
                    return false;
                }
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.values.size();
        }

        public Set<String> getTagValues() {
            return this.values;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(toString());
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (this.values.size() > 0) {
                builder.append('\"');
                Iterator<String> it = this.values.iterator();
                builder.append(it.next());
                while (it.hasNext()) {
                    builder.append(',');
                    builder.append(it.next());
                }
                builder.append('\"');
            }
            return builder.toString();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TagValueList)) {
                return false;
            }
            if (((TagValueList) obj).values.hashCode() == this.values.hashCode()) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.values.hashCode();
        }

        private void initialize(String values2) {
            if (values2.length() != 0) {
                this.values.addAll(Arrays.asList(values2.substring(1, values2.lastIndexOf(34)).split(",")));
            }
        }

        private void addValues(TagValueList valuesToAdd) {
            if (valuesToAdd.values.size() > 0) {
                this.values.addAll(valuesToAdd.values);
            }
        }

        private void removeValues(TagValueList valuesToRemove) {
            if (valuesToRemove.values.size() > 0) {
                this.values.removeAll(valuesToRemove.values);
            }
        }
    }
}
