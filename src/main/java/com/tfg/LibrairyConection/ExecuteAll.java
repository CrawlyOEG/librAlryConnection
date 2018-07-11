package com.tfg.LibrairyConection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.tfg.Extraccion.RangeExtraction;
import com.tfg.Extraccion.ExtractionMode;
import com.tfg.Extraccion.ReadPDF;

public class ExecuteAll {
	public static List<String[]> listaConTopicos;

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		String user = "";
		String mode = "";
		String password = "";
		String carpeta = "";
		String keyWord = "";
		String urlTopics = "";
		String urlSpace = "";
		String palabraClaveDescargar = "";
		String stopWords = "";
		String correo = "";
		int numeroDePdf = 0;
		try {
			CommandLine line = parser.parse(buildOptions(), args);
			if (line.hasOption('h') || !line.hasOption('m') ||
					(!line.hasOption('u') || !line.hasOption('p') || !line.hasOption('k') || !line.hasOption('f') || !line.hasOption('t'))) {
				printHelp();
				System.exit(1);
			}
			if(line.hasOption('v') && !line.hasOption('v')) {
				System.out.println("ES NECESARIO INDICAR EL ARGUMENTO -t");
				System.exit(1);
			}
			mode = line.getOptionValue('m');
			if(!(mode.equals("COMPLETE") || mode.equals("TRAINING") || mode.equals("VECTOR") || mode.equals("INICIALES") || mode.equals("COMPROBAR"))) {
				System.out.println("Use one option on -m: TRAINING|VECTOR|COMPLETE");
				System.exit(1);
			}
			if((mode.equals("COMPLETE") || mode.equals("VECTOR"))&& !line.hasOption("v"))
				System.out.println("Es necesario indicar en modo VECTOR el argumento -v");
			user = line.getOptionValue('u');
			password = line.getOptionValue('p');
			carpeta = line.getOptionValue('f');
			keyWord = line.getOptionValue('k');
			if(line.hasOption('t')) {
				urlTopics = line.getOptionValue('t');
			}
			if(line.hasOption('v')) {
				urlSpace = line.getOptionValue('v');
			}
			if(line.hasOption('n')) {
				numeroDePdf = Integer.parseInt(line.getOptionValue('n'));
			}
			if(line.hasOption('c')) {
				correo = line.getOptionValue('c');
				Pattern pattern = Pattern
						.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
								+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
				Matcher mather = pattern.matcher(correo);
				if(!mather.find()) {
					System.out.println("The email is not correct");
					System.exit(1);
				}
			}
		}
		catch (ParseException exp) {
			exp.printStackTrace();
			System.err.println("Error: " + exp.getMessage());
			System.exit(1);
		}
		if(!palabraClaveDescargar.equals("")) {
			//new Application(carpeta,"", true).run();
			//System.out.println("EXTRACCION DE DARKSKY TARDA: " + (System.nanoTime() - inicio));
			//inicio = System.nanoTime();
			//new ApplicationZenodo(carpeta,keyWord,true).run();
		}
		List<EstructuraDelPDF> listaTexto = new ArrayList<EstructuraDelPDF>();
		List<File> misPDF = comprobacionArchivo(carpeta);
		if(numeroDePdf == 0) {
			numeroDePdf = misPDF.size();
		}
		List<RangeExtraction> numeros = new ArrayList<RangeExtraction>();
		File myCarpetaEnFila = new File(carpeta);
		boolean fixText = false;
		int j = 0;
		System.out.println("Processing PDFs to upload to librAlry ...");
		for (File file : misPDF) {
			System.out.print("Processing " + file.getName());
			if(j == numeroDePdf)
				break;
			try {
				ReadPDF informacionPDF = new ReadPDF(file,ExtractionMode.COMPLETE,numeros,numeros,fixText,myCarpetaEnFila,true);
				String textoPDF = informacionPDF.obtainOnlyText();
				if(textoIngles(textoPDF, file.getAbsolutePath())) {
					EstructuraDelPDF auxx = new EstructuraDelPDF(file.getName(), textoPDF);
					listaTexto.add(auxx);
					j++;
					System.out.println();
				}
				else {
					System.out.println("Discarded pdf. The pdf is not in english or does not contain enought information\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println();
		//TODO cambiar otra vez a LibrairyConnection paraUsar = new LibrairyConnection(listaTexto,stopWords,keyWord,urlTopics,urlSpace,user,password);
		LibrairyConnection paraUsar = new LibrairyConnection(listaTexto,stopWords,keyWord,urlTopics,urlSpace,user,password,correo);
		switch (mode) {
		case "COMPROBAR":
			LibrairyConnection paraComprobar = new LibrairyConnection(keyWord,urlTopics,urlSpace,user,password);
			for (int i = 0; i < listaTexto.size(); i++) {
				System.out.println();
				System.out.println("% DE EXITO " + paraComprobar.comprobarArchivo(listaTexto.get(i).getTexto()));
			}
			System.exit(0);
		case "INICIALES":
			inicializarTemas();
			//TODO cambiar a String
			TreeMap<Double, EstructuraDelPDF> tmap =  new TreeMap<Double, EstructuraDelPDF>();
			//TODO cambiar a String
			for (EstructuraDelPDF elTexto : listaTexto) {
				double puntuacion = devolverPuntuacion(elTexto.getTexto());
				if(puntuacion != 0.0) {
					if(tmap.size() < 25) {
						tmap.put(puntuacion, elTexto);
					}
					else {
						Set set = tmap.entrySet();
						Iterator iterator = set.iterator();
						if(iterator.hasNext()) {
							Map.Entry mentry = (Map.Entry)iterator.next();
							double primeraPuntuacion = (double) mentry.getKey();
							if(primeraPuntuacion<puntuacion) {
								//TODO eliminar dos lineas de referencia
								EstructuraDelPDF asdadsaa = (EstructuraDelPDF) tmap.get(mentry.getKey());
								System.out.println("Remplazo " + asdadsaa.getTitulo() + "p:" + mentry.getKey() + "por " + elTexto.getTitulo() + "p:" + puntuacion);
								tmap.remove(mentry.getKey(), mentry.getValue());
								tmap.put(puntuacion, elTexto);
							}
						}
					}
				}
			}
			Set set = tmap.entrySet();
			Iterator iterator = set.iterator();
			List<EstructuraDelPDF> losElegidos = new ArrayList<>();
			while(iterator.hasNext()) {
				Map.Entry mentry = (Map.Entry)iterator.next();
				System.out.println("Puntuacion :" + mentry.getKey());
				//TODO cambiar a string. Ponerlo mas compacto
				EstructuraDelPDF vivaespana = (EstructuraDelPDF)mentry.getValue();
				System.out.println("Titulo: " + vivaespana.getTitulo());
				System.out.println();
				losElegidos.add(vivaespana);
			}
			paraUsar = new LibrairyConnection(losElegidos,stopWords,keyWord,urlTopics,urlSpace,user,password,correo);
			System.out.println("Building the points");
			paraUsar.entrenarModelo();
			System.exit(0);
		case "VECTOR":
			System.out.println("Building the topics");
			paraUsar.sacarTopicos();
			System.exit(0);
		case "TRAINING":
			//TODO descomentar toda esta parte
			//
			//			System.out.println("Procedemos a construir los puntos");
			//			paraUsar.entrenarModelo();
			//			LibrairyConnection paraComprobar = new LibrairyConnection(keyWord,urlTopics,urlSpace,user,password);
			//			for (int i = 0; i < listaTexto.size(); i++) {
			//				System.out.println();
			//				System.out.println("% DE EXITO " + paraComprobar.comprobarArchivo(listaTexto.get(i)));
			//			}
			break;
		default:
			System.out.println("The mode entered is not correct");
			System.exit(1);
		}
	}

	private static double calcularPuntuacion(String texto, int numeroPaginas) {
		String trimmed = texto.trim();
		int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
		return numeroPaginas*0.3 + words*0.7;
	}

	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("PDFExtractor", "Mised argument", buildOptions(), "", true);
	}

	//	public static void estoEsParaCopiarEnOtroSitio() {
	//		//Esto es de arriba
	//		TreeMap<Double, String> listaPuntuacion = new TreeMap();
	//		//-----------------
	//		double puntuacion = calcularPuntuacion(textoPDF, numberPages);
	//		System.out.println(puntuacion);
	//		listaPuntuacion.put(puntuacion, textoPDF);
	//		
	//		//Esto es del training
	//		int dondeCogerBajo = ((int) listaPuntuacion.size()/2)-12;
	//		double mediaTotal = 0.0;
	//		List<String> primerosPuntos = new ArrayList<String>();
	//		int controlar = -1;
	//		for(Map.Entry<Double,String> entry : listaPuntuacion.entrySet()) {
	//			controlar++;
	//			mediaTotal += entry.getKey();
	//			if(controlar < dondeCogerBajo)
	//				continue;
	//			if(primerosPuntos.size() < 25) {
	//				System.out.println("Valor de punto " + entry.getKey());
	//				primerosPuntos.add(entry.getValue());
	//			}
	//		}
	//		System.out.println("La media total es de: " + mediaTotal);
	//		paraUsar = new LibrairyConnection(primerosPuntos,stopWords,keyWord,urlTopics,urlSpace,user,password);
	//	}

	public static Options buildOptions() {
		Options o = new Options();
		o.addOption("h", "help", false, "Indicate how yo use the program.");
		o.addOption(Option.builder("m")
				.longOpt("mode")
				.desc("[REQUIRED] Indicates a mode of use of the program: \n"
						+ "TRAINING: Building a model with topics \n"
						+ "VECTOR: Building points to indicate whether the PDF belongs or not \n"
						+ "COMPLETE: Make the two previous ones")
				.hasArg()
				.argName("TRAINING|VECTOR|COMPLETE")
				.build());
		o.addOption(Option.builder("f")
				.longOpt("folder")
				.desc("[REQUIRED] Path to the folder in which the PDFs are located")
				.hasArg()
				.argName("folder")
				.build());
		o.addOption(Option.builder("u")
				.longOpt("user")
				.desc("[REQUIRED] User to use in librAlry")
				.hasArg()
				.argName("librAlry user")
				.build());
		o.addOption(Option.builder("p")
				.longOpt("password")
				.desc("[REQUIRED] Password to use in librAlry")
				.hasArg()
				.argName("librAlry Password")
				.build());
		o.addOption(Option.builder("k")
				.longOpt("keyword")
				.desc("[REQUIRED] Keyword to use in librAlry")
				.hasArg()
				.argName("KeyWord")
				.build());
		o.addOption(Option.builder("n")
				.longOpt("number")
				.desc("Number of PDF you want to upload to your model. By default, all those present in the folder are used.")
				.hasArg()
				.argName("Number of PDF's")
				.build());
		o.addOption(Option.builder("t")
				.longOpt("training")
				.desc("[REQUIRED] URL to build a model with your topics")
				.hasArg()
				.argName("Training path")
				.build());
		o.addOption(Option.builder("v")
				.longOpt("vector")
				.desc("[Required in VECTOR mode] URL to upload your files to build the points")
				.hasArg()
				.argName("Vector path")
				.build());
		o.addOption(Option.builder("c")
				.longOpt("mail")
				.desc("Email address to notify the user of the topic construction")
				.hasArg()
				.argName("mail")
				.build());
		return o;
	}

	private static boolean textoIngles(String myTexto, String myPath) {
		//load all languages:
		List<LanguageProfile> languageProfiles;
		try {
			languageProfiles = new LanguageProfileReader().readAllBuiltIn();
			//build language detector:
			LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
					.withProfiles(languageProfiles)
					.build();
			//create a text object factory
			TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
			//query:
			TextObject textObject = textObjectFactory.forText(myTexto);
			Optional<LdLocale> lang = languageDetector.detect(textObject);
			String resultado = lang.toString();
			String elverdad = resultado.substring(resultado.length()-3, resultado.length()-1);
			if(elverdad.equals("en") || resultado.equals("Optional.absent()"))
				return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static String leoDeArchivo(String rutaFile) {
		File f = new File(rutaFile);
		if(f.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				return br.readLine();
			}catch(FileNotFoundException e1) {
				System.out.println("Hay un problema con el fichero de las stopwords");
				e1.printStackTrace();
			}catch(IOException e2) {
				System.out.println("Hay un problema con el fichero de las stopwords");
				e2.printStackTrace();
			}
		}
		System.out.println("El fichero de las stopwords no existe");
		System.exit(1);
		return "";
	}

	private static List<File> comprobacionArchivo(String archivo) {
		File pdfOrFolder = null;
		try {
			pdfOrFolder = new File(archivo);
			if(!pdfOrFolder.exists()) {
				System.out.println("The input file or folder doesn't exist");
				System.exit(1);
			}
		}catch(java.lang.NullPointerException e) {
			System.out.println("The input file or folder doesn't exist.");
			System.exit(1);
		}
		List<File> aDevolver = new ArrayList<>();
		if(pdfOrFolder.isDirectory()) {
			for (File isAPDF : pdfOrFolder.listFiles()) {
				String nombreArchivo = isAPDF.getName();
				try {
					String extension = isAPDF.getName().substring(nombreArchivo.length()-4, nombreArchivo.length());
					if(extension.equalsIgnoreCase(".pdf")){
						aDevolver.add(isAPDF); 
					}
				}catch(java.lang.StringIndexOutOfBoundsException e) {
					//Para evitar archivos con longitud menor a tres caracteres
				}
			}
			if(aDevolver.isEmpty()) {
				System.out.println("The folder doesn't contains a PDF.");
				System.exit(1);
			}
			return aDevolver; //Es un directorio
		}
		else {
			String nombreArchivo = pdfOrFolder.getName();
			try {
				String extension = nombreArchivo.substring(nombreArchivo.length()-4, nombreArchivo.length());
				if(extension.equals(".pdf")){
					aDevolver.add(pdfOrFolder);
					return aDevolver;
				}
			}catch(java.lang.StringIndexOutOfBoundsException e) {
				//Para evitar archivos con longitud menor a tres caracteres
			}
			aDevolver.add(pdfOrFolder);
			return aDevolver;
		}
	}

	private static void inicializarTemas(){
		String aa = "";
		try {
			aa = readFile("src/test/resources/topics.txt", StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Pattern pattern = Pattern.compile("\"description\": \"(.*?)\"");
		Matcher matcher = pattern.matcher(aa);
		listaConTopicos = new ArrayList<>();
		while(matcher.find()) {
			String resultados = matcher.group(1);
			String[] myArray = resultados.split(",");
			listaConTopicos.add(myArray);
		}
	}

	private static double devolverPuntuacion(String textoo) {
		String elTexto = textoo.toLowerCase();
		double puntuacion = 0;
		int original = 0;
		original = numeroDePalabras(elTexto);
		if(original < 503) {
			return 0.0;
		}
		for (String[] strings : listaConTopicos) {
			for (String string : strings) {
				int count = original - numeroDePalabras(elTexto.replace(string, ""));
				puntuacion += count;
			}
		}
		return puntuacion/(original*0.35);
	}

	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	private static int numeroDePalabras(String texto) {
		String trimmed = texto.trim();
		return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
	}

}
