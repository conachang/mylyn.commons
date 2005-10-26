/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.monitor.tests;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.mylar.core.InteractionEvent;
import org.eclipse.mylar.monitor.InteractionEventLogger;
import org.eclipse.mylar.monitor.MylarMonitorPlugin;

/**
 * @author Mik Kersten
 */
public class InteractionLoggerTest extends TestCase {

    private InteractionEventLogger logger = MylarMonitorPlugin.getDefault().getInteractionLogger();
    
    @Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		MylarMonitorPlugin.getDefault().stopMonitoring();
	}
	
	public void testClearHistory() throws IOException {
		logger.start();
		File monitorFile = logger.getOutputFile();
    	assertTrue(monitorFile.exists());
    	logger.interactionObserved(InteractionEvent.makeCommand("a", "b"));
    	logger.stop();
    	assertTrue(monitorFile.length() > 0);
        logger.clearInteractionHistory();
        assertEquals(monitorFile.length(), 0);
	}
}