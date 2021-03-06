/*
 * 
 */

/**
 * @overview
 * This file contains the portal controller class.
 */

/**
 * Creates the portal controller.
 * @class
 * This class represents the portal controller.
 * 
 * @param {DwtComposite}	container	the containing element
 * @param	{ZmPortalApp}	app			the application
 * 
 * @extends		ZmListController
 */
ZmPortalController = function(container, app) {
	if (arguments.length == 0) { return; }
	ZmListController.call(this, container, app);

    // TODO: Where does this really belong?
    ZmOperation.registerOp(ZmId.OP_PAUSE_TOGGLE, {textKey:"pause", image:"Pause", style: DwtButton.TOGGLE_STYLE});

    this._listeners[ZmOperation.REFRESH] = new AjxListener(this, this._refreshListener);
    this._listeners[ZmOperation.PAUSE_TOGGLE] = new AjxListener(this, this._pauseListener);
}
ZmPortalController.prototype = new ZmListController;
ZmPortalController.prototype.constructor = ZmPortalController;

/**
 * Returns a string representation of the object.
 * 
 * @return		{String}		a string representation of the object
 */
ZmPortalController.prototype.toString = function() {
	return "ZmPortalController";
};

//
// Public methods
//

ZmPortalController.prototype.show = function() {
	ZmListController.prototype.show.call(this);
	this._setup(this._currentView);

	var elements = new Object();
	elements[ZmAppViewMgr.C_TOOLBAR_TOP] = this._toolbar[this._currentView];
	elements[ZmAppViewMgr.C_APP_CONTENT] = this._listView[this._currentView];
	this._setView({view:this._currentView, elements:elements, isAppView:true});
};

/**
 * Sets the paused flag for the portlets.
 * 
 * @param	{Boolean}	paused		if <code>true</code>, pause the portlets
 */
ZmPortalController.prototype.setPaused = function(paused) {
    var view = this._listView[this._currentView];
    var portletIds = view && view.getPortletIds();
    if (portletIds && portletIds.length > 0) {
        var portletMgr = appCtxt.getApp(ZmApp.PORTAL).getPortletMgr();
        for (var i = 0; i < portletIds.length; i++) {
            var portlet = portletMgr.getPortletById(portletIds[i]);
            portlet.setPaused(paused);
        }
    }
};

//
// Protected methods
//

ZmPortalController.prototype._defaultView = function() {
	return ZmId.VIEW_PORTAL;
};

ZmPortalController.prototype._getToolBarOps = function() {
	return [ ZmOperation.REFRESH /*, ZmOperation.PAUSE_TOGGLE*/ ];
};

ZmPortalController.prototype._createNewView = function(view) {
	return new ZmPortalView(this._container, this, this._dropTgt);
};

ZmPortalController.prototype._setViewContents = function(view) {
	this._listView[view].set();
};

// listeners

ZmPortalController.prototype._refreshListener = function() {
    this._app.refreshPortlets();
};

ZmPortalController.prototype._pauseListener = function(event) {
    var toolbar = this._toolbar[this._currentView];

    // en/disable refresh button
    var button = toolbar && toolbar.getButton(ZmOperation.REFRESH);
    var paused = event.item.isToggled();
    if (button) {
        button.setEnabled(!paused);
    }

    // pause portlets appearing on portal page
    this.setPaused(paused);
};

ZmPortalController.prototype._resetOperations = function(parent, num) {
//    ZmListController.prototype._resetOperations.call(parent, num);
    parent.enable(this._getToolBarOps(), true);
};