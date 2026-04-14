package moviefinder;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MovieFinderTest {

    @Configuration
    @ComponentScan(basePackages = "moviefinder")
    static class AppConfig {
    }

    /**
     * Constructor Injection（Annotation 方式）
     */
    @Test
    public void testConstructorInjection() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            MovieLister lister = ctx.getBean(MovieLister.class);
            List<Movie> movies = lister.moviesDirectedBy("Sergio Leone");

            assertEquals(2, movies.size());
            assertEquals("Once Upon a Time in the West", movies.get(0).getTitle());
        }
    }

    /**
     * Setter Injection（Annotation 方式）
     */
    @Test
    public void testSetterInjection() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            MovieListerSetter lister = ctx.getBean(MovieListerSetter.class);
            List<Movie> movies = lister.moviesDirectedBy("Quentin Tarantino");

            assertEquals(2, movies.size());
            assertEquals("Pulp Fiction", movies.get(0).getTitle());
        }
    }

    /**
     * XML 配置方式（对应 slides 中的 spring.xml 示例）
     */
    @Test
    public void testXmlConfiguration() {
        try (ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml")) {
            MovieLister lister = (MovieLister) ctx.getBean("MovieLister");
            List<Movie> movies = lister.moviesDirectedBy("Sergio Leone");

            assertEquals(2, movies.size());
            assertEquals("Once Upon a Time in the West", movies.get(0).getTitle());
        }
    }

    // ========== 灵活组装：同一个 MovieLister，换不同的 MovieFinder 实现 ==========

    /**
     * Java Config 方式：手动装配 InMemoryMovieFinder
     * 同样的 MovieLister 代码，换了数据来源，零修改。
     */
    @Configuration
    static class InMemoryConfig {
        @Bean
        public MovieFinder movieFinder() {
            return new InMemoryMovieFinder(
                    new Movie("Inception", "Christopher Nolan"),
                    new Movie("The Dark Knight", "Christopher Nolan"),
                    new Movie("Interstellar", "Christopher Nolan")
            );
        }

        @Bean
        public MovieLister movieLister(MovieFinder finder) {
            return new MovieLister(finder);
        }
    }

    @Test
    public void testSwapImplementation_JavaConfig() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(InMemoryConfig.class)) {
            MovieLister lister = ctx.getBean(MovieLister.class);
            List<Movie> movies = lister.moviesDirectedBy("Christopher Nolan");

            assertEquals(3, movies.size());
            assertEquals("Inception", movies.get(0).getTitle());
        }
    }

    /**
     * XML 方式：spring-inmemory.xml 把 MovieFinder 换成 InMemoryMovieFinder
     * MovieLister 代码完全不变，只改配置文件。
     */
    @Test
    public void testSwapImplementation_Xml() {
        try (ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring-inmemory.xml")) {
            MovieLister lister = (MovieLister) ctx.getBean("MovieLister");
            List<Movie> movies = lister.moviesDirectedBy("Christopher Nolan");

            assertEquals(3, movies.size());
            assertEquals("Inception", movies.get(0).getTitle());
        }
    }
}
