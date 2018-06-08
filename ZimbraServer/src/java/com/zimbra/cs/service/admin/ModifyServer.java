/*
 * 
 */

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.ServerBy;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.util.Config;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class ModifyServer extends AdminDocumentHandler {

    private static final String[] TARGET_SERVER_PATH = new String[] { AdminConstants.E_ID };
    protected String[] getProxiedServerPath()  { return TARGET_SERVER_PATH; }

    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        String id = request.getAttribute(AdminConstants.E_ID);
        Map<String, Object> attrs = AdminService.getAttrs(request);
        
        Server server = prov.get(ServerBy.id, id);
        if (server == null)
            throw AccountServiceException.NO_SUCH_SERVER(id);
        
        checkRight(zsc, context, server, attrs);

        // pass in true to checkImmutable
        prov.modifyAttrs(server, attrs, true);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                new String[] {"cmd", "ModifyServer","name", server.getName()}, attrs));

        // If updating user service enable flag on local server, we have to 
        // tell Config class about it.
        if (attrs.containsKey(Provisioning.A_zimbraUserServicesEnabled)) {
            Server localServer = Provisioning.getInstance().getLocalServer();
            if (server.equals(localServer))
                Config.enableUserServices(server.getBooleanAttr(Provisioning.A_zimbraUserServicesEnabled, true));
        }

        Element response = zsc.createElement(AdminConstants.MODIFY_SERVER_RESPONSE);
        GetServer.encodeServer(response, server);
        return response;
    }
    
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add(String.format(AdminRightCheckPoint.Notes.MODIFY_ENTRY, 
                Admin.R_modifyServer.getName(), "server"));
    }
}
