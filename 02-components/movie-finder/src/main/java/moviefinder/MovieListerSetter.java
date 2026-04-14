package moviefinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Setter Injection — 适用于可选依赖
 */
@Component
public class MovieListerSetter {

    private MovieFinder finder;

    @Autowired
    public void setFinder(MovieFinder finder) {
        this.finder = finder;
    }

    public List<Movie> moviesDirectedBy(String director) {
        return finder.findAll().stream()
                .filter(movie -> movie.getDirector().equals(director))
                .collect(Collectors.toList());
    }
}
