/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.ui;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.internal.monitor.ui.IMonitoredWindow;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.core.InteractionEvent.Kind;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Self-registering on construction. Encapsulates users' interaction with the context model.
 *
 * @author Mik Kersten
 * @author Shawn Minto
 * @since 2.0
 */
public abstract class AbstractUserInteractionMonitor implements ISelectionListener {

	protected Object lastSelectedElement = null;

	private static int lastEditorHashcode;

	/**
	 * Requires workbench to be active.
	 */
	public AbstractUserInteractionMonitor() {
		try {
			MonitorUiPlugin.getDefault().addWindowPostSelectionListener(this);
		} catch (NullPointerException e) {
			StatusHandler.log(new Status(IStatus.WARNING, MonitorUiPlugin.ID_PLUGIN,
					"Monitors can not be instantiated until the workbench is active", e)); //$NON-NLS-1$
		}
	}

	public void dispose() {
		try {
			MonitorUiPlugin.getDefault().removeWindowPostSelectionListener(this);
		} catch (NullPointerException e) {
			StatusHandler.log(new Status(IStatus.WARNING, MonitorUiPlugin.ID_PLUGIN, "Could not dispose monitor", e)); //$NON-NLS-1$
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part.getSite() != null && part.getSite().getWorkbenchWindow() != null) {
			IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
			if (window instanceof IMonitoredWindow && !((IMonitoredWindow) window).isMonitored()) {
				return;
			}
		}
		if (selection == null || selection.isEmpty()) {
			return;
		}
		if (!ContextCore.getContextManager().isContextActive()) {
			handleWorkbenchPartSelection(part, selection, false);
		} else {
			handleWorkbenchPartSelection(part, selection, true);
		}
	}

	protected abstract void handleWorkbenchPartSelection(IWorkbenchPart part, ISelection selection,
			boolean contributeToContext);

	/**
	 * Intended to be called back by subclasses.
	 */
	protected InteractionEvent handleElementSelection(IWorkbenchPart part, Object selectedElement,
			boolean contributeToContext) {
		return handleElementSelection(part.getSite().getId(), selectedElement, contributeToContext);
	}

	/**
	 * Intended to be called back by subclasses.
	 */
	protected void handleElementEdit(IWorkbenchPart part, Object selectedElement, boolean contributeToContext) {
		boolean isModified = isElementModified(selectedElement);
		handleElementEdit(part.getSite().getId(), selectedElement, contributeToContext, isModified);
	}

	/**
	 * Intended to be called back by subclasses.
	 */
	protected void handleNavigation(IWorkbenchPart part, Object targetElement, String kind, boolean contributeToContext) {
		handleNavigation(part.getSite().getId(), targetElement, kind, contributeToContext);
	}

	/**
	 * Intended to be called back by subclasses. *
	 *
	 * @since 3.1
	 */
	protected void handleNavigation(String partId, Object targetElement, String kind, boolean contributeToContext) {
		AbstractContextStructureBridge adapter = ContextCore.getStructureBridge(targetElement);
		if (adapter.getContentType() != null) {
			String handleIdentifier = adapter.getHandleIdentifier(targetElement);
			InteractionEvent navigationEvent = new InteractionEvent(InteractionEvent.Kind.SELECTION,
					adapter.getContentType(), handleIdentifier, partId, kind);
			if (handleIdentifier != null && contributeToContext) {
				ContextCore.getContextManager().processInteractionEvent(navigationEvent);
			}
			MonitorUiPlugin.getDefault().notifyInteractionObserved(navigationEvent);
		}
	}

	/**
	 * Intended to be called back by subclasses.
	 *
	 * @since 3.1
	 */
	protected void handleElementEdit(String partId, Object selectedElement, boolean contributeToContext) {
		handleElementEdit(partId, selectedElement, contributeToContext, false);
	}

	private void handleElementEdit(String partId, Object selectedElement, boolean contributeToContext,
			boolean isModified) {
		if (selectedElement == null) {
			return;
		}
		AbstractContextStructureBridge bridge = ContextCore.getStructureBridge(selectedElement);
		String handleIdentifier = bridge.getHandleIdentifier(selectedElement);
		String delta = isModified ? "modified" : "referred"; //$NON-NLS-1$//$NON-NLS-2$
		InteractionEvent editEvent = new InteractionEvent(InteractionEvent.Kind.EDIT, bridge.getContentType(),
				handleIdentifier, partId, "null", delta, 1f); //$NON-NLS-1$
		if (handleIdentifier != null && contributeToContext) {
			ContextCore.getContextManager().processInteractionEvent(editEvent);
		}
		MonitorUiPlugin.getDefault().notifyInteractionObserved(editEvent);
	}

	/**
	 * Intended to be called back by subclasses. *
	 *
	 * @since 3.1
	 */
	protected InteractionEvent handleElementSelection(String partId, Object selectedElement, boolean contributeToContext) {
		if (selectedElement == null || selectedElement.equals(lastSelectedElement)) {
			return null;
		}
		IDocument document = getDocument(selectedElement);
		if (document != null) {
			lastSelectedElement = selectedElement;
			lastEditorHashcode = document.get().hashCode();
		}
		AbstractContextStructureBridge bridge = ContextCore.getStructureBridge(selectedElement);
		String handleIdentifier = bridge.getHandleIdentifier(selectedElement);
		InteractionEvent selectionEvent;
		if (bridge.getContentType() != null) {
			selectionEvent = new InteractionEvent(InteractionEvent.Kind.SELECTION, bridge.getContentType(),
					handleIdentifier, partId);
		} else {
			selectionEvent = new InteractionEvent(InteractionEvent.Kind.SELECTION, null, null, partId);
		}
		if (handleIdentifier != null && contributeToContext) {
			ContextCore.getContextManager().processInteractionEvent(selectionEvent);
		}
		MonitorUiPlugin.getDefault().notifyInteractionObserved(selectionEvent);
		return selectionEvent;
	}

	private boolean isElementModified(Object editElement) {
		boolean isModified = false;
		IDocument document = getDocument(editElement);
		if (document == null) {
			return false;
		}
		int hashcode = document.get().hashCode();
		if (lastSelectedElement != null && lastSelectedElement.equals(editElement) && lastEditorHashcode != hashcode) {
			isModified = true;
		}
		lastSelectedElement = editElement;
		lastEditorHashcode = hashcode;
		return isModified;
	}

	/**
	 * Returns the hashcode of the selectedElement. Returns -1 if any error happens.
	 *
	 * @param selectedElement
	 * @return
	 */
	private synchronized IDocument getDocument(Object selectedElement) {
		IPath iPath = null;
		if (selectedElement instanceof IJavaElement) {
			IJavaElement iJElement = (IJavaElement) selectedElement;
			iPath = iJElement.getPath();
		} else if (selectedElement instanceof IFile) {
			IFile iFile = (IFile) selectedElement;
			iPath = iFile.getFullPath();
		}

		if (iPath == null) {
			return null;//error
		}
		try {
			ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
			manager.connect(iPath, LocationKind.IFILE, null);
			ITextFileBuffer buffer = manager.getTextFileBuffer(iPath, LocationKind.IFILE);
			IDocument document = buffer.getDocument();
			manager.disconnect(iPath, LocationKind.IFILE, null);
			return document;
		} catch (CoreException e) {
			return null;//error
		}

	}

	public Kind getEventKind() {
		return InteractionEvent.Kind.SELECTION;
	}
}
