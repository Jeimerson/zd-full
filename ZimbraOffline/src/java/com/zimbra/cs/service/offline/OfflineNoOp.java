/*
 * 
 */
package com.zimbra.cs.service.offline;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.offline.OfflineSyncManager;
import com.zimbra.cs.service.mail.NoOp;
import com.zimbra.soap.SoapServlet;

public class OfflineNoOp extends NoOp {

    private static final String TIME_KEY = "ZDNoOpStartTime";

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {
		if (!context.containsKey(SoapServlet.IS_RESUMED_REQUEST))
			OfflineSyncManager.getInstance().clientPing();
        boolean wait = request.getAttributeBool(MailConstants.A_WAIT, false);
        long start = System.currentTimeMillis();
        HttpServletRequest servletRequest = (HttpServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
        if (!context.containsKey(SoapServlet.IS_RESUMED_REQUEST)) {
            servletRequest.setAttribute(TIME_KEY, start);
        }
		Element response = super.handle(request, context);
		response.addAttribute(MailConstants.A_WAIT, wait);
		response.addAttribute(MailConstants.A_TIME, System.currentTimeMillis()-(Long) servletRequest.getAttribute(TIME_KEY));
		return response;
	}
}
