/*
 * 
 */

ZmCalendarAssistant = function() {
	ZmAssistant.call(this, ZmMsg.openCalendar, ZmMsg.ASST_CMD_CALENDAR, ZmMsg.ASST_CMD_SUM_CALENDAR);
};

ZmCalendarAssistant.prototype = new ZmAssistant();
ZmCalendarAssistant.prototype.constructor = ZmCalendarAssistant;

ZmCalendarAssistant.prototype.okHandler =
function(dialog) {
    var calMgr = appCtxt.getCalManager();
    calMgr.getMiniCalendar();
    var cc = calMgr.getCalViewController();
	cc.setDate(this._startDate);
	// need to call twice due to cal view controller bug
	cc.show(this._view);
	cc.show(this._view);	
	return true;
};

ZmCalendarAssistant.prototype.getHelp =
function() {
	return ZmMsg.ASST_CALENDAR_HELP;
};

ZmCalendarAssistant.prototype.handle =
function(dialog, verb, args) {
	
	this._startDate = null;
	// look for start date
	var match = this._objectManager.findMatch(args, ZmObjectManager.DATE);
	if (match) {
		args = args.replace(match[0], " ");
		this._startDate = match.context.date;
	}

	match = args.match(RegExp("\\b("
							  +ZmMsg.viewCalForDay+"|"
							  +ZmMsg.viewCalForWeek+"|"
							  +ZmMsg.viewCalForWorkWeek+"|"
							  +ZmMsg.viewCalForList+"|"
							  +ZmMsg.viewCalForMonth+")\\b"));
	var view = (match) ? match[1] : null;
	var icon;
	switch (view) {
		case ZmMsg.viewCalForDay:
			icon = 'DayView';
			this._view = ZmId.VIEW_CAL_DAY;
			view = ZmMsg.viewDay;
			break;
		case ZmMsg.viewCalForWeek:
			icon = 'WeekView';
			this._view = ZmId.VIEW_CAL_WEEK;
			view = ZmMsg.viewWeek;
			break;
		case ZmMsg.viewCalForWorkWeek:
			icon = 'WorkWeekView';
			this._view = ZmId.VIEW_CAL_WORK_WEEK;
			view = ZmMsg.viewWorkWeek;
			break;
		case ZmMsg.viewCalForMonth:
			icon = 'MonthView';
			this._view = ZmId.VIEW_CAL_MONTH;
			view = ZmMsg.viewMonth;
			break;
		case ZmMsg.viewCalForList:
			icon = 'TasksListView';
			this._view = ZmId.VIEW_CAL_LIST;
			view = ZmMsg.viewList;
			break;
		default:
			icon = "CalendarApp";
			this._view = null;
	}

	dialog._setOkButton(AjxMsg.ok, true, true);
	
	if (this._startDate == null) this._startDate = new Date();
	var startDateValue = DwtCalendar.getDateFullFormatter().format(this._startDate);

	this._setField(ZmMsg.goToDate, startDateValue, false, true);
	this._setField(ZmMsg.view, view == null ? ZmMsg.calAssistDefaultView : view, view == null, true);
};
