package edu.csula.cs594.client.dao;

import java.util.List;

public class Service {
	private int id;
	private String name;
	private String baseUri;
	private String descriptionUri;
	private String method;
	private List<String> parameters;
	private String testUri;

	public Service(int id, String name, String descriptionUri, String baseUri, String method, List<String> parameters, String testUri) {
		this.id = id;
		this.name = name;
		this.descriptionUri = descriptionUri;
		this.baseUri = baseUri;
		this.method = method;
		this.parameters = parameters;
		this.testUri = testUri;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescriptionUri() {
		return descriptionUri;
	}

	public void setDescriptionUri(String descriptionUri) {
		this.descriptionUri = descriptionUri;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public String getTestUri() {
		return testUri;
	}

	public void setTestUri(String testUri) {
		this.testUri = testUri;
	}
}
