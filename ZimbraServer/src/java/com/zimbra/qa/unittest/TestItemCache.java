/*
 * 
 */

package com.zimbra.qa.unittest;

import java.util.List;

import junit.framework.TestCase;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.stats.ZimbraPerf;
import com.zimbra.common.util.ZimbraLog;

/**
 * @author bburtin
 */
public class TestItemCache extends TestCase
{
    private Mailbox mMbox;
    private Account mAccount;
    
    protected void setUp()
    throws Exception {
        ZimbraLog.test.debug("TestTags.setUp()");
        super.setUp();

        mAccount = TestUtil.getAccount("user1");
        mMbox = MailboxManager.getInstance().getMailboxByAccount(mAccount);
    }

    /**
     * Re-gets the same message 10 times and makes sure we don't hit the database.
     */
    public void testCacheHit()
    throws Exception {
        ZimbraLog.test.debug("testCacheHit");

        List messages = mMbox.getItemList(null, MailItem.TYPE_MESSAGE);
        assertTrue("No messages found", messages.size() > 0);
        Message msg = (Message) messages.get(0);
        mMbox.getItemById(null, msg.getId(), msg.getType());
        
        int prepareCount = ZimbraPerf.getPrepareCount();
        for (int i = 1; i <= 10; i++) {
            mMbox.getItemById(null, msg.getId(), msg.getType());
        }
        
        prepareCount = ZimbraPerf.getPrepareCount() - prepareCount;
        assertEquals("Detected unexpected SQL statements.",
            0, prepareCount);
    }
}
