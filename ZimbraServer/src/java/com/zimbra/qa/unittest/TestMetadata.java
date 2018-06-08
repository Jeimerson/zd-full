/*
 * 
 */
package com.zimbra.qa.unittest;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Metadata;

import junit.framework.TestCase;


public class TestMetadata 
extends TestCase {

    private static final String USER_NAME = "user1";
    private static final String METADATA_SECTION = TestMetadata.class.getSimpleName();
    
    public void setUp()
    throws Exception {
        cleanUp();
    }
    
    /**
     * Tests insert, update and delete operations for mailbox metadata.
     */
    public void testMetadata()
    throws Exception {
        ZimbraLog.test.info("Starting testMetadata");
        
        Mailbox mbox = TestUtil.getMailbox(USER_NAME);
        assertNull(mbox.getConfig(null, METADATA_SECTION));

        // Insert
        Metadata config = new Metadata();
        config.put("string", "mystring");
        mbox.setConfig(null, METADATA_SECTION, config);
        config = mbox.getConfig(null, METADATA_SECTION);
        assertEquals("mystring", config.get("string"));
        
        // Update
        config.put("long", 87);
        mbox.setConfig(null, METADATA_SECTION, config);
        config = mbox.getConfig(null, METADATA_SECTION);
        assertEquals(87, config.getLong("long"));
        assertEquals("mystring", config.get("string"));
        
        // Delete
        mbox.setConfig(null, METADATA_SECTION, null);
        assertNull(mbox.getConfig(null, METADATA_SECTION));
    }
    
    public void tearDown()
    throws Exception {
        cleanUp();
    }
    
    private void cleanUp()
    throws Exception {
        Mailbox mbox = TestUtil.getMailbox(USER_NAME);
        mbox.setConfig(null, METADATA_SECTION, null);
    }
}
