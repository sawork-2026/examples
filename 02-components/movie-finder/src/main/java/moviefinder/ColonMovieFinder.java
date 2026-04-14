package moviefinder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@Primary
public class ColonMovieFinder implements MovieFinder {

    @Value("movies.txt")
    private String filename;

    public ColonMovieFinder() {
    }

    public ColonMovieFinder(String filename) {
        this.filename = filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public List<Movie> findAll() {
        List<Movie> movies = new ArrayList<>();
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new RuntimeException("File not found: " + filename);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    movies.add(new Movie(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading " + filename, e);
        }
        return movies;
    }
}
