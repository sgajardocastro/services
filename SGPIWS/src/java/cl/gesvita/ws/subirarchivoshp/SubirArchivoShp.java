package cl.gesvita.ws.subirarchivoshp;

import cl.gesvita.ws.archivoshp.bean.db.InterchangeFile;
import cl.gesvita.ws.archivoshp.bean.db.InterchangeFileOut;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.subirarchivoshp.util.BinaryFiles;
import cl.gesvita.ws.subirarchivoshp.util.CallProcess;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.util.http.fileupload.IOUtils;

/**
 * Servlet implementation class SubirArchivo
 * "/WSSubirArchivoShp/SubirArchivoShp"
 */
@WebServlet("/WSSubirArchivoShp/SubirArchivoShp")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 100, // 100Mb (era 10MB)
        maxRequestSize = 1024 * 1024 * 1024 * 30)   // 50Gb (era 50MB)
public class SubirArchivoShp extends HttpServlet {

    private static final long serialVersionUID = 26535612783564752L;
    static Logger logger = Logger.getLogger(SubirArchivoShp.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubirArchivoShp() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    private static final int STAT_PENDING_OF_WRITE = 1;
    private static final int STAT_PENDING_OF_READ = 2;
    private static final int STAT_WRITING = 3;
    private static final int STAT_READING = 4;
    private static final int STAT_WRITTEN = 5;
    private static final int STAT_READY_TO_READ = 6;
    private static final String TIPO_DOC = "DOCUMENTO";

    private static final char alfabeto[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    private static final int NCHAR_ALFABETO = 32;
    private static final int LEN_FILESRANDOM = 20;

    private static String genRandomString() {
        return genRandomString(LEN_FILESRANDOM);
    }

    private static String genRandomString(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(alfabeto[(int) Math.floor(Math.random() * NCHAR_ALFABETO)]);
        }
        return sb.toString();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        // Properties prop = new Properties();
        Properties propsLog4 = new Properties();

        response.setHeader("Access-Control-Allow-Origin", "*");

        // Variables para la salida
        String ResultadoEjecucion = "NOK";
        String mensajeUsr = "Ocurrio un error";
        String mensajeTec;
        String inputFileName;
        String mimetype;
        String newFileName = "";
        InterchangeFileOut salida = null;

        try {
            // Leer archivos de propiedades
            InputStream inputStreamLog4 = this.getClass().getResourceAsStream("log4j.properties");
            propsLog4.load(inputStreamLog4);
            PropertyConfigurator.configure(propsLog4);
            // BYSECURITY logger.debug("en /WSSubirArchivoShp/SubirArchivoShp");

            // BYSECURITY logger.debug("properties loaded");
            // BYSECURITY logger.debug("URL '" + p.getProperty("urlConexion").trim() + "'");
            // BYSECURITY logger.debug("Driver '" + p.getProperty("driverClass").trim() + "'");
            // BYSECURITY logger.debug("User '" + p.getProperty("userName").trim() + "'");
            // BYSECURITY logger.debug("passwd " + ((p.getProperty("password").equalsIgnoreCase("")) ?  "don't exist" : "Setted"));
            // BYSECURITY logger.debug("properties setted");
            int numparts = 0;

            // BYSECURITY logger.debug("Nro partes : " + request.getParts().size());
            // Recorrer las partes ingresadas
            for (Part part : request.getParts()) {
                // BYSECURITY logger.debug("leyendo part");
                inputFileName = extractFileName(part);
                if (inputFileName.equalsIgnoreCase("")) {
                    // BYSECURITY logger.debug("part sin archivo (ignored)");
                    continue;
                }
                numparts++;
                mimetype = part.getContentType();
                // mimetype = URLConnection.guessContentTypeFromName(inputFileName);
                // Puede ser  part.getContentType()
                // BYSECURITY logger.debug("Que mimetype alt1:" + mimetype + ", alt2:" + part.getContentType());
                newFileName = genRandomString() + "." + getExtension(inputFileName);
                // BYSECURITY logger.debug("part name :" + inputFileName);
                // llamar a escribir el archivo en base de datos 17-07-2018 fcruz
                salida = storePart(part, inputFileName, newFileName);
                // fileName2 = fileName;
                
                CallProcess.callCopy(logger, salida.getId());
                if (salida.getCode() != -1) {
                    // BYSECURITY logger.info("Archivo guardado con exito en " + newFileName);
                }
                break;
            }
            response.setContentType("application/json");
            if (numparts == 0) {
                out.print("{\"resultadoEjecucion\":\"NOK\",\"message\":\"Archivo no selecionado\",\"messageTec\":\"No se ingreso ningun archivo\"}");
                return;
            }

            try {
                if (salida == null) {
                    out.print("{\"resultadoEjecucion\":\"NOK\",\"message\":\"No pudo ser almacenado el archivo\",\"messageTec\":\"Sin salida del proceso\"}");
                } else if (salida.getCode() == 0) {
                    String salidaOK ="{\"resultadoEjecucion\":\"OK\",\"idcache\":\"" + salida.getId() + "\",\"archivo\":\"" + newFileName + "\",\"rutaArchivo\":\"" + newFileName + "\"}"; 
                    out.print(salidaOK);
                } else {
                    out.print("{\"resultadoEjecucion\":\"NOK\",\"message\":\"No pudo ser almacenado el archivo\",\"messageTec\":\"SQLExeption\"}");
                    throw new GesvitaException("ERROR SQL: (" + salida.getCode() + ") " + salida.getDescription());
                }
            } catch (GesvitaException ex) {
                logger.error(ex.getMessage());
            }
            return;
        } catch (IOException e) {
            logger.error("Error durante el proceso del archivo: " + e.getMessage());
            response.setContentType("application/json");
            out.print("{\"resultadoEjecucion\":\"" + ResultadoEjecucion + "\",\"mensaje\":\"" + mensajeUsr + "\",\"mensajeTec\":\"IOException\"}");
        } catch (ServletException e) {
            logger.error("Error durante el proceso web: " + e.getMessage());
            response.setContentType("application/json");
            out.print("{\"resultadoEjecucion\":\"" + ResultadoEjecucion + "\",\"mensaje\":\"" + mensajeUsr + "\",\"mensajeTec\":\"ServletException\"}");

//        } catch (Exception e) {
//            logger.error("Error : ", e.fillInStackTrace());
//            response.setContentType("application/json");
//            out.print("{\"resultadoEjecucion\":\"" + ResultadoEjecucion + "\",\"mensaje\":\"" + mensajeUsr + "\",\"mensajeTec\":\"Exception\"}");
        }

    }

    public String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "";
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Max-Age", "86400");
        response.setHeader("Allow", "GET, HEAD, POST, TRACE, OPTIONS");
    }

