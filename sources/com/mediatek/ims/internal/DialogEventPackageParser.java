package com.mediatek.ims.internal;

import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParserException;

public interface DialogEventPackageParser {
    DialogInfo parse(InputStream inputStream) throws XmlPullParserException, IOException;
}
