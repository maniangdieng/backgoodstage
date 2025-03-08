package gestion_voyage.gestion_voyage.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**") // Autorise les routes sous /api
      .allowedOrigins("http://localhost:4200") // Autorise uniquement localhost:4200
      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Méthodes autorisées
      .allowedHeaders("*") // Autorise tous les en-têtes
      .allowCredentials(true); // Autorise les cookies et les en-têtes d'authentification
  }
}
