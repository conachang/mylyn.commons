/*******************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Mik Kersten
 */
public class AllMonitorTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllMonitorTests.class.getName());
		suite.addTestSuite(CheckActivityJobTest.class);
		suite.addTestSuite(ActivityContextManagerTest.class);
		return suite;
	}

}
