package es.rhms;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(TracksApplication.class);
	}

	@Override
	public void onStartup(jakarta.servlet.ServletContext servletContext) throws jakarta.servlet.ServletException {
		super.onStartup(servletContext);

		// Configurar multipart para despliegue en Tomcat externo
		ServletRegistration.Dynamic dispatcherServlet = (ServletRegistration.Dynamic) servletContext.getServletRegistration("dispatcherServlet");
		if (dispatcherServlet != null) {
			dispatcherServlet.setMultipartConfig(
				new MultipartConfigElement(
					null,           				// location - temp directory
					10 * 1024 * 1024, 				// maxFileSize - 10MB
					10 * 1024 * 1024, 				// maxRequestSize - 10MB
					0               				// fileSizeThreshold - 0 bytes before writing to disk
				)
			);
		}
	}

}