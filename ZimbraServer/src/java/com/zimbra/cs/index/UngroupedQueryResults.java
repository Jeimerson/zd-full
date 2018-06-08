/*
 * 
 */

/*
 * Created on Oct 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.zimbra.cs.index;

import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;

/*
 * Created on Nov 3, 2004
 *
 * UngroupedQueryResults which do NOT group (ie return parts or messages in whatever mix)
 */
class UngroupedQueryResults extends ZimbraQueryResultsImpl 
{
    ZimbraQueryResults mResults;
    
    UngroupedQueryResults(ZimbraQueryResults results, byte[] types, SortBy searchOrder, Mailbox.SearchResultMode mode) {
        super(types, searchOrder, mode);
        mResults = results;
    }
    
    public void resetIterator() throws ServiceException {
        mResults.resetIterator();
    }
    
    public ZimbraHit getNext() throws ServiceException
    {
        return mResults.getNext();
    }
    
    public ZimbraHit peekNext() throws ServiceException
    {
        return mResults.peekNext();
    }
    
    public void doneWithSearchResults() throws ServiceException {
        mResults.doneWithSearchResults();
    }

    public ZimbraHit skipToHit(int hitNo) throws ServiceException {
        return mResults.skipToHit(hitNo);
    }
    
    public List<QueryInfo> getResultInfo() { return mResults.getResultInfo(); }
    
    public int estimateResultSize() throws ServiceException { return mResults.estimateResultSize(); }
    
}
