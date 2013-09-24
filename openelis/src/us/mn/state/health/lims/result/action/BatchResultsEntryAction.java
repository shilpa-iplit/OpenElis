/*
* The contents of this file are subject to the Mozilla Public License
* Version 1.1 (the "License"); you may not use this file except in
* compliance with the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/ 
* 
* Software distributed under the License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific language governing rights and limitations under
* the License.
* 
* The Original Code is OpenELIS code.
* 
* Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
*/
package us.mn.state.health.lims.result.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import us.mn.state.health.lims.common.action.BaseAction;
import us.mn.state.health.lims.common.util.DateUtil;
import us.mn.state.health.lims.login.dao.UserTestSectionDAO;
import us.mn.state.health.lims.login.daoimpl.UserTestSectionDAOImpl;
import us.mn.state.health.lims.result.dao.ResultDAO;
import us.mn.state.health.lims.result.daoimpl.ResultDAOImpl;
import us.mn.state.health.lims.result.valueholder.Result;
import us.mn.state.health.lims.test.valueholder.TestComparator;
import us.mn.state.health.lims.test.valueholder.TestSectionComparator;

/**
 * @author diane benz
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class BatchResultsEntryAction extends BaseAction {

	private boolean isNew = false;

	protected ActionForward performAction(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// The first job is to determine if we are coming to this action with an
		// ID parameter in the request. If there is no parameter, we are
		// creating a new Result.
		// If there is a parameter present, we should bring up an existing
		// Result to edit.
		String id = request.getParameter(ID);

		String forward = FWD_SUCCESS;
		request.setAttribute(ALLOW_EDITS_KEY, "false");
		request.setAttribute(PREVIOUS_DISABLED, "true");
		request.setAttribute(NEXT_DISABLED, "true");

		DynaActionForm dynaForm = (DynaActionForm) form;

		//Get tests/testsections by user system id
		//bugzilla 2160
		UserTestSectionDAO userTestSectionDAO = new UserTestSectionDAOImpl();
		List testSections = userTestSectionDAO.getAllUserTestSections(request);
		//bugzilla 2291
		List tests = userTestSectionDAO.getAllUserTests(request, true);

		// get 3 drop down selections so we can repopulate
		String selectedTestSectionId = (String) dynaForm
				.get("selectedTestSectionId");
		String selectedTestId = (String) dynaForm.get("selectedTestId");

		// initialize the form
		dynaForm.initialize(mapping);

		// default received date and entered date to today's date
		Date today = Calendar.getInstance().getTime();
		Locale locale = (Locale) request.getSession().getAttribute(
				"org.apache.struts.action.LOCALE");

		String dateAsText = DateUtil.formatDateAsText(today, locale);
		String blank = "";

		Result result = new Result();

		if ((id != null) && (!"0".equals(id))) { // this is an existing
			// result
			result.setId(id);
			ResultDAO resultDAO = new ResultDAOImpl();
			resultDAO.getData(result);

			isNew = false; // this is to set correct page title

			// do we need to enable next or previous?
			List results = resultDAO.getNextResultRecord(result.getId());
			if (results.size() > 0) {
				// enable next button
				request.setAttribute(NEXT_DISABLED, "false");
			}
			results = resultDAO.getPreviousResultRecord(result.getId());
			if (results.size() > 0) {
				// enable next button
				request.setAttribute(PREVIOUS_DISABLED, "false");
			}
			// end of logic to enable next or previous button
		} else { // this is a new result

			isNew = true; // this is to set correct page title
		}

		if (result.getId() != null && !result.getId().equals("0")) {
			request.setAttribute(ID, result.getId());
		}

		// #1347 sort dropdown values
		Collections.sort(testSections, TestSectionComparator.NAME_COMPARATOR);
		
		//bugzilla 1844 change sort
		Collections.sort(tests, TestComparator.DESCRIPTION_COMPARATOR);

		// populate form from valueholder
		PropertyUtils.copyProperties(form, result);

		//bgm - bugzilla 1569 changed dateAsText to blank for the receivedDateForDisplay field.
		PropertyUtils.setProperty(form, "tests", tests);
		PropertyUtils.setProperty(form, "testSections", testSections);
		PropertyUtils.setProperty(form, "currentDate", dateAsText);
		PropertyUtils.setProperty(form, "receivedDateForDisplay", blank);
		PropertyUtils.setProperty(form, "sample_TestAnalytes", new ArrayList());
		PropertyUtils.setProperty(form, "testAnalyte_TestResults",
				new ArrayList());

	      //bugzila 2553 removed a line of code from 2187 - not needed
		
		return mapping.findForward(forward);
	}

	protected String getPageTitleKey() {
		if (isNew) {
			return "batchresultsentry.add.title";
		} else {
			return "batchresultsentry.edit.title";
		}
	}

	protected String getPageSubtitleKey() {
		if (isNew) {
			return "batchresultsentry.add.subtitle";
		} else {
			return "batchresultsentry.edit.subtitle";
		}
	}

}
