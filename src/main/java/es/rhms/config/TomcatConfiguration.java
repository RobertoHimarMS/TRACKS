package es.rhms.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.ServletContext;

/**
 * Configuración de Tomcat para permitir múltiples partes en peticiones multipart.
 * Tomcat 11+ tiene un límite por defecto de 10 partes (maxPartCount).
 *
 * Esta configuración aumenta el límite a 50 partes para formularios con múltiples inputs.
 * Funciona tanto para desarrollo (Tomcat embebido) como para producción (Tomcat externo).
 */
@Configuration
public class TomcatConfiguration {

	private static final int MAX_PART_COUNT = 50;

	/**
	 * Configuración para Tomcat embebido (desarrollo con mvn spring-boot:run)
	 */
	@Bean
	public ServletContextInitializer tomcatMaxPartCountInitializer() {
		return new ServletContextInitializer() {
			@Override
			public void onStartup(ServletContext servletContext) {
				configureMaxPartCount(servletContext);
			}
		};
	}

	private void configureMaxPartCount(ServletContext servletContext) {
		try {
			ClassLoader classLoader = servletContext.getClass().getClassLoader();

			// Navegar desde ApplicationContextFacade hasta StandardContext
			Class<?> facadeClass = Class.forName("org.apache.catalina.core.ApplicationContextFacade", true, classLoader);
			Class<?> contextClass = Class.forName("org.apache.catalina.core.ApplicationContext", true, classLoader);
			Class<?> standardContextClass = Class.forName("org.apache.catalina.core.StandardContext", true, classLoader);

			if (!facadeClass.isInstance(servletContext)) {
				return;
			}

			// Obtener ApplicationContext desde la fachada
			Field contextField = facadeClass.getDeclaredField("context");
			contextField.setAccessible(true);
			Object applicationContext = contextField.get(servletContext);

			// Obtener StandardContext desde ApplicationContext
			Field contextField2 = contextClass.getDeclaredField("context");
			contextField2.setAccessible(true);
			Object standardContext = contextField2.get(applicationContext);

			if (!standardContextClass.isInstance(standardContext)) {
				return;
			}

			// Navegar: StandardContext -> Host -> Engine -> Service -> Connector[]
			Method getParentMethod = standardContextClass.getMethod("getParent");
			Object host = getParentMethod.invoke(standardContext);
			Object engine = getParentMethod.invoke(host);

			Method getServiceMethod = engine.getClass().getMethod("getService");
			Object service = getServiceMethod.invoke(engine);

			Method findConnectorsMethod = service.getClass().getMethod("findConnectors");
			Object[] connectors = (Object[]) findConnectorsMethod.invoke(service);

			// Configurar maxPartCount en cada connector
			for (Object connector : connectors) {
				Method setPropertyMethod = connector.getClass().getMethod("setProperty", String.class, String.class);
				setPropertyMethod.invoke(connector, "maxPartCount", String.valueOf(MAX_PART_COUNT));
			}

		} catch (Exception e) {
			// No es Tomcat o no se pudo configurar - ignorar silenciosamente
		}
	}

}