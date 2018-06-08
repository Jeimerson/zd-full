/*
 * 
 */
/*
 * Package: TasksCore
 * 
 * Supports: Creation of a tasks folder
 * 
 * Loaded:
 *  - If a task folder arrives in a <refresh> block
 *  - If a search for tasks returns results
 */
AjxPackage.require("zimbraMail.calendar.model.ZmCalItem");
AjxPackage.require("zimbraMail.calendar.model.ZmRecurrence");
AjxPackage.require("zimbraMail.tasks.model.ZmTaskFolder");
AjxPackage.require("zimbraMail.tasks.model.ZmTask");

AjxPackage.require("zimbraMail.tasks.controller.ZmTaskListController");

AjxPackage.require("zimbraMail.tasks.view.ZmTaskListView");