    private InterchangeFileOut storePart(Part part, String inputFileName, String newFileName) throws IOException {

        String mimetype = part.getContentType();
        // String ext = getExtension(inputFileName);
        // String mimetype = URLConnection.guessContentTypeFromName(inputFileName);
        // Puede ser  part.getContentType()
        // BYSECURITY logger.debug("Que mimetype alt1:" + mimetype + ", alt2:" + part.getContentType());
        // String newFileName = genRandomString() + "." + ext;
        // BYSECURITY logger.debug("part name :" + inputFileName);
        // llamar a escribir el archivo en base de datos 17-07-2018 fcruz
        // String content = slurp(part.getInputStream());

//        // Para la generación de archivo temporal 
//        File tempFile = File.createTempFile(genRandomString(3),".swp");
//        String tempFileName = tempFile.getAbsolutePath();
//        tempFile.deleteOnExit();
//        part.write(tempFileName);
        // logger.info("Comienzo : " + content.substring(0,50));
        // logger.info("# bytes leidos = " + content.length() );
        InterchangeFile fileObj = new InterchangeFile();
        // fileObj.setContent(content);
        fileObj.setMimetype(mimetype);
        fileObj.setName(inputFileName);
        fileObj.setFinalname(newFileName);
        fileObj.setEstado(STAT_PENDING_OF_WRITE);

        // BYSECURITY logger.debug("Objeto Setted");
        InterchangeFileOut salida = storePartDB(fileObj,
                 slurpbin(part.getInputStream()));

        // InterchangeFileDao dao = new InterchangeFileDao();
        // dao.putInterchangeFile(logger, fileObj);
        // BYSECURITY logger.debug("Resultado de consulta : " + salida.toString());
        return salida;
    }

