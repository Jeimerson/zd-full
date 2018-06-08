/*
 * 
 */

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.store.file.Volume;
import com.zimbra.soap.ZimbraSoapContext;

public class CreateVolume extends AdminDocumentHandler {

    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext lc = getZimbraSoapContext(context);

        Server localServer = Provisioning.getInstance().getLocalServer();
        checkRight(lc, context, localServer, Admin.R_manageVolume);
        
        Element eVol = request.getElement(AdminConstants.E_VOLUME);
        String name  = eVol.getAttribute(AdminConstants.A_VOLUME_NAME);
        String path  = eVol.getAttribute(AdminConstants.A_VOLUME_ROOTPATH);
        short type   = (short) eVol.getAttributeLong(AdminConstants.A_VOLUME_TYPE);

        // TODO: These "bits" parameters are ignored inside Volume.create() for now.
//        short mgbits = (short) eVol.getAttributeLong(AdminService.A_VOLUME_MGBITS);
//        short mbits  = (short) eVol.getAttributeLong(AdminService.A_VOLUME_MBITS);
//        short fgbits = (short) eVol.getAttributeLong(AdminService.A_VOLUME_FGBITS);
//        short fbits  = (short) eVol.getAttributeLong(AdminService.A_VOLUME_FBITS);

        boolean compressBlobs = eVol.getAttributeBool(AdminConstants.A_VOLUME_COMPRESS_BLOBS);
        long compressionThreshold = eVol.getAttributeLong(AdminConstants.A_VOLUME_COMPRESSION_THRESHOLD);
//        Volume vol = Volume.create(Volume.ID_AUTO_INCREMENT, type, name, path,
//                                   mgbits, mbits, fgbits, fbits, compressBlobs, compressionThreshold);
        Volume vol = Volume.create(Volume.ID_AUTO_INCREMENT, type, name, path,
                                   (short) 0, (short) 0, (short) 0, (short) 0,
                                   compressBlobs, compressionThreshold);

        Element response = lc.createElement(AdminConstants.CREATE_VOLUME_RESPONSE);
        GetVolume.addVolumeElement(response, vol);
        return response;
    }
    
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_manageVolume);
    }
}
