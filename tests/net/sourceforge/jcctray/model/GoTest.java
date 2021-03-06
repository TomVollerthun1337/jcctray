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
package net.sourceforge.jcctray.model;

import junit.framework.TestCase;

/**
 * @author Ketan Padegaonkar
 */
public class GoTest extends TestCase {

	private Go	cruise;

	public void testFormatsDate() throws Exception {
		String formattedDate = cruise.formatDate("2007-07-01T10:14:34", null);
		assertEquals("10:14:34 AM, 01 Jul", formattedDate);
	}

	public void testInvalidDateReturnsSameDate() throws Exception {
		String invalidDate = "xyz";
		String formattedDate = cruise.formatDate(invalidDate, null);
		assertEquals(invalidDate, formattedDate);
	}
	
	public void testGetsForceBuildPortWhenSystemPropertyIsSet() throws Exception {
		System.setProperty("forcebuild.myCruiseServer.port", "1000");
		assertEquals("1000", cruise.getForceBuildPort(new Host("myCruiseServer", "my.ip.add.ress")));
	}
	
	public void testGetsForceBuildURL() throws Exception {
		DashBoardProject dashBoardProject = new DashBoardProject("myProject", new Host("myHost", "http://my.host.name", cruise));
		String forceBuildURL = cruise.forceBuildURL(dashBoardProject);
		assertEquals("http://my.host.name:8000/go/api/pipelines/myProject/schedule", forceBuildURL);
	}
	
	public void testGetsForceBuildURLWithTrailingSlashInHostURL() throws Exception {
		DashBoardProject dashBoardProject = new DashBoardProject("myProject", new Host("myHost", "http://my.host.name/foo//", cruise));
		String forceBuildURL = cruise.forceBuildURL(dashBoardProject);
		assertEquals("http://my.host.name:8000/foo/go/api/pipelines/myProject/schedule", forceBuildURL);
	}
	
	public void testGetXmlReportURL() throws Exception {
		assertEquals("http://host.name/foo/go/cctray.xml",cruise.getXmlReportURL(new Host("blah", "http://host.name/foo//"))); 
	}
	
	public void testGetsName() throws Exception {
		assertEquals("ThoughtWorks Go",cruise.getName());
	}
	
//	https://go01.thoughtworks.com/go/api/pipelines/GadgetToolkitJs/schedule
	
	public void testGetsSuccessMessage() throws Exception {
		assertEquals("Invocation successful",cruise.getSuccessMessage(null));
	}
	
	protected void setUp() throws Exception {
		cruise = new Go();
	}
}
