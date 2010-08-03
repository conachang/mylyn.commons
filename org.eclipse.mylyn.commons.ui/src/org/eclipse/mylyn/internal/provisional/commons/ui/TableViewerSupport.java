/*******************************************************************************
 * Copyright (c) 2010 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.provisional.commons.ui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * @author Frank Becker
 * @author Steffen Pingel
 */
public class TableViewerSupport {

	private class TableColumnState {

		int width;

	}

	private int[] defaultOrder;

	private TableColumnState[] defaults;

	private final File stateFile;

	private final Table table;

	private final TableViewer viewer;

	private int defaultSortDirection;

	private int defaultSortColumnIndex;

	public TableViewerSupport(TableViewer viewer, File stateFile) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(stateFile);
		this.viewer = viewer;
		this.table = viewer.getTable();
		this.stateFile = stateFile;

		initialize();
		restore();
	}

	private void initialize() {
		TableColumn[] columns = table.getColumns();
		defaults = new TableColumnState[columns.length];
		defaultSortColumnIndex = -1;
		for (int i = 0; i < columns.length; i++) {
			final TableColumn column = columns[i];
			column.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int direction = table.getSortDirection();
					if (table.getSortColumn() == column) {
						direction = direction == SWT.UP ? SWT.DOWN : SWT.UP;
					} else {
						direction = SWT.DOWN;
					}
					table.setSortDirection(direction);
					table.setSortColumn(column);
					viewer.refresh();
				}
			});

			defaults[i] = new TableColumnState();
			defaults[i].width = column.getWidth();

			if (column == table.getSortColumn()) {
				defaultSortColumnIndex = i;
			}
		}
		defaultOrder = table.getColumnOrder();
		defaultSortDirection = table.getSortDirection();

		table.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				save();
			}
		});
	}

	public void restore() {
		if (stateFile.exists()) {
			try {
				FileReader reader = new FileReader(stateFile);
				try {
					XMLMemento memento = XMLMemento.createReadRoot(reader);

					IMemento[] children = memento.getChildren("Column"); //$NON-NLS-1$
					int[] order = new int[children.length];
					for (int i = 0; i < children.length; i++) {
						TableColumn column = table.getColumn(i);
						column.setWidth(children[i].getInteger("width")); //$NON-NLS-1$ 
						order[i] = children[i].getInteger("order"); //$NON-NLS-1$
					}
					try {
						table.setColumnOrder(order);
					} catch (IllegalArgumentException e) {
						// ignore
					}

					IMemento child = memento.getChild("Sort"); //$NON-NLS-1$
					if (child != null) {
						int columnIndex = child.getInteger("column"); //$NON-NLS-1$
						TableColumn column = table.getColumn(columnIndex);
						table.setSortColumn(column);
						table.setSortDirection(child.getInteger("direction")); //$NON-NLS-1$
					}
				} catch (Exception e) {
					// ignore
				} finally {
					reader.close();
				}
			} catch (IOException e) {
				// ignore
			}

			viewer.refresh();
		}
	}

	public void restoreDefaults() {
		for (int index = 0; index < defaults.length; index++) {
			TableColumn column = table.getColumn(index);
			column.setWidth(defaults[index].width);
		}
		table.setColumnOrder(defaultOrder);
		if (defaultSortColumnIndex != -1) {
			table.setSortColumn(table.getColumn(defaultSortColumnIndex));
			table.setSortDirection(defaultSortDirection);
		} else {
			table.setSortColumn(null);
		}
		viewer.refresh();
	}

	public void save() {
		XMLMemento memento = XMLMemento.createWriteRoot("Table"); //$NON-NLS-1$

		int[] order = table.getColumnOrder();
		TableColumn[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++) {
			TableColumn column = columns[i];
			IMemento child = memento.createChild("Column"); //$NON-NLS-1$
			child.putInteger("width", column.getWidth()); //$NON-NLS-1$
			child.putInteger("order", order[i]); //$NON-NLS-1$
		}

		TableColumn sortColumn = table.getSortColumn();
		if (sortColumn != null) {
			IMemento child = memento.createChild("Sort"); //$NON-NLS-1$
			child.putInteger("column", table.indexOf(sortColumn)); //$NON-NLS-1$
			child.putInteger("direction", table.getSortDirection()); //$NON-NLS-1$
		}

		try {
			FileWriter writer = new FileWriter(stateFile);
			try {
				memento.save(writer);
			} finally {
				writer.close();
			}
		} catch (IOException e) {
			// ignore
		}
	}

}