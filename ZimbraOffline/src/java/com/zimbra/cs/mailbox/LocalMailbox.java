/*
 * 
 */
package com.zimbra.cs.mailbox;

import java.util.HashSet;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning.AccountBy;
import com.zimbra.cs.account.offline.OfflineProvisioning;
import com.zimbra.cs.db.DbOfflineMailbox;
import com.zimbra.cs.db.DbPool.Connection;
import com.zimbra.cs.mailbox.ChangeTrackingMailbox.TracelessContext;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.redolog.op.CreateFolder;
import com.zimbra.cs.redolog.op.DeleteMailbox;

public class LocalMailbox extends DesktopMailbox {
    public LocalMailbox(MailboxData data) throws ServiceException {
        super(data);
    }
    
    @Override synchronized void ensureSystemFolderExists() throws ServiceException {
        super.ensureSystemFolderExists();
        try {
            getFolderById(ID_FOLDER_NOTIFICATIONS);
        } catch (NoSuchItemException e) {
            CreateFolder redo = new CreateFolder(getId(), NOTIFICATIONS_PATH,
                ID_FOLDER_USER_ROOT, Folder.FOLDER_IS_IMMUTABLE,
                MailItem.TYPE_UNKNOWN, 0, MailItem.DEFAULT_COLOR_RGB, null);
            
            redo.setFolderId(ID_FOLDER_NOTIFICATIONS);
            redo.start(System.currentTimeMillis());
            createFolder(new TracelessContext(redo), NOTIFICATIONS_PATH,
                ID_FOLDER_USER_ROOT, Folder.FOLDER_IS_IMMUTABLE,
                MailItem.TYPE_UNKNOWN, 0, MailItem.DEFAULT_COLOR_RGB, null);
        }
        OfflineProvisioning prov = OfflineProvisioning.getOfflineInstance();
        for (String accountId : prov.getAllAccountIds()) {
            Account acct = prov.get(AccountBy.id, accountId);
            if (acct == null || prov.isGalAccount(acct) || prov.isMountpointAccount(acct))
                continue;
            try {
                getFolderByName(null, ID_FOLDER_NOTIFICATIONS, accountId);
            } catch (NoSuchItemException e) {
                createMountpoint(null, ID_FOLDER_NOTIFICATIONS, accountId,
                    accountId, ID_FOLDER_USER_ROOT,
                    MailItem.TYPE_UNKNOWN, 0, MailItem.DEFAULT_COLOR_RGB);
            }
        }
    }

    @Override protected synchronized void initialize() throws ServiceException {
        super.initialize();
        getCachedItem(ID_FOLDER_CALENDAR).setColor(new MailItem.Color((byte)8));
        Folder.create(ID_FOLDER_NOTIFICATIONS, this,
            getFolderById(ID_FOLDER_USER_ROOT), NOTIFICATIONS_PATH,
            Folder.FOLDER_IS_IMMUTABLE, MailItem.TYPE_UNKNOWN, 0,
            MailItem.DEFAULT_COLOR_RGB, null, null);
    }

    Set<Folder> getAccessibleFolders(short rights) throws ServiceException {
        Set<Folder> accessable = super.getAccessibleFolders(rights);
        Set<Folder> visible = new HashSet<Folder>();

        for (Folder folder : accessable == null ? getFolderById(
            ID_FOLDER_ROOT).getSubfolderHierarchy() : accessable) {
            if (!(folder instanceof Mountpoint))
                visible.add(folder);
        }
        return visible;
    }
    
    public synchronized void forceDeleteMailbox(Mailbox mbox) throws ServiceException {
        DeleteMailbox redoRecorder = new DeleteMailbox(mbox.getId());
        boolean success = false;
        try {
            beginTransaction("deleteMailbox", null, redoRecorder);
            redoRecorder.log();

            try {
                // remove all the relevant entries from the database
                Connection conn = getOperationConnection();
                ZimbraLog.mailbox.info("attempting to remove the zimbra.mailbox row for id "+mbox.getId());
                DbOfflineMailbox.forceDeleteMailbox(conn, mbox.getId());    
                success = true;
            } finally {
                // commit the DB transaction before touching the store!  (also ends the operation)
                endTransaction(success);
            }

            if (success) {
                // remove all traces of the mailbox from the Mailbox cache
                //   (so anyone asking for the Mailbox gets NO_SUCH_MBOX or creates a fresh new empty one with a different id)
                MailboxManager.getInstance().markMailboxDeleted(mbox);
            }
        } finally {
            if (success)
                redoRecorder.commit();
            else
                redoRecorder.abort();
        }
    }

    @Override
    public boolean isImmutableSystemFolder(int folderId) {
        if (folderId == ID_FOLDER_NOTIFICATIONS) {
            return true;
        } else {
            return false;
        }
    }
    
}
