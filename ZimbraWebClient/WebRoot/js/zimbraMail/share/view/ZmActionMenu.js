/*
 * 
 */

/**
 * @overview
 */

/**
 * Creates an action menu with the given menu items.
 * @class
 * This class represents an action menu, which is a popup menu with a few added features.
 * It can be easily created using a set of standard operations, and/or custom menu items
 * can be provided. This class is designed for use with items ({@link ZmItem}), so it can for
 * example contain a tab submenu. See also {@link ZmButtonToolBar}.
 *
 * @author Conrad Damon
 *
 * @param {hash}	params		a hash of parameters
 * @param {DwtComposite}	params.parent		the containing widget
 * @param {ZmController}	params.controller	the owning controller
 * @param {array}	params.menuItems	a list of operation IDs
 * @param {hash}	params.overrides	a hash of overrides by op ID
 * @param {string}	params.context		the context (used to create ID)
 * @param {constant}	params.menuType		the menu type (used to generate menu item IDs)
 * 
 * @extends		ZmPopupMenu
 */
ZmActionMenu = function(params) {

    var id = params.context ? ZmId.getMenuId(params.context, params.menuType) : null;
	ZmPopupMenu.call(this, params.parent, null, id, params.controller);

	// standard menu items default to Tag/Print/Delete
	var menuItems = params.menuItems;
	if (!menuItems) {
		menuItems = [ZmOperation.TAG_MENU, ZmOperation.PRINT, ZmOperation.DELETE];
	} else if (menuItems == ZmOperation.NONE) {
		menuItems = null;
	}
	// weed out disabled ops, save list of ones that make it
	this.opList = ZmOperation.filterOperations(menuItems);
	this._context = params.context;
	this._menuType = params.menuType;

	this._menuItems = ZmOperation.createOperations(this, this.opList, params.overrides);
};

ZmActionMenu.prototype = new ZmPopupMenu;
ZmActionMenu.prototype.constructor = ZmActionMenu;

// Public methods

/**
 * Returns a string representation of the object.
 * 
 * @return		{string}		a string representation of the object
 */
ZmActionMenu.prototype.toString = 
function() {
	return "ZmActionMenu";
};

/**
 * Creates a menu item and adds its operation ID as data.
 * 
 * @param {String}	id			the name of the operation
 * @param	{hash}	params		a hash of parameters
 * @param  {string}	params.text			the menu item text
 * @param {string}	params.image			the icon class for the menu item
 * @param {string}	params.disImage		the disabled version of icon
 * @param {boolean}	params.enabled		if <code>true</code>, menu item is enabled
 * @param {constant}	params.style			the menu item style
 * @param {string}	params.radioGroupId	the ID of radio group for this menu item
 * @param {constant}	params.shortcut		the shortcut ID (from {@link ZmKeyMap}) for showing hint
 * 
 * @private
 */
ZmActionMenu.prototype.createOp =
function(id, params) {
	params.id = this._context ? ZmId.getMenuItemId(this._context, id, this._menuType) : null;
	var mi = this.createMenuItem(id, params);
	mi.setData(ZmOperation.KEY_ID, id);

	return mi;
};

ZmActionMenu.prototype.addOp =
function(id) {
	ZmOperation.addOperation(this, id, this._menuItems);
};

ZmActionMenu.prototype.removeOp =
function(id) {
	ZmOperation.removeOperation(this, id, this._menuItems);
};

/**
 * Gets the menu item with the given ID.
 *
 * @param {constant}	id		an operation ID
 * @return	{DwtMenuItem}	the menu item
 * @see		ZmOperation
 */
ZmActionMenu.prototype.getOp =
function(id) {
	return this.getMenuItem(id);
};

/**
 * Gets the menu tag sub-menu (if any).
 * 
 * @return	{DwtMenu}		the menu
 */
ZmActionMenu.prototype.getTagMenu =
function() {
	var menuItem = this.getMenuItem(ZmOperation.TAG_MENU);
	if (menuItem) {
		return menuItem.getMenu();
	}
};

/**
 * Gets the menu search sub-menu (if any).
 *
 * @return {DwtMenu}        the menu
 */
ZmActionMenu.prototype.getSearchMenu =
function() {
    var menuItem = this.getMenuItem(ZmOperation.SEARCH_MENU);
    if (menuItem) {
        return menuItem.getMenu();
    }
};

// Private methods

// Returns the ID for the given menu item.
ZmActionMenu.prototype._menuItemId =
function(menuItem) {
	return menuItem.getData(ZmOperation.KEY_ID);
};
