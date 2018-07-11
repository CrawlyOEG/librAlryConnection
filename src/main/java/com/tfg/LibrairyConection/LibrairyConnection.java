package com.tfg.LibrairyConection;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONValue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class LibrairyConnection {

	public LibrairyConnection(List<EstructuraDelPDF> textos, String stopWords, String typeTopic, String urlTopics, String urlSpace, String user, String password, String correo) {
		this.textos = textos;
		mequedo = 0;
		this.client = Client.create();
		this.stopWords = stopWords;
		this.typeTopic = typeTopic;
		if(urlTopics.charAt(urlTopics.length()-1) != '/')
			this.urlTopics = urlTopics + "/";
		else
			this.urlTopics = urlTopics;
		if(urlSpace.charAt(urlSpace.length()-1) != '/')
			this.urlSpace = urlSpace + "/";
		else
			this.urlSpace = urlSpace;
		this.user = user;
		this.password = password;
		this.internalCounter = 0; 
	}

	public LibrairyConnection(List<EstructuraDelPDF> textos,String typeTopic,String urlTopics, String urlSpace, String user, String password, String correo) {
		this.textos = textos;
		mequedo = 0;
		this.client = Client.create();
		this.typeTopic = typeTopic;
		this.stopWords = "";
		if(urlTopics.charAt(urlTopics.length()-1) != '/')
			this.urlTopics = urlTopics + "/";
		else
			this.urlTopics = urlTopics;
		if(urlSpace.charAt(urlSpace.length()-1) != '/')
			this.urlSpace = urlSpace + "/";
		else
			this.urlSpace = urlSpace;
		this.user = user;
		this.password = password;
		this.internalCounter = 0; 
	}
	
	//Costructor llamado si unicamente queremos comprobar un PDF
	public LibrairyConnection(String typeTopic,String urlTopics, String urlSpace, String user, String password) {
		this.textos = new ArrayList<>();
		mequedo = 0;
		this.client = Client.create();
		this.typeTopic = typeTopic;
		this.stopWords = "";
		if(urlTopics.charAt(urlTopics.length()-1) != '/')
			this.urlTopics = urlTopics + "/";
		else
			this.urlTopics = urlTopics;
		if(urlSpace.charAt(urlSpace.length()-1) != '/')
			this.urlSpace = urlSpace + "/";
		else
			this.urlSpace = urlSpace;
		this.user = user;
		this.password = password;
		this.internalCounter = 0; 
	}

	private List<EstructuraDelPDF> textos;
	private Integer mequedo;
	private Client client;
	private String stopWords;
	private String typeTopic;
	private String urlTopics;
	private String urlSpace;
	private String user;
	private String password;
	private int internalCounter;
	
	/*
	 * Creditos a: http://www.mkyong.com/webservices/jax-rs/restful-java-client-with-jersey-client/
	 */

	public void sacarTopicos() {
		//Por si quieres guardarte las trazas del programa.
		/*File errores = new File(carpeta + "\\ERRORES.txt");
		if(!errores.exists()) {
			try {
				errores.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}*/
		for (int i = mequedo; i < textos.size(); i++) {
			System.out.println("Pasamos el texto " + textos.get(i).getTitulo());
			String textoFixeado = fixearString(textos.get(i).getTexto());
			documentsPOST("id" + i, "name" + i, textoFixeado);
			mequedo = i;
		}
		System.out.println("\nVamos a realizar las dimensiones");
		dimensionsPOST();
		System.out.println("Waiting to show the subjects. \r\n" + 
				"Caution! This operation can take a long time. If you want to abort the program no problem will occur, you can see the results in " + urlTopics +
				"\n, on GET/dimensions");
		dimensionsGET();
	}
	
	public void entrenarModelo() {
		for (int i = 0; i < textos.size(); i++) {
			String vectorTexto = shapePOST(fixearString(textos.get(i).getTexto()));
			pointsPOST("id" + i,"name" + i,vectorTexto);
		}
	}
	
	/**
	 * 
	 * @param texto
	 * @return
	 */
	public double[] comprobarArchivo(String texto) {
		String vectorTexto = shapePOST(fixearString(texto));
		System.out.println(vectorTexto);
		Pattern pattern = Pattern.compile("\\[(.*)\\]");
		Matcher matcher = pattern.matcher(vectorTexto);
		if(matcher.find()) {
			String[] numeros = matcher.group(1).split(",");
			double[] todosJuntos = new double[numeros.length];
			for (int j = 0; j < numeros.length; j++) {
				double numeroOriginal = Double.parseDouble(numeros[j])*100;
				todosJuntos[j] = (double)Math.round(numeroOriginal * 100.0) / 100.0;
			}
			return todosJuntos;
		}
		else
			return null;
	}
	
	
	public double comprobarArchivo(String texto, String numeroVecinos) {
		String vectorTexto = shapePOST(fixearString(texto));
		return neighboursPOST(vectorTexto, Integer.parseInt(numeroVecinos));
	}

	public ClientResponse manejarCliente(String url,String input) {
		try {
			this.client.destroy();
			this.client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter(user, password));
			WebResource webResource = this.client.resource(url);
			if(input != null) 
				return webResource.type("application/json").post(ClientResponse.class, input);
			else
				return webResource.accept("application/json").get(ClientResponse.class);
		}catch(com.sun.jersey.api.client.ClientHandlerException ex) {
			System.out.println("Conexion caducada en " + url);
			try {
				Thread.sleep(12000);
			} catch (InterruptedException e) {
			}
			return this.manejarCliente(url,input);
		}
	}

	public void borrarElemento() {
		System.out.println("Vamos a eliminar elementos");
		try {
			documentsDELETE();
		}catch(com.sun.jersey.api.client.ClientHandlerException ex) {
			System.out.println("Conexion caducada en BORRAR ELEMENTOS");
			try {
				Thread.sleep(12000);
			} catch (InterruptedException e) {
			}
			this.borrarElemento();
		}
	}

	private String fixearString(String prueba) {
		//		String aDevolver = prueba;
		//		//aDevolver = aDevolver.replaceAll("[\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F]", "");
		//		aDevolver = aDevolver.replaceAll("\"","\\\"");
		//		aDevolver = aDevolver.replaceAll("\\p{Cc}", "");
		//		//aDevolver = aDevolver.replaceAll("'", "\\\"");
		//		return Base64.getEncoder().encodeToString(prueba.getBytes());
		return JSONValue.escape(prueba);
	}
	
	private void documentsGET() {
		Client client = Client.create();
		client.addFilter(new HTTPBasicAuthFilter(user, password));
		WebResource webResource = client
				.resource(urlTopics + "documents");
		ClientResponse response = webResource.accept("application/json")
				.get(ClientResponse.class);
		if (response.getStatus() != 200) {
			try {
				Thread.sleep(12000);
				documentsGET();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void documentsPOST(String ssID,String ssNAME,String ssTEXT) {
		String ssLABELS = "[  ]";
		String input = "{  \"id\": \""+ssID+"\",  "
				+ "\"labels\": "+ssLABELS+",  "
				+ "\"name\": \""+ssNAME+"\",  "
				+ "\"text\": \""+ssTEXT+"\"}";
		ClientResponse response = manejarCliente(urlTopics + "documents", input);
		int status = response.getStatus();
		if(!comprobarConexion(status))
			documentsPOST(ssID,ssNAME,ssTEXT);
		System.out.println("Subido el archivo " + ssID + " correctamente. Codigo " + status);
	}

	public void documentsDELETE() {
		Client client = Client.create();
		client.addFilter(new HTTPBasicAuthFilter(user, password));
		WebResource webResource = client
				.resource(urlTopics + "documents");
		ClientResponse response = webResource.accept("application/json")
				.delete(ClientResponse.class);
		if(!comprobarConexion(response.getStatus()))
			documentsDELETE();
	}

	public void dimensionsGET() {
		ClientResponse response = manejarCliente(urlTopics + "dimensions", null);
		if (response.getStatus() != 200) {
			try {
				Thread.sleep(180000);
				dimensionsGET();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		String laRespuesta = obtenerElJson(response.getEntity(String.class),"dimensions");
		if(laRespuesta.equals("[]")) {
			try {
				Thread.sleep(40000);
				dimensionsGET();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Mis topicos son: ");
			System.out.println(laRespuesta);
		}
	}

	private String obtenerElJson(String laRespuesta, String queObtener) {

		JsonParser jsonParser = new JsonParser();
		JsonObject nuevo = (JsonObject)jsonParser.parse(laRespuesta);
		return nuevo.get(queObtener).toString();
	}

	public void dimensionsPOST() {
		ClientResponse response = manejarCliente(urlTopics + "dimensions",
				"{ \"parameters\": { "
						+ "\"language\": \"en\", "
						+ "\"email\": \"j.galonso@alumnos.upm.es\", "
						+ "\"stopwords\": \""+ stopWords +"\" "
						+ "}}");
		int status = response.getStatus();
		if(!comprobarConexion(status))
			dimensionsPOST();
	}

	public String shapePOST(String texto) {
		ClientResponse response = manejarCliente(urlTopics + "shape",
				"{ \"text\": \""+texto+"\"}");
		int status = response.getStatus();
		if(!comprobarConexion(status)) {
			return this.shapePOST(texto);
		}
		else {
			String laRespuesta = obtenerElJson(response.getEntity(String.class),"vector");
			if(laRespuesta.equals("[]")) {
				System.out.println("A error has ocurred in POST/shape. The vector is empty");
				System.exit(0);
			}
			System.out.println("Mi vector es: " + laRespuesta);
			return laRespuesta;
		}
	}

	public void pointsPOST(String id, String name, String vector) {
		ClientResponse response = manejarCliente(urlSpace + "points",
				"{ \"id\": \""+id+"\", "
						+"\"name\": \""+name+"\", "
						+"\"shape\": "+ vector + ", "
						+"\"type\": \""+typeTopic+"\""
						+ " }");
		int status = response.getStatus();
		if(!comprobarConexion(status)) {
			System.out.println("A error has ocurred in POST/points.");
			System.exit(1);
		}
		System.out.println(response);
	}

	public double neighboursPOST(String vector, int numeroVecinos) {
		ClientResponse response = manejarCliente(urlSpace + "neighbours",
				"{ \"force\": true, "
						+"\"number\": "+ numeroVecinos + " , "
						+"\"shape\": "+ vector + " , "
						+"\"types\": [\""+ this.typeTopic+"\"]}");
		int status = response.getStatus();
		if(!comprobarConexion(status)) {
			System.out.println("A problem has ocurred in POST/neightbours");
			System.exit(0);
		}
		String laRespuesta = obtenerElJson(response.getEntity(String.class),"neighbours");
		System.out.println("Mis vecinos");
		Pattern pattern = Pattern.compile("[\\[,]\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(laRespuesta);
		while(matcher.find()) {
			System.out.println(matcher.group(1));
		}
		pattern = Pattern.compile("score\":(.*?)}");
		matcher = pattern.matcher(laRespuesta);
		if(matcher.find()) {
			return Double.parseDouble(matcher.group(1));
		}
		else {
			return -1.0;
		}
	}
	
	private boolean comprobarConexion(int status) {
		if(status == 401) {
			System.out.println("The user and/or password entered are not correct or do not correspond for this url ");
			System.exit(0);
		}
		if (status == 400 || status == 402 || status == 403 || status == 404) {
			System.out.println("There is a problem to call from your computer. " + "Status: " + status);
			System.exit(0);
		}
		if(status == 500 || status == 502 || status == 503 ) {
			if(this.internalCounter == 5) {
				System.out.println("Attempted to connect 5 times (10 minutes). Aborting operation ...");
				System.exit(1);
			}
			try {
				Thread.sleep(120000);
				this.internalCounter++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("There is a problem on the server or your conection. " + "Status: " + status + " Waiting 2 minutes ... \n");
			return false;
		}
		this.internalCounter = 0;
		return true;
	}
}
