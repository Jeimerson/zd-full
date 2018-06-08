/*
 * 
 */

/**
 * Singleton alert class that alerts the user by popping up a message on the desktop.
 * @class
 * @private
 */
ZmDesktopAlert = function() {
	if (window.webkitNotifications) {
		this.useWebkit = true;
	} else if (window.Notification) {
		this.useNotification = true;
	}
};

ZmDesktopAlert.prototype = new ZmAlert;
ZmDesktopAlert.prototype.constructor = ZmDesktopAlert;

ZmDesktopAlert.prototype.toString =
	function() {
		return "ZmDesktopAlert";
	};

ZmDesktopAlert.getInstance =
	function() {
		return ZmDesktopAlert.INSTANCE = ZmDesktopAlert.INSTANCE || new ZmDesktopAlert();
	};

/**
 * Returns text to show in a prefs page next to the checkbox to enable this type of alert.
 */
ZmDesktopAlert.prototype.getDisplayText =
function() {
	if (this.useWebkit || this.useNotification) {
		return ZmMsg.showPopup;
	}
};

ZmDesktopAlert.prototype.start =
function(title, message, sticky) {
	if (this.useWebkit) {
		var allowedCallback = this._showWebkitNotification.bind(this, title, message, sticky);
		this._checkWebkitPermission(allowedCallback);
	} else if (this.useNotification) {
		var notificationCallback = this._showNotification.bind(this, title, message, sticky);
		this._checkNotificationPermission(notificationCallback);
	}
};

/* Checks if we have permission to use webkit notifications. If so, or when the user
 * grants permission, allowedCallback is called.
 */
ZmDesktopAlert.prototype._checkWebkitPermission =
function(allowedCallback) {
	var allowed = window.webkitNotifications.checkPermission() == 0;
	if (allowed) {
		allowedCallback();
	} else if (!ZmDesktopAlert.requestedPermission) {
		ZmDesktopAlert.requestedPermission = true; // Prevents multiple permission requests in one session.
		window.webkitNotifications.requestPermission(this._checkWebkitPermission.bind(this, allowedCallback));
	}
};

ZmDesktopAlert.prototype._showWebkitNotification =
function(title, message, sticky) {
	sticky = sticky || false;
	// Icon: I chose to use the favIcon because it's already overridable by skins.
	// It's a little ugly though.
	// change for bug#67359: Broken notification image in chrome browser
	// //var icon = window.favIconUrl;
	var icon = skin.hints.notificationBanner;
	var popup = window.webkitNotifications.createNotification(icon, title, message);
	popup.show();
	popup.onclick = function() {popup.cancel();};
	if (sticky) {
		if (!ZmDesktopAlert.notificationArray) {
			ZmDesktopAlert.notificationArray = [];
		}
		ZmDesktopAlert.notificationArray.push(popup);
	}
	else {
		// Close the popup after 5 seconds.
		setTimeout(popup.cancel.bind(popup), 5000);
	}
};

/* Checks if we have permission to use the notification api. If so, or when the user
 * grants permission, allowedCallback is called.
 */
ZmDesktopAlert.prototype._checkNotificationPermission = function(allowedCallback) {
	var allowed = window.Notification.permission === 'granted';
	if (allowed) {
		allowedCallback();
	} else if (!ZmDesktopAlert.requestedPermission) {
		ZmDesktopAlert.requestedPermission = true; // Prevents multiple permission requests in one session.
		// Currently, cannot directly call requestPermission.  Re-test when Chrome 37 is released
		//window.Notification.requestPermission(this._checkNotificationPermission.bind(this, allowedCallback));
		var requestCallback = this._checkNotificationPermission.bind(this, allowedCallback);
		this.requestRequestPermission(requestCallback);
	}
};

// Chrome Notification only allows requesting permission in response to a user action, not a programmatic call.
// The issue may be fixed in Chrome 37, whenever that comes out.  See:
//   https://code.google.com/p/chromium/issues/detail?id=274284
ZmDesktopAlert.prototype.requestRequestPermission = function(requestCallback) {
	var msgDialog = appCtxt.getYesNoMsgDialog();
	var callback = 	this._doRequestPermission.bind(this, msgDialog, requestCallback);
	msgDialog.registerCallback(DwtDialog.YES_BUTTON, callback);
	msgDialog.setMessage(ZmMsg.notificationPermission, DwtMessageDialog.INFO_STYLE);
	msgDialog.popup();
};

ZmDesktopAlert.prototype._doRequestPermission = function(msgDialog, requestCallback) {
	msgDialog.popdown();
	window.Notification.requestPermission(requestCallback);
};

ZmDesktopAlert.prototype._showNotification = function(title, message, sticky) {
	var icon = skin.hints.notificationBanner;

	var popup = new Notification(title, { body: message, icon: icon});
	//popup.show();
	popup.onclick = function() {popup.close();};
	if (sticky) {
		if (!ZmDesktopAlert.notificationArray) {
			ZmDesktopAlert.notificationArray = [];
		}
		ZmDesktopAlert.notificationArray.push(popup);
	}
	else {
		// Close the popup after 5 seconds.
		setTimeout(popup.close.bind(popup), 5000);
	}
};

/**
 * Closes desktop notification if any during onbeforeunload event
 */
ZmDesktopAlert.closeNotification =
function() {
	var notificationArray = ZmDesktopAlert.notificationArray,
		popup;

	if (notificationArray) {
		while (popup = notificationArray.pop()) {
			//notifications may be already closed by the user go for try catch
			try {
				popup.cancel();
			}
			catch (e) {
			}
		}
	}
};
