package niceligue;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@OpenAPIDefinition(
  servers = {
    @io.swagger.v3.oas.annotations.servers.Server(
      url = "/",
      description = "Default Server URL"
    ),
  }
)
@SpringBootApplication
public class NiceLigueCodingExerciseApplication {

  public static void main(String[] args) {
    SpringApplication.run(NiceLigueCodingExerciseApplication.class, args);
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
          .addMapping("/**")
          .allowedOriginPatterns(
            "https://*.app.github.dev",
            "http://localhost:*"
          )
          .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
          .allowedHeaders("*");
      }
    };
  }
}
