/*
 * 
 */

/*
 * Created on 2005. 1. 12.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.zimbra.cs.redolog.op;

import java.io.File;
import java.io.IOException;

import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

/**
 * @author jhahm
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Rollover extends RedoableOp {

    long mSeq;
    String mFilename;

    public Rollover() {
        mSeq = 0;
    }

    public Rollover(File logfile, long seq) {
        mSeq = seq;
    	mFilename = logfile.getName();
    }

    public long getSequence() {
    	return mSeq;
    }

    public String getFilename() {
    	return mFilename;
    }

	public int getOpCode() {
		return OP_ROLLOVER;
	}

	/* (non-Javadoc)
	 * @see com.zimbra.cs.redolog.op.RedoableOp#redo()
	 */
	public void redo() throws Exception {
        // nothing to do
	}

	/* (non-Javadoc)
	 * @see com.zimbra.cs.redolog.op.RedoableOp#getPrintableData()
	 */
	protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("seq=");
        sb.append(mSeq);
        sb.append(", filename=").append(mFilename);
        return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.zimbra.cs.redolog.op.RedoableOp#serializeData(java.io.RedoLogOutput)
	 */
	protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeLong(mSeq);
        out.writeUTF(mFilename);
	}

	/* (non-Javadoc)
	 * @see com.zimbra.cs.redolog.op.RedoableOp#deserializeData(java.io.RedoLogInput)
	 */
	protected void deserializeData(RedoLogInput in) throws IOException {
        mSeq = in.readLong();
        mFilename = in.readUTF();
	}
}
