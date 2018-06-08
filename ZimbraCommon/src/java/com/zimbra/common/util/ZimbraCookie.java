/*
 * 
 */
package com.zimbra.common.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpOnlyCookie;

import com.zimbra.common.localconfig.LC;

public class ZimbraCookie {

    public static final String COOKIE_ZM_AUTH_TOKEN       = "ZM_AUTH_TOKEN";
    public static final String COOKIE_ZM_ADMIN_AUTH_TOKEN = "ZM_ADMIN_AUTH_TOKEN";
    
    public static String PATH_ROOT = "/";

    public static String authTokenCookieName(boolean isAdminReq) {
        return isAdminReq? 
                COOKIE_ZM_ADMIN_AUTH_TOKEN : 
                COOKIE_ZM_AUTH_TOKEN;
    }
    
    /**
     * set cookie domain and path for the cookie going back to the browser
     * 
     * @param cookie
     * @param path
     */
    public static void setAuthTokenCookieDomainPath(Cookie cookie, String path) {
        if (LC.zimbra_authtoken_cookie_domain.value().length() > 0) {
            cookie.setDomain(LC.zimbra_authtoken_cookie_domain.value());
        }
        
        cookie.setPath(path);
    }
    
    public static boolean secureCookie(HttpServletRequest request) {
        return "https".equalsIgnoreCase(request.getScheme());
    }
    
    public static void addHttpOnlyCookie(HttpServletResponse response, String name, String value, 
            String path, Integer maxAge, boolean secure) {
        addCookie(response, name, value, path, maxAge, true, secure);
    }
    
    private static void addCookie(HttpServletResponse response, String name, String value, 
            String path, Integer maxAge, boolean httpOnly, boolean secure) {
        Cookie cookie;
        
        if (httpOnly) {
            // jetty-6 only: jetty specific HttpOnlyCookie class that extends Cookie.
            cookie = new HttpOnlyCookie(name, value); 
        } else {
            cookie = new Cookie(name, value);
        }

        if (maxAge != null) {
            cookie.setMaxAge(maxAge.intValue());
        }
        ZimbraCookie.setAuthTokenCookieDomainPath(cookie, ZimbraCookie.PATH_ROOT);

        cookie.setSecure(secure);
        response.addCookie(cookie);
    }

    public static void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        setAuthTokenCookieDomainPath(cookie, PATH_ROOT);
        response.addCookie(cookie);
    }

}