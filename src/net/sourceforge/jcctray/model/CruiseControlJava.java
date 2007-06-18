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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.jcctray.exceptions.HTTPErrorException;
import net.sourceforge.jcctray.exceptions.InvocationException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

/**
 * @author Ketan Padegaonkar
 */
public class CruiseControlJava implements ICruise {

	private static final Logger	log	= Logger.getLogger(CCNet.class);

	private static HttpClient	client;

	public void forceBuild(DashBoardProject project) throws Exception {
		HttpMethod method = httpMethod(project);
		if (executeMethod(method) != HttpStatus.SC_OK)
			throw new Exception("There was an http error connecting to the server at " + project.getHost().getHostName());
		if (!isInvokeSuccessful(method, project))
			throw new Exception("The force build was not successful, the server did not return what JCCTray was expecting.");
	}


	private HttpMethod httpMethod(DashBoardProject project) {
		HttpMethod method = new GetMethod(forceBuildURL(project));
		configureMethod(method, project);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		return method;
	}

	private void configureMethod(HttpMethod method, DashBoardProject project) {
		// do nothing
	}


	private boolean isInvokeSuccessful(HttpMethod method, DashBoardProject project) throws InvocationException {
		try {
			String message = getSuccessMessage(project);
			boolean invokeSuccessful = method.getResponseBodyAsString().contains(message);
			if (!invokeSuccessful)
				log.error("Could not find the string '" + message + "' in the page located at "+ method.getURI());
			return invokeSuccessful;
		} catch (IOException e) {
			log.error("Attempted to force the build, but the force was not successful", e);
			throw new InvocationException("Attempted to force the build, but the force was not successful", e);
		}
	}


	private String getSuccessMessage(DashBoardProject project) {
		return "Invocation successful";
	}

	private int executeMethod(HttpMethod method) throws HTTPErrorException {
		try {
			int httpStatus = getClient().executeMethod(method);
			if (httpStatus == HttpStatus.SC_OK)
				log.error("Method failed: " + method.getStatusLine());
			return httpStatus;
		} catch (Exception e) {
			log.error("Could not force a build on project, either the webpage is not available, or there was a timeout in connecting to the cruise server.", e);
			throw new HTTPErrorException("Could not force a build on project, either the webpage is not available, or there was a timeout in connecting to the cruise server.", e);
		}
	}

	private String forceBuildURL(DashBoardProject project) {
		String hostString = project.getHost().getHostName();
		URL url = null;
		try {
			url = new URL(hostString);
			return url.getProtocol() + "://" + url.getHost()
					+ ":8000/invoke?operation=build&objectname=CruiseControl+Project%3Aname%3D" + project.getName();
		} catch (MalformedURLException e) {
			log.error("The url was malformed: " + url, e);
		}
		return null;
	}

	public String getName() {
		return "CruiseControl";
	}

	private static HttpClient getClient() {
		if (client == null) {
			client = new HttpClient();
			client.setConnectionTimeout(10000);
			client.setTimeout(10000);
		}
		return client;
	}

	public String formatDate(String date) {
		Date parse;
		try {
			parse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date);
			return new SimpleDateFormat("h:mm:ss a, dd MMM").format(parse);
		} catch (ParseException e) {
			log.error("Could not parse date: " + date);
		}
		return date;
	}

}
