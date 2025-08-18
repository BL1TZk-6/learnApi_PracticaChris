package IntegracionBackFront.backfront.Services.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {
    //Constante que define el tamaño máximo permitido para los archivos (5 mb)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    //Constante para definir los tipos de archivos adminitidos
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png"};
    //Cliente de cloudinary inyectado como dependencia
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    /**
     * subir imagenes a la raiz de cloudinary
     * @param file
     * @return URL de la imagen
     * @throws IOException
     */
    //Subir imagenes a la raiz
    public String uploadImage(MultipartFile file) throws IOException {
        //1. Validamos el archivo
        validateImage(file);

        //Sube el archive a Cloudinary con configuraciones básicas
        //Tipo de recurso auto-detectado
        //Calidad automática con nivel "good"
        Map<?,?> uploadResult = cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap(
                        "resource_type", "auto",
                        "quality", "auto:good"
                ));
        //Retorna la URL segura de la imagen
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Sube una imagen a una carpeta en especifico
     * @param file
     * @param folder carpeta destino
     * @return URL segura (HTTPS) de la imagen subido
     * @throws IOException Si ocurre un error durante la subida
     */
    public String UploadImage(MultipartFile file, String folder) throws IOException {
        validateImage(file);
        //Generar un nombre unico para el archivo
        //Conservar la extensión original
        //Agregar un prefijo y un UUID para evitar colisiones
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFilename = "img" + UUID.randomUUID() + fileExtension;

        //Configuración para subir imagen
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder, //Carpeta de destino
                "public_id", uniqueFilename, //Nombre unico para el archivo
                "use_filename", false, //No usar el nombre original
                "unique_filename", false,  //No generar nombre unico (Proceso hecho anteriormente)
                "overwrite", false, //No sobrescribir archivos
                "resource_type", "auto", //Auto-detectar tipo de recurso
                "quality", "auto:good" //Optiización de calidad automática
        );
        //subir archivo
        Map <?,?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        //Retornamos la URL segura
        return (String) uploadResult.get("secure_url");
    }

    /**
     *
     * @param file
     */
    private  void validateImage(MultipartFile file){
        //1. Verificar si el archivo está vacío
        if (file.isEmpty()){
            throw new IllegalArgumentException("El archivo no puede estar vacío.");
        }
        //2.Verificar el tamaño de la imagen
        if (file.getSize() > MAX_FILE_SIZE){
            throw new IllegalArgumentException("El archivo no puede ser mayor a 5MB");
        }

        //3. Obtener y validar el nombre original del archivo
        String origanlFileName = file.getOriginalFilename();
        if (origanlFileName == null) {
            throw new IllegalArgumentException("Nombre de archivo invalidado");
        }
        //4. Extraer y validar la extensión
        String extension = origanlFileName.substring(origanlFileName.lastIndexOf(".")).toLowerCase();
        if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(extension)){
            throw new IllegalArgumentException("Solo se permiten archivos JPG, JPEG, Y PNG");
        }
        //5. Verifica que el tipo de MIME sea una imagen
        if (!file.getContentType().startsWith("image/")){
            throw new IllegalArgumentException("El archivo debe ser una imagen valida");
        }
    }
}
