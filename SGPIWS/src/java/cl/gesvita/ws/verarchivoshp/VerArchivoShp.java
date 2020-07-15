package cl.gesvita.ws.verarchivoshp;

import cl.gesvita.ws.archivoshp.bean.db.InterchangeFile;
import cl.gesvita.util.ClobUtil;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.subirarchivoshp.util.BinaryFiles;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * Servlet implementation class VerArchivoShp "/WSVerArchivoShp/VerArchivoShp"
 */
@WebServlet("/WSVerArchivoShp/VerArchivoShp")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50)   // 50MB
public class VerArchivoShp extends HttpServlet {

    private static final long serialVersionUID = 26535612783564752L;
    static Logger logger = Logger.getLogger(VerArchivoShp.class);
    private static String mensajeError = "No se pudo obtener archivo";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public VerArchivoShp() {
        super();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        // PrintWriter out = response.getWriter(); Metodo POST
        // OutputStream out = response.getOutputStream();  metodo original
        // Properties prop = new Properties();
        Properties propsLog4 = new Properties();
        response.setHeader("access-control-allow-origin", "*");

        // response.setHeader("Access-Control-Allow-Origin", "*");

        // Variables para la salida
        String ResultadoEjecucion = "NOK";
        String mensajeTec;
        InterchangeFile salida = null;

        try {
            // Leer archivos de propiedades Log4j
            InputStream inputStreamLog4 = this.getClass().getResourceAsStream("log4j.properties");
            propsLog4.load(inputStreamLog4);
            PropertyConfigurator.configure(propsLog4);
            // BYSECURITY logger.debug("en /WSVerArchivoShp/VerArchivoShp");

            // Leer la cadena de entrada
            // JsonObject jsonObj = LeerEntradaJson(request);  req.getParameter("id_cache");

            // Determinar el id de archivo que se solicita
            // int id_archivo = Integer.parseInt(jsonObj.get("in_archivo").toString().replace("\"", ""));
            String strId = req.getParameter("id_cache");
            if (strId == null || strId.equalsIgnoreCase("")) {
                PrintWriter out1 = response.getWriter();
                response.setContentType("application/json");
                out1.write("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Falta valor de 'id_cache'\",\"mensajeTec\":\"Falta valor de 'id_cache'\"}");
                return;
            }
            int id_archivo;
            try {
                id_archivo = Integer.parseInt(strId);
            } catch (NumberFormatException ex) {
                PrintWriter out1 = response.getWriter();
                response.setContentType("application/json");
                out1.write("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Falta valor de 'id_cache' numerico\",\"mensajeTec\":\"Falta valor de 'id_cache'\"}");
                return;
            }

            // Consultar la base de datos por el id
            salida = ConsultaArchivoDB(id_archivo, response);

            if (salida.getEstado() != STAT_IN_DISK)
                EscribirSalida(salida, response);
            else
                EscribirSalidaDesdeArchivo(salida,response);

        } catch (IOException e) {
            PrintWriter out = response.getWriter();
            logger.error("Error durante el proceso del archivo:", e.fillInStackTrace());
            mensajeTec = "IOException";
            response.setContentType("application/json");
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"" + mensajeError + "\",\"mensajeTec\":\"" + mensajeTec + "\"}");
        }  catch (Exception e) {
            showFullError(e);
            mensajeTec = "ServletException:" + e.getMessage();
            PrintWriter out1 = response.getWriter();
            response.setContentType("application/json");
            out1.print("{\"resultadoEjecucion\":\"" + ResultadoEjecucion + "\",\"mensaje\":\"" + mensajeError + "\",\"mensajeTec\":\"" + e.getClass().toString() + "\"}");
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        // TODO Auto-generated method stub
//        response.getWriter().append("Served at: ").append(request.getContextPath());
//    }

    private static final int STAT_PENDING_OF_WRITE = 1;
    private static final int STAT_PENDING_OF_READ = 2;
    private static final int STAT_WRITING = 3;
    private static final int STAT_READING = 4;
    private static final int STAT_WRITTEN = 5;
    private static final int STAT_READY_TO_READ = 6;
    private static final int STAT_IN_DISK = 7;
    private static final String TIPO_DOC = "DOCUMENTO";

    private static final char alfabeto[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

//    private static final int NCHAR_ALFABETO = 32;
//    private static final int LEN_FILESRANDOM = 20;

//    private static String genRandomString() {
//        return genRandomString(LEN_FILESRANDOM);
//    }
//
//    private static String genRandomString(int len) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < len; i++) {
//            sb.append(alfabeto[(int) Math.floor(Math.random() * NCHAR_ALFABETO)]);
//        }
//        return sb.toString();
//    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // PrintWriter out = response.getWriter();
        // Properties prop = new Properties();
        Properties propsLog4 = new Properties();

        response.setHeader("Access-Control-Allow-Origin", "*");

        // Variables para la salida
        String mensajeTec;
        InterchangeFile salida = null;

        try {
            // Leer archivos de propiedades Log4j
            InputStream inputStreamLog4 = this.getClass().getResourceAsStream("log4j.properties");
            propsLog4.load(inputStreamLog4);
            PropertyConfigurator.configure(propsLog4);
            // BYSECURITY logger.debug("en /WSVerArchivoShp/VerArchivoShp");

            // Leer la cadena de entrada
            JsonObject jsonObj = LeerEntradaJson(request);

            // Determinar el id de archivo que se solicita
            int id_archivo = Integer.parseInt(jsonObj.get("in_archivo").toString().replace("\"", ""));

            // Consultar la base de datos por el id
            salida = ConsultaArchivoDB(id_archivo, response);

            if (salida.getEstado() != STAT_IN_DISK)
                EscribirSalida(salida, response);
            else
                EscribirSalidaDesdeArchivo(salida,response);
            
        } catch (IOException e) {
            PrintWriter out = response.getWriter();
            logger.error("Error durante el proceso del archivo:", e.fillInStackTrace());
            mensajeTec = "IOException";
            response.setContentType("application/json");
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"" + mensajeError + "\",\"mensajeTec\":\"" + mensajeTec + "\"}");
        } catch (GesvitaException e) {
            PrintWriter out = response.getWriter();
            logger.error("Error durante el proceso del archivo:", e.fillInStackTrace());
            mensajeTec = "GesvitaException";
            response.setContentType("application/json");
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"" + mensajeError + "\",\"mensajeTec\":\"" + mensajeTec + "\"}");
        }
    }

    private JsonObject LeerEntradaJson(HttpServletRequest request) throws IOException, JsonSyntaxException {

        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = request.getReader().readLine()) != null) {
            sb.append(s);
        }
      // BYSECURITY logger.info("Data Entrada Antes:");
      // BYSECURITY logger.info(sb);
        String requestS = sb.toString();
      // BYSECURITY logger.info("Data Extendida con toString:");
      // BYSECURITY logger.info(requestS);
        JsonParser parser = new JsonParser();
        JsonObject jsonObj = (JsonObject) parser.parse(requestS);
        return jsonObj;
    }

    
    private InterchangeFile ConsultaArchivoDB(int id, HttpServletResponse response) throws IOException {
        Connection conn;
        PreparedStatement stmt = null;
        InterchangeFile salida = null;
        ResultSet rs;
        int totalRegistro = 0;
        DataSource datasource = null;
        try {
            datasource = getDataSource();
            conn = datasource.getConnection();
            
            stmt = conn.prepareStatement("SELECT ID_CACHE, TIME_CREATION, TIME_UPDATE"
                    + " ,DESC_CONTENIDO, DESC_MIMETYPE, NOMBRE_ORIGINAL, NOMBRE_FINAL"
                    + ",ID_ESTADO_CACHE"
                    + " FROM tb_proyecto_cache WHERE ID_CACHE = ?");
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            salida = new InterchangeFile();
            salida.setId(id);
            if (rs.next()) {
                salida.setEstado(rs.getInt("ID_ESTADO_CACHE"));
                salida.setTimeCreation(rs.getTimestamp("TIME_CREATION"));
                salida.setTimeUpdate(rs.getTimestamp("TIME_UPDATE"));
                salida.setMimetype(rs.getString("DESC_MIMETYPE"));
                salida.setName(rs.getString("NOMBRE_ORIGINAL"));
                salida.setFinalname(rs.getString("NOMBRE_FINAL"));
                totalRegistro++;
                if (salida.getEstado() != STAT_IN_DISK) {
                     String hexData = ClobUtil.clobToString(rs.getClob("DESC_CONTENIDO"));
                     // BYSECURITY logger.debug("Prueba de rescate: " + hexData.substring(0,30));
                     salida.setBinaryContent(BinaryFiles.hexStringToByteArray(hexData));
                     return salida;
                }
            }
        } catch (IOException ex) {
            logger.error("Error durante el proceso del lectura de archivos de propiedades de BD");
            showFullError(ex);
            String mensajeTec = "IOException:" + ex.getMessage();
            response.setContentType("application/json");
            PrintWriter out1 = response.getWriter();
            out1.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"" + mensajeError + "\",\"mensajeTec\":\"IOException\"}");
        } catch (SQLException e) {
            showFullError(e);
            response.setContentType("application/json");
            PrintWriter out1 = response.getWriter();
            out1.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"" + mensajeError + "\",\"mensajeTec\":\"SQLException\"}");
        } finally {
            if (datasource != null) {
                try {
                    datasource.close();
                } catch (Exception e) {
                    showFullError(e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    showFullError(e);
                }
            }
        }
        return salida;
    }
    
    private InterchangeFile ConsultaArchivoDB(int id, HttpServletResponse response, PrintWriter out) {
        Connection conn;
        Statement stmt = null;
        InterchangeFile salida = null;
        ResultSet rs;
        int totalRegistro = 0;
        DataSource datasource = null;
        try {
            datasource = getDataSource();
            conn = datasource.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT ID_CACHE, TIME_CREATION, TIME_UPDATE"
                    + " DESC_CONTENIDO, DESC_MIMETYPE, NOMBRE_ORIGINAL, NOMBRE_FINAL"
                    + ",ID_ESTADO_CACHE"
                    + " FROM tb_proyecto_cache WHERE ID_CACHE = " + id);

            salida = new InterchangeFile();
            salida.setId(id);
            if (rs.next()) {
                salida.setEstado(rs.getInt("ID_ESTADO_CACHE"));
                salida.setTimeCreation(rs.getTimestamp("TIME_CREATION"));
                salida.setTimeUpdate(rs.getTimestamp("TIME_UPDATE"));
                salida.setMimetype(rs.getString("DESC_MIMETYPE"));
                salida.setName(rs.getString("NOMBRE_ORIGINAL"));
                salida.setFinalname(rs.getString("NOMBRE_FINAL"));
                totalRegistro++;
                if (salida.getEstado() != STAT_IN_DISK) {
                    String hexData = ClobUtil.clobToString(rs.getClob("DESC_CONTENIDO"));
                    salida.setContent(BinaryFiles.hex2Bin(hexData));
                    return salida;
                }
            }
        } catch (IOException ex) {
            logger.error("Error durante el proceso del lectura de archivos de propiedades de BD");
            showFullError(ex);
            response.setContentType("application/json");
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"" + mensajeError + "\",\"mensajeTec\":\"IOException\"}");

        } catch (SQLException e) {
            showFullError(e);
            response.setContentType("application/json");
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"" + mensajeError + "\",\"mensajeTec\":\"SQLException\"}");
        } finally {
            if (datasource != null) {
                try {
                    datasource.close();
                } catch (Exception e) {
                    showFullError(e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    showFullError(e);
                }
            }
        }
        return salida;
    }
    
    // Con metodo POST (NO utilizado)

    private void EscribirSalida(InterchangeFile salida, HttpServletResponse resp) throws IOException {
        String mimetype = salida.getMimetype();
        String filename = salida.getName();
        resp.setContentType(mimetype.replaceAll("\r|\n", ""));
        resp.setContentLength(salida.getBinaryContent().length);
        resp.setHeader("content-disposition", "inline; filename=\""+ filename.replaceAll("\r|\n", "") + "\"");

        OutputStream out = resp.getOutputStream();
        // Copy the contents of the file to the output stream

        byte buf[] = salida.getBinaryContent();
        int len = buf.length;
        out.write(buf, 0, len);
        out.close();
    }
    
    private void EscribirSalidaDesdeArchivo(InterchangeFile salida, HttpServletResponse resp) throws IOException, GesvitaException {
        
        Properties prop = LeerPropiedades("archivo");
        
        String diskDir = prop.getProperty("ruta");
        String mimetype = salida.getMimetype();
        String filename = salida.getName();
        String fileInDisk = diskDir + File.separator + salida.getFinalname();
        resp.setContentType(mimetype.replaceAll("\r|\n", ""));
        File file = new File(fileInDisk);
        resp.setContentLength((int) file.length());
        FileInputStream in = new FileInputStream(file);
        resp.setHeader("content-disposition", "inline; filename=\""+ filename.replaceAll("\r|\n", "") + "\"");

        OutputStream out = resp.getOutputStream();
        try {
            // Copy the contents of the file to the output stream
            byte[] buf = new byte[1024];
            int count;
            while ((count = in.read(buf)) >= 0) {
                    out.write(buf, 0, count);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            
        }
    }


    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Max-Age", "86400");
        response.setHeader("Allow", "GET, HEAD, POST, TRACE, OPTIONS");
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

    public static String slurp(final InputStream is) throws UnsupportedEncodingException, IOException {
        final int bufferSize = 8196;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is); //, "UTF-8");
        for (;;) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0) {
                break;
            }
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }

    private void showFullError(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- BEGIN STACK TRACE ---");
        sb.append("\n");
        for (StackTraceElement elem : e.getStackTrace()) {
            sb.append(elem.toString());
            sb.append("\n");
        }
        sb.append("--- END STACK TRACE ---");
        logger.error("" + e.getClass().toString() + ":" + e.getMessage());
        logger.error(sb.toString());
    }

    private Properties LeerPropiedades(String archivo) throws GesvitaException {
        Properties prop = new Properties();
        try {
            String archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/" + archivo + ".properties";
            prop.load(new FileInputStream(archivoPro));
        } catch (FileNotFoundException e) {
            logger.error("Error FileNotFoundException", e.fillInStackTrace());
            throw new GesvitaException("FileNotFoundException in LeerPropiedades()");
        } catch (IOException ex) {
            logger.error("Error IOException", ex.fillInStackTrace());
            throw new GesvitaException("IOException in LeerPropiedades()");
        }
        return prop;
    }
}
