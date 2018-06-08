/*
 * 
 */

/*
 * Created on 2004. 7. 21.
 */
package com.zimbra.cs.redolog.op;

import java.io.IOException;
import java.util.Arrays;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailItem.TargetConstraint;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class AlterItemTag extends RedoableOp {

    private int[] mIds;
    private byte mType;
    private int mTagId;
    private boolean mTagged;
    private String mConstraint;

    public AlterItemTag() {
        mType = MailItem.TYPE_UNKNOWN;
        mTagId = UNKNOWN_ID;
        mTagged = false;
        mConstraint = null;
    }

    public AlterItemTag(int mailboxId, int[] ids, byte type, int tagId, boolean tagged, TargetConstraint tcon) {
        setMailboxId(mailboxId);
        mIds = ids;
        mType = type;
        mTagId = tagId;
        mTagged = tagged;
        mConstraint = (tcon == null ? null : tcon.toString());
    }

    @Override public int getOpCode() {
        return OP_ALTER_ITEM_TAG;
    }

    @Override protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("ids=");
        sb.append(Arrays.toString(mIds)).append(", type=").append(mType);
        sb.append(", tag=").append(mTagId).append(", tagged=").append(mTagged);
        if (mConstraint != null)
            sb.append(", constraint=").append(mConstraint);
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        boolean hasConstraint = mConstraint != null;
        out.writeInt(-1);
        out.writeByte(mType);
        out.writeInt(mTagId);
        out.writeBoolean(mTagged);
        out.writeBoolean(hasConstraint);
        if (hasConstraint)
            out.writeUTF(mConstraint);
        out.writeInt(mIds == null ? 0 : mIds.length);
        if (mIds != null)
            for (int i = 0; i < mIds.length; i++)
                out.writeInt(mIds[i]);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        int id = in.readInt();
        if (id > 0)
            mIds = new int[] { id };
        mType = in.readByte();
        mTagId = in.readInt();
        mTagged = in.readBoolean();
        if (in.readBoolean())
            mConstraint = in.readUTF();
        if (id <= 0) {
            mIds = new int[in.readInt()];
            for (int i = 0; i < mIds.length; i++)
                mIds[i] = in.readInt();
        }
    }

    @Override public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());

        TargetConstraint tcon = null;
        if (mConstraint != null)
            try {
                tcon = TargetConstraint.parseConstraint(mbox, mConstraint);
            } catch (ServiceException e) {
                mLog.warn(e);
            }

            mbox.alterTag(getOperationContext(), mIds, mType, mTagId, mTagged, tcon);
    }
}
