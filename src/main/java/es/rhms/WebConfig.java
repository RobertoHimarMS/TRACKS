package es.rhms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion para servir recursos estaticos desde ubicaciones multiples:
 * - Directorio externo para imagenes subidas en tiempo de ejecucion
 * - Classpath (WAR) para imagenes incluidas en el despliegue inicial
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:C:/TrackYours/uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve /imgs/** desde dos ubicaciones en orden:
        // 1. Directorio externo (imagenes subidas en ejecucion)
        // 2. Classpath (imagenes incluidas en el WAR)
        registry.addResourceHandler("/imgs/**")
                .addResourceLocations(
                        "file:" + uploadPath + "/",
                        "classpath:/static/imgs/"
                );
    }
}