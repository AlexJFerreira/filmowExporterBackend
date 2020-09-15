package filmow.exporter.service;

import static filmow.exporter.util.ApplicationUtils.getFormatedUrl;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.apache.commons.lang3.StringUtils.trim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.MessagingException;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;

@Service
public class FilmowExporterService {
	
	@Autowired
	private EmailService emailService;
	
	private final static String ALREADY_WATCHED_MOVIES_URL = "https://filmow.com/usuario/%s/filmes/ja-vi/";
	private final static String ALREADY_WATCHED_MOVIES_URL_WITH_INDEX = "https://filmow.com/usuario/%s/filmes/ja-vi/?pagina=%s";
	private final static String FILMOW_ROOT_URL = "https://filmow.com%s";
	
	private static final Logger logger = LoggerFactory.getLogger(FilmowExporterService.class);

		
	public void retrieMoviesCSV(String filmowUserName) throws IOException {
		logger.info("Iniciando recuperação de filmes no filmow para arquivo CSV");
		final Map<String, String> mapaFilmes = new HashMap<String, String>();
        final List<Triple<String, String, String>> dadosFilmes = new ArrayList<>();
        
		final String rootHtmlUserPage = getHtmlPage(getFormatedUrl(ALREADY_WATCHED_MOVIES_URL, filmowUserName));
		
		final Integer pagesNumber = getTotalOfPages(rootHtmlUserPage);
		
		logger.info("Iniciando recuperação de dados iniciais dos filmes");
		for (int pageIndex = 1; pageIndex <= 2; pageIndex++) {
			logger.info("Recuperando página {}/{}",pageIndex,pagesNumber);

			String paginafilmes = getHtmlPage(getFormatedUrl(ALREADY_WATCHED_MOVIES_URL_WITH_INDEX,filmowUserName,valueOf(pageIndex)));
			mapaFilmes.putAll(getUrlsFilmes(paginafilmes)); 		
		}	
		
		Integer filmeNumeroAtual = 1;
		logger.info("Iniciando recuperação de dados de cada filme");
		for (Map.Entry<String, String> entry : mapaFilmes.entrySet()) {
			
			logger.info("Recuperando Dados do filme: {}",entry.getKey());
			logger.info("Filme {}/{} ",filmeNumeroAtual,mapaFilmes.size());
			logger.info("----------------------------------------------------------");

			final String movieHTMLPage = getHtmlPage( new URL(format(FILMOW_ROOT_URL, entry.getKey())));
			final Document doc = Jsoup.parse(movieHTMLPage);
			final String year;
			final Elements yearElement = doc.getElementsByClass("release"); 
		
			if(!yearElement.select("small").eachText().isEmpty()) {
				year = yearElement.select("small").eachText().get(0);				
			}else {
				year = "";
			}
			
			final Elements originalTitleElement = doc.getElementsByClass("movie-original-title"); 			
			final String originaltitle = originalTitleElement.select("h2").eachText().get(0);

			dadosFilmes.add(new ImmutableTriple<String, String, String>(originaltitle,year,entry.getValue()));
			filmeNumeroAtual++;
		}
		File csv = generateCSV(dadosFilmes);
		
		try {
			emailService.sendEmailWithAttachment(csv);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Arquivo gerado com sucesso");

	}

	private File generateCSV(List<Triple<String, String, String>> dadosFilmes) {
		logger.info("Iniciando geração do CSV");
		List<String[]> dataLines = new ArrayList<>();
		dataLines.add(new String[] { "Title", "Year", "Rating"});

		for (Triple<String, String, String> filme : dadosFilmes) {
			dataLines.add(new String[] {filme.getLeft(),filme.getMiddle(),filme.getRight()});			
		}
		
		final File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
		final File csvOutputFile = new File(tempDirectory.getAbsolutePath() +"/filmes.csv");
	   
		try (PrintWriter pw = new PrintWriter(csvOutputFile,StandardCharsets.ISO_8859_1)) {
	        dataLines.stream()
	          .map(this::convertToCSV)
	          .forEach(pw::println);
	        
	        Files.touch(csvOutputFile);
	        return csvOutputFile;
	        
	    } catch (IOException e) {
	    	logger.error("Falha ao gerar arquivo CSV");
	    	return null;
		}
		
		
	}
	
	public String convertToCSV(String[] data) {
	    return Stream.of(data)
	      .map(this::escapeSpecialCharacters)
	      .collect(Collectors.joining(","));
	}
	
	public String escapeSpecialCharacters(String data) {
		if(data == null) {
			return "";
		}
	    String escapedData = data.replaceAll("\\R", " ");
	    if (data.contains(",") || data.contains("\"") || data.contains("'")) {
	        data = data.replace("\"", "\"\"");
	        escapedData = "\"" + data + "\"";
	    }
	    return escapedData;
	}

	private Map<String, String> getUrlsFilmes(String paginafilmes) {
		Map<String, String> map = new HashMap<String, String>();

		Document doc = Jsoup.parse(paginafilmes);
		Elements links = doc.getElementsByClass("span2 movie_list_item");  
				
		for (Element link : links) {
			Element userRatingDiv = link.select("div.user-rating").first();
			Elements span = userRatingDiv.select("span.tip");
			
			Element linka = link.select("a").first();
			String linkHref = linka.attr("href");
			
			map.put(linkHref, trim(substringBetween(span.attr("title"), "Nota:", "estrela")));

		}

		return map;
	}

	private Integer getTotalOfPages(String rootHtmlUserPage) {
		Integer qtdPaginas = Integer.parseInt(substringAfterLast(substringBeforeLast(rootHtmlUserPage, "\" title=\"última página\""),"="));
		return qtdPaginas;
	}

	private String getHtmlPage(URL url) throws IOException {
		String urlContentString;
		
        try (var urlContent = new BufferedReader(new InputStreamReader(url.openStream()))) {

            String line;

            var sb = new StringBuilder();

            while ((line = urlContent.readLine()) != null) {

                sb.append(line);
                sb.append(System.lineSeparator());
            }
            
            urlContentString = sb.toString();
        
    }
		return urlContentString;
	}

}
