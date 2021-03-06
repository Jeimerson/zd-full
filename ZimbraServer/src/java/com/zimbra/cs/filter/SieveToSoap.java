/*
 * 
 */
package com.zimbra.cs.filter;

import java.util.Date;
import java.util.List;

import com.zimbra.common.util.StringUtil;
import org.apache.jsieve.parser.generated.Node;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.Element.ElementFactory;
import com.zimbra.cs.filter.FilterUtil.DateComparison;
import com.zimbra.cs.filter.FilterUtil.Flag;
import com.zimbra.cs.filter.FilterUtil.NumberComparison;
import com.zimbra.cs.filter.FilterUtil.StringComparison;

/**
 * Converts a Sieve node tree to the SOAP representation of
 * filter rules.
 */
public class SieveToSoap extends SieveVisitor {

    private Element mRoot;
    private List<String> mRuleNames;
    private Element mCurrentRule;
    private int mCurrentRuleIndex = 0;
    
    public SieveToSoap(ElementFactory factory, List<String> ruleNames) {
        mRoot = factory.createElement(MailConstants.E_FILTER_RULES);
        mRuleNames = ruleNames;
    }
    
    public Element getRootElement() {
        return mRoot;
    }
    
    @Override
    protected void visitRule(Node ruleNode, VisitPhase phase, RuleProperties props) {
        if (phase == VisitPhase.end) {
            return;
        }
        
        // rule element
        mCurrentRule = mRoot.addElement(MailConstants.E_FILTER_RULE);
        String name = getCurrentRuleName();
        if (name != null) {
            mCurrentRule.addAttribute(MailConstants.A_NAME, name);
        }
        mCurrentRule.addAttribute(MailConstants.A_ACTIVE, props.isEnabled);

        // filterTests element
        Element filterTests = mCurrentRule.addElement(MailConstants.E_FILTER_TESTS);
        filterTests.addAttribute(MailConstants.A_CONDITION, props.condition.toString());
        
        // filterActions element
        mCurrentRule.addElement(MailConstants.E_FILTER_ACTIONS);

        mCurrentRuleIndex++;
    }
    
    private Element addTest(String elementName, RuleProperties props)
    throws ServiceException {
        Element tests = mCurrentRule.getElement(MailConstants.E_FILTER_TESTS);
        int index = tests.listElements().size();
        Element test = tests.addElement(elementName);
        if (props.isNegativeTest) {
            test.addAttribute(MailConstants.A_NEGATIVE, "1");
        }
        test.addAttribute(MailConstants.A_INDEX, index);
        return test;
    }
    
    private Element addAction(String elementName)
    throws ServiceException {
        Element actions = mCurrentRule.getElement(MailConstants.E_FILTER_ACTIONS);
        int index = actions.listElements().size();
        Element action = actions.addElement(elementName);
        action.addAttribute(MailConstants.A_INDEX, Integer.toString(index));
        return action;
    }
    
