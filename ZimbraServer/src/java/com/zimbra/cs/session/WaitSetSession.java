/*
 * 
 */
package com.zimbra.cs.session;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.service.util.SyncToken;

/**
 * A session subclass which is used when an external entity wants to listen on a 
 * particular account for changes to the mailbox, but doesn't care what exactly
 * those changes are: presumably because the caller is going to use some sort
 * of other channel (e.g. IMAP) for synchronizing with the Mailbox.
 */
public class WaitSetSession extends Session {
    SomeAccountsWaitSet mWs = null;
    int mInterestMask;
    int mHighestChangeId;
    SyncToken mSyncToken;

    WaitSetSession(SomeAccountsWaitSet ws, String accountId, int interestMask, SyncToken lastKnownSyncToken) {
        super(accountId, Session.Type.WAITSET);
        mWs = ws;
        mInterestMask = interestMask;
        mSyncToken = lastKnownSyncToken;
    }

    @Override
    protected boolean isMailboxListener() {
        return true;
    }

    @Override
    protected boolean isRegisteredInCache() {
        return false;
    }
    
    void update(int interestMask, SyncToken lastKnownSyncToken) {
        mInterestMask = interestMask;
        mSyncToken = lastKnownSyncToken;
        if (mSyncToken != null) {
            // if the sync token is non-null, then we want
            // to signal IFF the passed-in changeId is after the current
            // synctoken...and we want to cancel the existing signalling
            // if the new synctoken is up to date with the mailbox
            int mboxHighestChange = getMailbox().getLastChangeID();
            if (mboxHighestChange > mHighestChangeId)
                mHighestChangeId = mboxHighestChange;
            if (mSyncToken.after(mHighestChangeId))
                mWs.unsignalDataReady(this);
            else 
                mWs.signalDataReady(this);
        }
    }

    @Override
    protected void cleanup() { 
        mWs.cleanupSession(this);
    }

    @Override
    protected long getSessionIdleLifetime() { return 0; }

    @Override
    public void notifyPendingChanges(PendingModifications pns, int changeId, Session source) {
        boolean trace = ZimbraLog.session.isTraceEnabled();
        if (trace)
            ZimbraLog.session.trace("Notifying WaitSetSession: change id=" + changeId +
                    ", highest change id=" + mHighestChangeId + ", sync token=" + mSyncToken);
        if (changeId > mHighestChangeId) {
            mHighestChangeId = changeId;
        }
        if (mSyncToken != null && mSyncToken.after(mHighestChangeId)) {
            if (trace) ZimbraLog.session.trace("Not signaling waitset; sync token is later than highest change id");
            return; // don't signal, sync token stopped us
        }
        if ((mInterestMask & pns.changedTypes) != 0) {
            if (trace) ZimbraLog.session.trace("Signaling waitset");
            mWs.signalDataReady(this);
        } else {
            if (trace) ZimbraLog.session.trace("Not signaling waitset; waitset is not interested in change type");
        }
        if (trace) ZimbraLog.session.trace("WaitSetSession.notifyPendingChanges done");
    }
}
