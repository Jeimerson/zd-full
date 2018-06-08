/*
 * 
 */

/**
 * @overview
 * This file defines an IM address.
 *
 */

/**
 * 
 * @class
 * This class represents an IM address and is used to centralize the representation of IM addresses,
 * since they will be used in both contacts, IM and probably in a Zimlet that recognizes them in email.
 * 
 */
ZmImAddress = {

    IM_SERVICES : [
            { label: ZmMsg.zimbraTitle , value: "local" },
            { label: ZmMsg.yahoo       , value: "yahoo" },
            { label: ZmMsg.msn         , value: "msn" },
            { label: ZmMsg.aol         , value: "aol" },
            { label: ZmMsg.other       , value: "other" }
    ],

    REGEXP : [], // it's built at load-time below

    parse : function(addr) {
        var m = ZmImAddress.REGEXP.exec(addr);
        if (m) {
            return { service    : m[1],
                screenName : m[2] };
        }
                // undef if unknown
    },

    make : function(service, screenName) {
        var addr = service + "://" + screenName;
                // check if it's acceptable
        if (ZmImAddress.parse(addr)) {
            return addr;
        }
        return "";
    },

    display : function(id) {
        var addr = ZmImAddress.parse(id);
        if (addr) {
            var a = ZmImAddress.IM_SERVICES, i = 0, s;
            while (s = a[i++]) {
                if (s.value == addr.service)
                    break;
            }
            if (s)
                return addr.screenName + " (" + s.label + ")";
        }
        return id;
    }

};

// HACK for bug 23603
if (/^SmartZone/.test(ZmMsg.zimbraTitle)) {
        ZmImAddress.IM_SERVICES.splice(0, 1);
}

for (var i = 0; i < ZmImAddress.IM_SERVICES.length; ++i)
        ZmImAddress.REGEXP.push(ZmImAddress.IM_SERVICES[i].value);

ZmImAddress.REGEXP = new RegExp("^(" + ZmImAddress.REGEXP.join("|") + ")://([^\\s]+)$", "i");
