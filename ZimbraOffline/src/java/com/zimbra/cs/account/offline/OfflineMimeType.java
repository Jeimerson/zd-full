/*
 * 
 */
package com.zimbra.cs.account.offline;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.zimbra.cs.mime.MimeTypeInfo;

class OfflineMimeType implements MimeTypeInfo {
    private String mTypes[], mHandler;
    private Set<String> mFileExtensions;
    private boolean mIndexed;

    private OfflineMimeType(String type, String handler, boolean index, String[] fext) {
        mTypes = new String[] { type };  mHandler = handler;  mIndexed = index;
        mFileExtensions = new TreeSet<String>();
        if (fext != null) {
            for (String ext : fext) {
                mFileExtensions.add(ext.toLowerCase());
            }
        }
    }

    public String[] getMimeTypes()           { return mTypes; }
    public String getExtension()         { return null; }
    public String getHandlerClass()      { return mHandler; }
    public boolean isIndexingEnabled()   { return mIndexed; }
    public String getDescription()       { return null; }
    public Set<String> getFileExtensions()  { return mFileExtensions; }
    public int getPriority()             { return 0; }

    static List<MimeTypeInfo> instantiateAll() {
        // just hardcode 'em for now...
        List<MimeTypeInfo> infos = new ArrayList<MimeTypeInfo>();
        infos.add(new OfflineMimeType("text/plain",     "TextPlainHandler",     true, new String[] { "txt", "text" } ));
        infos.add(new OfflineMimeType("text/html",      "TextHtmlHandler",      true, new String[] { "htm", "html" } ));
        infos.add(new OfflineMimeType("text/calendar",  "TextCalendarHandler",  true, new String[] { "ics", "vcs"} ));
        infos.add(new OfflineMimeType("message/rfc822", "MessageRFC822Handler", true, new String[] { } ));
        infos.add(new OfflineMimeType("text/enriched",  "TextEnrichedHandler",  true, new String[] { "txe" } ));
        infos.add(new OfflineMimeType("all",            "UnknownTypeHandler",   true, new String[] { } ));
        return infos;
    }
}