    private InterchangeFileOut storePartDB(InterchangeFile fileData, String hexString) throws IOException {
        Connection conn = null;
        CallableStatement stmt = null;
        InterchangeFileOut salida = new InterchangeFileOut(0, -1, "");

        DataSource datasource = getDataSource();
        try {
            conn = datasource.getConnection();
            // BYSECURITY logger.debug("Preparing stament");
            stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.PROYECTO_CACHE_ADD(?,?,?,?,?,?,?,?,?)}");
            stmt.setString(1, TIPO_DOC);
            stmt.setInt(2, fileData.getEstado());
            stmt.setString(3, hexString);  // Archivo en Hexadecimal
            stmt.setString(4, fileData.getMimetype());
            stmt.setString(5, fileData.getName());
            stmt.setString(6, fileData.getFinalname());
            stmt.registerOutParameter(7, Types.NUMERIC);
            stmt.registerOutParameter(8, Types.NUMERIC);
            stmt.registerOutParameter(9, Types.VARCHAR);
            stmt.execute();
            salida.setId(stmt.getInt(7));
            salida.setCode(stmt.getInt(8));
            salida.setDescription(stmt.getString(9).toString());

        } catch (Exception e) {
            logger.error("storePartDB : Exception: ", e.fillInStackTrace());
            StringBuilder sb = new StringBuilder();
        } finally {
            if (datasource != null) {
                try {
                    datasource.close();
                } catch (Exception e) {
                    logger.error("datasource.close() : Exception: ", e.fillInStackTrace());
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error("stmt.close() : Exception: ", e.fillInStackTrace());
                }
            }
        }
        return salida;
    }

    private DataSource getDataSource() throws FileNotFoundException, IOException {

        String archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/db.properties";

        Properties prop = new Properties();
        PoolProperties p = new PoolProperties();
        prop.load(new FileInputStream(archivoPro));

        p.setUrl(prop.getProperty("urlConexion"));
        p.setDriverClassName(prop.getProperty("driverClass"));
        p.setUsername(prop.getProperty("userName"));
        p.setPassword(prop.getProperty("password"));
        p.setJmxEnabled(Boolean.parseBoolean(prop.getProperty("jmxEnabled")));
        p.setTestWhileIdle(Boolean.parseBoolean(prop.getProperty("testWhileIdle")));
        p.setTestOnBorrow(Boolean.parseBoolean(prop.getProperty("testOnBorrow")));
        p.setValidationQuery(prop.getProperty("validationQuery"));
        p.setTestOnReturn(Boolean.parseBoolean(prop.getProperty("testOnReturn")));
        p.setValidationInterval(Long.parseLong(prop.getProperty("validationInterval")));
        p.setTimeBetweenEvictionRunsMillis(Integer.parseInt(prop.getProperty("timeBetweenEvictionRunsMillis")));
        p.setMaxActive(Integer.parseInt(prop.getProperty("maxActive")));
        p.setInitialSize(Integer.parseInt(prop.getProperty("initialSize")));
        p.setMaxWait(Integer.parseInt(prop.getProperty("maxWait")));
        p.setRemoveAbandonedTimeout(Integer.parseInt(prop.getProperty("removeAbandonedTimeout")));
        p.setMinEvictableIdleTimeMillis(Integer.parseInt(prop.getProperty("minEvictableIdleTimeMillis")));
        p.setMinIdle(Integer.parseInt(prop.getProperty("minIdle")));
        p.setLogAbandoned(Boolean.parseBoolean(prop.getProperty("logAbandoned")));
        p.setRemoveAbandoned(Boolean.parseBoolean(prop.getProperty("removeAbandoned")));
        p.setJdbcInterceptors(
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);

        return datasource;
    }

    private String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    public static String slurpbin(final InputStream is) throws UnsupportedEncodingException, IOException {
        final int bufferSize = 8196;
        final byte[] buffer = new byte[bufferSize];
        final StringBuilder out = new StringBuilder();
        DataInputStream in = new DataInputStream(is);
        for (;;) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0) {
                break;
            }
            out.append(BinaryFiles.bytesToHex(buffer, rsz));
        }
        return out.toString();
    }

    /**
     * Read and return the entire contents of the supplied
     * {@link InputStream stream}. This method always closes the stream when
     * finished reading.
     *
     * @param stream the stream to the contents; may be null
     * @return the contents, or an empty byte array if the supplied reader is
     * null
     * @throws IOException if there is an error reading the content
     */
    public static byte[] readBytes(InputStream stream) throws IOException {
        if (stream == null) {
            return new byte[]{};
        }
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean error = false;
        try {
            int numRead = 0;
            while ((numRead = stream.read(buffer)) > -1) {
                output.write(buffer, 0, numRead);
            }
        } catch (IOException e) {
            error = true; // this error should be thrown, even if there is an error closing stream
            throw e;
        } catch (RuntimeException e) {
            error = true; // this error should be thrown, even if there is an error closing stream
            throw e;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                if (!error) {
                    throw e;
                }
            }
        }
        output.flush();
        return output.toByteArray();
    }
}
