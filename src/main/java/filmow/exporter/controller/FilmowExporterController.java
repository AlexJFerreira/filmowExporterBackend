package filmow.exporter.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import filmow.exporter.service.FilmowExporterService;

@Controller
@RequestMapping("/filmow")
public class FilmowExporterController {
	
	@Autowired
	FilmowExporterService exporterService;
	
	@GetMapping("/user/{filmowUserName}/email/{userEmail}")
	public ResponseEntity<Object> retrieveProfileMoviesCSVfileWithRattings(@PathVariable String filmowUserName,@PathVariable String userEmail) throws IOException {
		exporterService.retrieMoviesCSV(filmowUserName,userEmail);
		return ResponseEntity.ok().build();

	}

}
