package moviefinder;

import java.util.Arrays;
import java.util.List;

/**
 * 另一种 MovieFinder 实现：电影数据直接存储在内存中。
 * 没有 @Component 注解 —— 通过 XML 或 Java Config 手动装配，
 * 演示同一个 MovieLister 可以灵活搭配不同的 MovieFinder 实现。
 */
public class InMemoryMovieFinder implements MovieFinder {

    private final List<Movie> movies;

    public InMemoryMovieFinder(List<Movie> movies) {
        this.movies = movies;
    }

    public InMemoryMovieFinder(Movie... movies) {
        this.movies = Arrays.asList(movies);
    }

    @Override
    public List<Movie> findAll() {
        return movies;
    }
}
