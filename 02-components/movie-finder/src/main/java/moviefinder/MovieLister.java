package moviefinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Constructor Injection — Spring 官方推荐方式
 */
@Component
public class MovieLister {

    private final MovieFinder finder;

    @Autowired
    public MovieLister(MovieFinder finder) {
        this.finder = finder;
    }

    public List<Movie> moviesDirectedBy(String director) {
        return finder.findAll().stream()
                .filter(movie -> movie.getDirector().equals(director))
                .collect(Collectors.toList());
    }
}
