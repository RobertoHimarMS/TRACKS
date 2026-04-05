package es.rhms.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadUtility {

	@Value("${upload.path:C:/TrackYours/uploads}")
	private String uploadPath;

	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	public String saveImage(MultipartFile file, String folder) throws IOException {
		// Validar que no esté vacío
		if (file == null || file.isEmpty()) {
			return null;
		}

		// Validar tamaño
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("El archivo excede el tamaño máximo de 10MB");
		}

		// Validar tipo MIME
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new IllegalArgumentException("Solo se permiten archivos de imagen");
		}

		// Generar nombre único
		String originalFilename = file.getOriginalFilename();
		String extension = originalFilename != null && originalFilename.contains(".")
				? originalFilename.substring(originalFilename.lastIndexOf("."))
				: ".jpg";
		String newFilename = UUID.randomUUID().toString() + extension;

		// Crear directorio si no existe
		Path uploadDir = Paths.get(uploadPath, folder);
		if (!Files.exists(uploadDir)) {
			Files.createDirectories(uploadDir);
		}

		// Guardar archivo
		Path filePath = uploadDir.resolve(newFilename);
		Files.copy(file.getInputStream(), filePath);

		return newFilename;
	}

	public void deleteImage(String filename, String folder) {
		if (filename == null || filename.isEmpty()) {
			return; // No hay imagen que borrar
		}
		try {
			Path filePath = Paths.get(uploadPath, folder, filename);
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			// Error al eliminar archivo - se queda huérfano, pero no rompe la app
			// Se puede limpiar manualmente o con un job programado
			System.err.println("No se pudo eliminar el archivo: " + filename + " en " + folder);
		}
	}
}