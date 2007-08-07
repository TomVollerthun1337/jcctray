/*******************************************************************************
 *  Copyright 2007 Ketan Padegaonkar http://ketan.padegaonkar.name
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package net.sourceforge.jcctray.ui;

import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.jcctray.model.DashBoardProject;
import net.sourceforge.jcctray.model.DashBoardProjects;
import net.sourceforge.jcctray.model.Host;
import net.sourceforge.jcctray.model.IJCCTraySettings;
import net.sourceforge.jcctray.model.ISettingsConstants;
import net.sourceforge.jcctray.ui.settings.providers.ProjectLabelProvider;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TrayItem;

/**
 * Monitors CruiseControl servers, and updates the JCCTray UI and system tray
 * icon.
 * 
 * @author Ketan Padegaonkar
 */
public class JCCTrayRunnable implements Runnable {

	private static final Logger		log			= Logger.getLogger(JCCTrayRunnable.class);
	private TableViewer				tableViewer;
	public boolean					shouldRun	= true;
	private final TrayItem			trayItem;
	private final IJCCTraySettings	traySettings;

	public JCCTrayRunnable(TableViewer tableViewer, TrayItem trayItem, IJCCTraySettings traySettings) {
		this.tableViewer = tableViewer;
		this.trayItem = trayItem;
		this.traySettings = traySettings;
	}

	public void run() {
		updateProjectsList(getAllProjects());
		while (shouldRun) {
			try {
				updateUI();
				Thread.sleep(traySettings.getInt(ISettingsConstants.POLL_INTERVAL) * 1000);
			} catch (Exception e) {
				log.error("Exception waiting on the background thread that fetches project status", e);
			}
		}
	}

	private void updateTrayIcon(DashBoardProjects projects) {
		trayItem.setImage(deduceImageToSet(projects));
	}

	private Image deduceImageToSet(DashBoardProjects projects) {
		DashBoardProject[] projectList = projects.toArray();
		Image icon = ProjectLabelProvider.GREEN_IMG;
		for (int i = 0; i < projectList.length; i++) {
			Image projectIcon = new ProjectLabelProvider().getImage(projectList[i]);
			if (projectIcon == ProjectLabelProvider.RED_IMG)
				icon = ProjectLabelProvider.RED_IMG;
			if (projectIcon == ProjectLabelProvider.YELLOW_IMG)
				icon = ProjectLabelProvider.YELLOW_IMG;
			if (projectIcon == ProjectLabelProvider.ORANGE_IMG)
				icon = ProjectLabelProvider.ORANGE_IMG;
		}
		return icon;
	}

	private void updateUI() {
		final DashBoardProjects projects = getAllProjects();
		Collection hosts = traySettings.getHosts();

		for (Iterator iterator = hosts.iterator(); iterator.hasNext();) {
			Host host = (Host) iterator.next();
			try {
				projects.add(host.getCruiseProjects());
			} catch (Exception e) {
				log.error("Could not fetch project list: " + host, e);
			}
		}

		updateProjectsList(projects);
	}

	private void updateProjectsList(final DashBoardProjects projects) {
		final Table table = tableViewer.getTable();
		table.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!table.isDisposed()) {
					tableViewer.setInput(projects);
					updateTrayIcon(projects);
					updateShellIcon(projects);
				}
			}
		});
	}

	private DashBoardProjects getAllProjects() {
		DashBoardProjects enabledProjects = new DashBoardProjects();

		Collection hosts = traySettings.getHosts();

		for (Iterator iterator = hosts.iterator(); iterator.hasNext();) {
			Collection projects = ((Host) iterator.next()).getConfiguredProjects();
			for (Iterator iterator2 = projects.iterator(); iterator2.hasNext();) {
				enabledProjects.add((DashBoardProject) iterator2.next());
			}
		}

		return enabledProjects;
	}

	protected void updateShellIcon(DashBoardProjects projects) {
		tableViewer.getTable().getShell().setImage(deduceImageToSet(projects));
	}

}
