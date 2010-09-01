/*******************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.provisional.commons.ui;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author Steffen Pingel
 */
public abstract class TableSorter extends AbstractColumnViewerSorter<TableViewer> {

	@Override
	int getColumnIndex(Viewer viewer, Item column) {
		if (viewer instanceof TableViewer && column instanceof TableColumn) {
			((TableViewer) viewer).getTable().indexOf((TableColumn) column);
		}
		return 0;
	}

	@Override
	Item getSortColumn(Viewer viewer) {
		if (viewer instanceof TableViewer) {
			return ((TableViewer) viewer).getTable().getSortColumn();
		}
		return null;
	}

	@Override
	int getSortDirection(Viewer viewer) {
		if (viewer instanceof TableViewer) {
			return ((TableViewer) viewer).getTable().getSortDirection();
		}
		return 0;
	}

}
