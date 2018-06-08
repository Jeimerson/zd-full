/*
 * 
 */
package com.zimbra.cs.account.soap;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.XMPPComponent;

/**
 * 
 */
public class SoapXMPPComponent extends XMPPComponent implements SoapEntry {
    
    SoapXMPPComponent(Element e, Provisioning prov) throws ServiceException {
        super(e.getAttribute(AdminConstants.A_NAME), e.getAttribute(AdminConstants.A_ID), SoapProvisioning.getAttrs(e), prov);
    }

    public void modifyAttrs(SoapProvisioning prov,
                            Map<String, ? extends Object> attrs,
                            boolean checkImmutable) throws ServiceException {
    // TODO Auto-generated method stub
    }

    public void reload(SoapProvisioning prov) throws ServiceException {
    // TODO Auto-generated method stub

    }

}