    @Override
    protected void visitAttachmentTest(Node node, VisitPhase phase, RuleProperties props)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            addTest(MailConstants.E_ATTACHMENT_TEST, props);
        }
    }

    @Override
    protected void visitBodyTest(Node node, VisitPhase phase, RuleProperties props, boolean caseSensitive, String value)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            Element test = addTest(MailConstants.E_BODY_TEST, props);
            if (caseSensitive)
                test.addAttribute(MailConstants.A_CASE_SENSITIVE, caseSensitive);
            test.addAttribute(MailConstants.A_VALUE, value);
        }
    }

    @Override
    protected void visitDateTest(Node node, VisitPhase phase, RuleProperties props,
                                 DateComparison comparison, Date date)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            Element test = addTest(MailConstants.E_DATE_TEST, props);
            test.addAttribute(MailConstants.A_DATE_COMPARISON, comparison.toString());
            test.addAttribute(MailConstants.A_DATE, date.getTime() / 1000);
        }
    }

    @Override
    protected void visitCurrentTimeTest(Node node, VisitPhase phase, RuleProperties props,
                                        DateComparison comparison, String timeStr)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            Element test = addTest(MailConstants.E_CURRENT_TIME_TEST, props);
            test.addAttribute(MailConstants.A_DATE_COMPARISON, comparison.toString());
            test.addAttribute(MailConstants.A_TIME, timeStr);
        }
    }

    @Override
    protected void visitCurrentDayOfWeekTest(Node node, VisitPhase phase, RuleProperties props, List<String> days)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            Element test = addTest(MailConstants.E_CURRENT_DAY_OF_WEEK_TEST, props);
            test.addAttribute(MailConstants.A_VALUE, StringUtil.join(",", days));
        }
    }

    @Override
    protected void visitTrueTest(Node node, VisitPhase phase, RuleProperties props)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            addTest(MailConstants.E_TRUE_TEST, props);
        }
    }

    @Override
    protected void visitHeaderExistsTest(Node node, VisitPhase phase, RuleProperties props,
                                         String header)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            Element test = addTest(MailConstants.E_HEADER_EXISTS_TEST, props);
            test.addAttribute(MailConstants.A_HEADER, header);
        }
    }

    @Override
    protected void visitHeaderTest(String testEltName, Node node, VisitPhase phase, RuleProperties props,
                                   List<String> headers, StringComparison comparison, boolean caseSensitive, String value)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            Element test = addTest(testEltName, props);
            test.addAttribute(MailConstants.A_HEADER, StringUtil.join(",", headers));
            test.addAttribute(MailConstants.A_STRING_COMPARISON, comparison.toString());
            if (caseSensitive)
                test.addAttribute(MailConstants.A_CASE_SENSITIVE, caseSensitive);
            test.addAttribute(MailConstants.A_VALUE, value);
        }
    }

    @Override
    protected void visitSizeTest(Node node, VisitPhase phase, RuleProperties props,
                                 NumberComparison comparison, int size, String sizeString)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            Element test = addTest(MailConstants.E_SIZE_TEST, props);
            test.addAttribute(MailConstants.A_NUMBER_COMPARISON, comparison.toString());
            test.addAttribute(MailConstants.A_SIZE, sizeString);
        }
    }

    @Override
    protected void visitAddressBookTest(Node node, VisitPhase phase, RuleProperties props,
                                        String header, String folderPath) throws ServiceException {
        if (phase == VisitPhase.begin) {
            Element test = addTest(MailConstants.E_ADDRESS_BOOK_TEST, props);
            test.addAttribute(MailConstants.A_HEADER, header);
            test.addAttribute(MailConstants.A_FOLDER_PATH, folderPath);
        }
    }

    @Override
    protected void visitInviteTest(Node node, VisitPhase phase, RuleProperties props, List<String> methods)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            Element test = addTest(MailConstants.E_INVITE_TEST, props);
            for (String method : methods) {
                test.addElement(MailConstants.E_METHOD).setText(method);
            }
        }
    }

    private String getCurrentRuleName() {
        if (mRuleNames == null || mCurrentRuleIndex >= mRuleNames.size()) {
            return null;
        }
        return mRuleNames.get(mCurrentRuleIndex);
    }

    @Override
    protected void visitDiscardAction(Node node, VisitPhase phase, RuleProperties props)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            addAction(MailConstants.E_ACTION_DISCARD);
        }
    }

    @Override
    protected void visitFileIntoAction(Node node, VisitPhase phase, RuleProperties props,
                                       String folderPath) throws ServiceException {
        if (phase == VisitPhase.begin) {
            addAction(MailConstants.E_ACTION_FILE_INTO).addAttribute(MailConstants.A_FOLDER_PATH, folderPath);
        }
    }

    @Override
    protected void visitFlagAction(Node node, VisitPhase phase, RuleProperties props, Flag flag)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            addAction(MailConstants.E_ACTION_FLAG).addAttribute(MailConstants.A_FLAG_NAME, flag.toString());
        }
    }

    @Override
    protected void visitKeepAction(Node node, VisitPhase phase, RuleProperties props)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            addAction(MailConstants.E_ACTION_KEEP);
        }
    }

    @Override
    protected void visitRedirectAction(Node node, VisitPhase phase, RuleProperties props,
                                       String address) throws ServiceException {
        if (phase == VisitPhase.begin) {
            addAction(MailConstants.E_ACTION_REDIRECT).addAttribute(MailConstants.A_ADDRESS, address);
        }
    }

    @Override
    protected void visitReplyAction(Node node, VisitPhase phase, RuleProperties props, String bodyTemplate)
            throws ServiceException {
        if (phase == VisitPhase.begin) {
            addAction(MailConstants.E_ACTION_REPLY).addElement(MailConstants.E_CONTENT).addText(bodyTemplate);
        }
    }

    @Override
    protected void visitNotifyAction(Node node, VisitPhase phase, RuleProperties props,
            String emailAddr, String subjectTemplate, String bodyTemplate, int maxBodyBytes, List<String> origHeaders)
            throws ServiceException {
        if (phase == VisitPhase.begin) {
            Element action = addAction(MailConstants.E_ACTION_NOTIFY);
            action.addAttribute(MailConstants.A_ADDRESS, emailAddr);
            if (!StringUtil.isNullOrEmpty(subjectTemplate))
                action.addAttribute(MailConstants.A_SUBJECT, subjectTemplate);
            if (!StringUtil.isNullOrEmpty(bodyTemplate))
                action.addElement(MailConstants.E_CONTENT).addText(bodyTemplate);
            if (maxBodyBytes != -1)
                action.addAttribute(MailConstants.A_MAX_BODY_SIZE, maxBodyBytes);
            if (origHeaders != null && !origHeaders.isEmpty())
                action.addAttribute(MailConstants.A_ORIG_HEADERS, StringUtil.join(",", origHeaders));
        }
    }

     @Override
    protected void visitStopAction(Node node, VisitPhase phase, RuleProperties props)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            addAction(MailConstants.E_ACTION_STOP);
        }
    }

    @Override
    protected void visitTagAction(Node node, VisitPhase phase, RuleProperties props, String tagName)
    throws ServiceException {
        if (phase == VisitPhase.begin) {
            addAction(MailConstants.E_ACTION_TAG).addAttribute(MailConstants.A_TAG_NAME, tagName);
        }
    }
}
