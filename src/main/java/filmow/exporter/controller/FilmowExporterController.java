package filmow.exporter.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import filmow.exporter.service.FilmowExporterService;

@Controller
@RequestMapping("/filmow")
public class FilmowExporterController {
	
	@Autowired
	FilmowExporterService exporterService;
	
	@GetMapping("/watchedMovies")
	public ResponseEntity<Object> retrieveProfileMoviesCSVfileWithRattings(@RequestParam("filmowUserName") String filmowUserName,@RequestParam ("userEmail") String userEmail) throws IOException {
		exporterService.retrieMoviesCSV(filmowUserName,userEmail);
		return ResponseEntity.ok().build();

	}

}
