/*
 * 
 */
package com.zimbra.zimbrasync.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.mailbox.ChangeTrackingMailbox;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.session.PendingModifications.Change;
import com.zimbra.zimbrasync.client.ChangeTracker;
import com.zimbra.zimbrasync.client.ExchangeItemMapping;

public class OfflineChangeTracker extends ChangeTracker {
    
    private Set<Integer> allDeletes; //don't init here as super constructor will call into findClientChanges()

    public OfflineChangeTracker(DataSource ds, Map<Integer, ExchangeFolderMapping> folderMappingsByClientId) throws ServiceException {
        super(ds, folderMappingsByClientId);
    }
    
    @Override
    protected void findClientChanges() throws ServiceException {
        ChangeTrackingMailbox ctmbox = (ChangeTrackingMailbox)mbox;
        Map<Integer, Pair<Integer, Integer>> changes = ctmbox.getChangeMasksAndFolders(getContext(false));
        addItemMappings(ExchangeItemMapping.getMappings(ds, changes.keySet()));
        for (Iterator<Entry<Integer, Pair<Integer, Integer>>> i = changes.entrySet().iterator(); i.hasNext();) {
            Entry<Integer, Pair<Integer, Integer>> entry = i.next();
            int id = entry.getKey();
            int mask = entry.getValue().getFirst();
            int folderId = entry.getValue().getSecond();
            
            if ((mask & Change.MODIFIED_CONFLICT) != 0) {
                if (folderId != Mailbox.ID_FOLDER_TRASH) //a deleted draft message can be in Trash
                    addClientAdd(folderId, id);
            } else {
                if ((mask & Change.MODIFIED_FOLDER) != 0) {
                    int oldFldId = mappingByClientId.get(id).getFolderId();
                    if (oldFldId != folderId) {
                        ExchangeItemMapping eim = mappingByClientId.get(id);
                        assert eim != null : "id=" + id; //this is not a new item
                        String remoteId = eim.getRemoteId();
                        String remoteSrcFldId = eim.getRemoteParentId();
                        String remoteDstFldId = null;
                        ExchangeFolderMapping efm = folderMappingsByClientId.get(folderId);
                        if (efm != null) {
                            remoteDstFldId = efm.getRemoteId();
                        } else { //the dst folder is new and hasn't been pushed up to server yet
                            ZimbraLog.xsync.debug("item=%d moved to new folder (id=%d) that hasn't been pushed to server", id, folderId);
                        }
                        addItemMove(new TrackerItemMove(remoteId, remoteSrcFldId, remoteDstFldId, id, oldFldId, folderId));
                    } else { //somehow item was moved but maybe moved back to the previous folder
                        clearItemMoved(id, folderId); //just clear the bit if item is still in there
                    }
                }
                if ((mask & ~Change.MODIFIED_FOLDER) != 0)
                    addClientChange(folderId, id);
            }
        }
        
        List<Integer> tombstones = ctmbox.getTombstones(0).getAll();
        addItemMappings(ExchangeItemMapping.getMappings(ds, tombstones));
        for (int id : tombstones) {
            ExchangeItemMapping eim = mappingByClientId.get(id);
            if (eim != null) {
                addClientDelete(eim.getFolderId(), id);
                if (allDeletes == null)
                    allDeletes = new HashSet<Integer>();
                allDeletes.add(id);
            }
        }
    }
    
    @Override
    protected void clearItemMoved(int id, int folderId) throws ServiceException {
        ChangeTrackingMailbox ctmbox = (ChangeTrackingMailbox)mbox;
        synchronized (ctmbox) {
            MailItem item = ctmbox.getItemById(getContext(false), id, MailItem.TYPE_UNKNOWN);
            if (folderId == item.getFolderId()) {
                int mask = ctmbox.getChangeMask(getContext(false), id, item.getType());
                mask &= ~Change.MODIFIED_FOLDER;
                ctmbox.setChangeMask(getContext(false), id, item.getType(), mask);
            }
        }
    }
    
    @Override
    protected void clearItemAdded(int id, int changeId) throws ServiceException {
        ChangeTrackingMailbox ctmbox = (ChangeTrackingMailbox)mbox;
        synchronized (ctmbox) {
            int mask = 0;
            MailItem item = ctmbox.getItemById(getContext(false), id, MailItem.TYPE_UNKNOWN);
            if (item.getModifiedSequence() != changeId) {
                mask = ctmbox.getChangeMask(getContext(false), id, item.getType());
                mask &= ~Change.MODIFIED_CONFLICT;
            }
            ctmbox.setChangeMask(getContext(false), id, item.getType(), mask);
        }
    }
    
    @Override
    protected void clearItemChanged(int id, int changeId) throws ServiceException {
        ChangeTrackingMailbox ctmbox = (ChangeTrackingMailbox)mbox;
        synchronized (ctmbox) {
            MailItem item = ctmbox.getItemById(getContext(false), id, MailItem.TYPE_UNKNOWN);
            if (item.getModifiedSequence() == changeId) {
                ctmbox.setChangeMask(getContext(false), id, item.getType(), 0); //TODO: well we may have to leave some bits for future implementaiton, like if we don't sync flags initially
            }
        }
    }
    
    @Override
    protected void clearItemDeleted(int id) throws ServiceException {
        allDeletes.remove(id);
    }
    
    @Override
    public void folderSyncComplete() throws ServiceException {
        //by now we may have deleted some folders, so clean up the deletes
        for (Iterator<Entry<Integer, List<Integer>>> i = deletesByFoderId.entrySet().iterator(); i.hasNext();) {
            Entry<Integer, List<Integer>> entry = i.next();
            if (!folderMappingsByClientId.containsKey(entry.getKey())) { //folder got deleted, either from client or from server
                for (int id : entry.getValue())
                    allDeletes.remove(id);
                i.remove();
            }
        }
    }
    
    @Override
    public void syncComplete() throws ServiceException {
        if (deletesByFoderId.size() > 0) {
            if (allDeletes.size() == 0) {
                ChangeTrackingMailbox ctmbox = (ChangeTrackingMailbox)mbox;
                ctmbox.clearTombstones(getContext(false), cutoffChangeId);
            } else {
                ZimbraLog.xsync.warn("%d client deletes not acknowledged: %s", allDeletes.size(), allDeletes.toString());
            }
        }
    }
}
