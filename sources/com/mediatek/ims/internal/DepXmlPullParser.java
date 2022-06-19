package com.mediatek.ims.internal;

import android.telephony.Rlog;
import android.util.Xml;
import com.mediatek.ims.internal.DialogInfo;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DepXmlPullParser implements DialogEventPackageParser {
    private static final String namespace = null;

    public DialogInfo parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            parser.setInput(in, (String) null);
            parser.nextTag();
            return readDialogInfo(parser);
        } finally {
            in.close();
        }
    }

    private DialogInfo readDialogInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
        DialogInfo dialogInfo = new DialogInfo();
        parser.require(2, namespace, "dialog-info");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if (parser.getName().equals("dialog")) {
                    dialogInfo.addDialog(readDialog(parser));
                } else {
                    skip(parser);
                }
            }
        }
        return dialogInfo;
    }

    private DialogInfo.Dialog readDialog(XmlPullParser parser) throws XmlPullParserException, IOException {
        DialogInfo.Local local = null;
        parser.require(2, namespace, "dialog");
        int dialogId = Integer.parseInt(parser.getAttributeValue((String) null, "id"));
        boolean exclusive = true;
        String state = "";
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals("sa:exclusive")) {
                    exclusive = Boolean.valueOf(readText(parser)).booleanValue();
                } else if (name.equals("state")) {
                    state = readText(parser);
                } else if (name.equals("local")) {
                    local = readLocal(parser);
                } else {
                    skip(parser);
                }
            }
        }
        return new DialogInfo.Dialog(dialogId, exclusive, state, local);
    }

    private DialogInfo.Local readLocal(XmlPullParser parser) throws XmlPullParserException, IOException {
        DialogInfo.Local local = new DialogInfo.Local();
        parser.require(2, namespace, "local");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals("identity")) {
                    local.setIdentity(readText(parser));
                } else if (name.equals("target")) {
                    local.setTargetUri(parser.getAttributeValue((String) null, "uri"));
                    readTargetParamAttributesToLocal(parser, local);
                } else if (name.equals("mediaAttributes")) {
                    local.addMediaAttribute(readMediaAttributes(parser));
                } else if (name.equals("param")) {
                    local.setParam(readParam(parser));
                    Rlog.d("DEP Parser", "read param from Local()");
                } else {
                    skip(parser);
                }
            }
        }
        return local;
    }

    private void readTargetParamAttributesToLocal(XmlPullParser parser, DialogInfo.Local local) throws XmlPullParserException, IOException {
        parser.require(2, namespace, "target");
        Rlog.d("DEP Parser", "readTargetParamAttributesToLocal()");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if (parser.getName().equals("param")) {
                    local.setParam(readParam(parser));
                } else {
                    skip(parser);
                }
            }
        }
    }

    private DialogInfo.MediaAttribute readMediaAttributes(XmlPullParser parser) throws XmlPullParserException, IOException {
        String mediaType = "";
        String mediaDirection = "";
        boolean port0 = false;
        parser.require(2, namespace, "mediaAttributes");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals("mediaType")) {
                    mediaType = readText(parser);
                } else if (name.equals("mediaDirection")) {
                    mediaDirection = readText(parser);
                } else if (name.equals("port0")) {
                    port0 = true;
                    skip(parser);
                } else {
                    skip(parser);
                }
            }
        }
        return new DialogInfo.MediaAttribute(mediaType, mediaDirection, port0);
    }

    private DialogInfo.Param readParam(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, namespace, "param");
        String pname = parser.getAttributeValue((String) null, "pname");
        String pval = parser.getAttributeValue((String) null, "pval");
        skip(parser);
        return new DialogInfo.Param(pname, pval);
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.next() != 4) {
            return "";
        }
        String result = parser.getText();
        parser.nextTag();
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() == 2) {
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case 2:
                        depth++;
                        break;
                    case 3:
                        depth--;
                        break;
                }
            }
            return;
        }
        throw new IllegalStateException();
    }
}
