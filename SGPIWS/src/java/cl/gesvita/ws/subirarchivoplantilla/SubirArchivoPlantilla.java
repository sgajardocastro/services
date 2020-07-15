package cl.gesvita.ws.subirarchivoplantilla;

import cl.gesvita.ws.archivoplantilla.bean.db.ResultOut;


import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import cl.gesvita.ws.subirarchivoshp.util.BinaryFiles;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Properties;

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

/**
 * Servlet implementation class SubirArchivo
 * "/WSSubirArchivoShp/SubirArchivoShp"
 */
@WebServlet("/WSSubirArchivoPlantilla/SubirArchivoPlantilla")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 100, // 100Mb (era 10MB)
        maxRequestSize = 1024 * 1024 * 1024 * 30)   // 50Gb (era 50MB)
public class SubirArchivoPlantilla { //  extends HttpServlet {

    private static final long serialVersionUID = 26535612783564752L;
    static Logger logger = Logger.getLogger(SubirArchivoPlantilla.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubirArchivoPlantilla() {
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



    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     * Servicio NO UTILIZADO!!!!!! BORRAR
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        Map params = request.getParameterMap();
        PrintWriter out = response.getWriter();
        DataSource datasource;
        JsonObject jsonObj = null;
        response.setHeader("access-control-allow-origin", "*");
        try {
            // Inicializar Log4J
            ObtenerLib.setLogParam(this.getClass());
            // Obtener Data Source
            datasource = ObtenerLib.getDataSource(logger);
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error en conexión a BD\"}");
            return;
        }
        String inputFileName;
        String mimetype;
        String newFileName = "";
        ResultOut salida = null;
        int numparts = 0;
        try{
            String archivoPro;
           // BYSECURITY logger.info("Nro partes : " + request.getParts().size());
            // Recorrer las partes ingresadas
            for (Part part : request.getParts()) {
                // BYSECURITY logger.debug("leyendo part");
                inputFileName = extractFileName(part);
                if (!inputFileName.equalsIgnoreCase("")) {
                    numparts++;
                    mimetype = part.getContentType();
                    int proj;
                    try{
                        proj = Integer.parseInt((String)params.get("project"));
                    } catch (NumberFormatException ex) {
                        out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Parametro no ingresado\",\"mensajeTec\":\"Falta parametro project\"}");
                        return;
                    }
//                    salida = storePart(part, inputFileName, proj, datasource);
                    if (salida.getCode() != -1) {
                       // BYSECURITY logger.info("Archivo guardado con exito en " + newFileName);
                    }
                    break;
                }
            }
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
        } catch (IOException e) {
            logger.error("Error durante el proceso del archivo: " + e.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"IOException\"}");
        } catch (ServletException e) {
            logger.error("Error durante el proceso web: " + e.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"ServletException\"}");
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

    private ResultOut storePart(Part part, String inputFileName, int proj, DataSource datasource) throws IOException, SQLException {

        ResultOut salida = new ResultOut(0, -1, "");
        String mimetype = part.getContentType();
        
        InputStream input = part.getInputStream();
        
        Connection connection = datasource.getConnection();
        
//        for (line : alllines) {
//            salida = storePartDBDetail(connection,proj, line);
//        }
        return salida;
    }

    private ResultOut storePartDBHead(Connection conn, JsonObject jsonObj) throws IOException, SQLException {
        CallableStatement stmt = null;
        ResultOut salida = new ResultOut(0, -1, "");

        try {
            stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.PROYECTO_SETUP(?,?,?,?,?,?,?,?,?,?,?,?,to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),?,?,?,?,?,?,?,?)}");
            Clob clob = conn.createClob();
            Clob clob2 = conn.createClob();
            stmt.setFloat(1, Float.parseFloat(readFieldString(jsonObj, "in_id_estado")));
            stmt.setString(2, readFieldString(jsonObj, "in_nombre"));
            stmt.setString(3, readFieldString(jsonObj, "in_id_usuario_jefe_proyecto"));
            stmt.setFloat(4, Float.parseFloat(readFieldString(jsonObj, "in_id_programa")));
            stmt.setFloat(5, Float.parseFloat(readFieldString(jsonObj, "in_id_empresa")));
            // BYSECURITY logger.info(("previo a in_objetivo");
            clob.setString(1, readFieldString(jsonObj, "in_objetivo"));
            stmt.setClob(6, clob);
            stmt.setFloat(7, Float.parseFloat(readFieldString(jsonObj, "in_id_tipo")));
            stmt.setFloat(8, Float.parseFloat(readFieldString(jsonObj, "in_id_magnitud")));
            stmt.setFloat(9, Float.parseFloat(readFieldString(jsonObj, "in_id_prioridad")));
            stmt.setFloat(10, Float.parseFloat(readFieldString(jsonObj, "in_id_sponsor_area")));
            stmt.setFloat(11, Float.parseFloat(readFieldString(jsonObj, "in_id_usuario_sponsor")));
            stmt.setFloat(12, Float.parseFloat(readFieldString(jsonObj, "in_id_ppto")));
            // BYSECURITY logger.info(("previo a fechas");
            stmt.setString(13, readFieldString(jsonObj, "in_fecha_plan_ini"));
            stmt.setString(14, readFieldString(jsonObj, "in_fecha_plan_fin"));
            stmt.setString(15, readFieldString(jsonObj, "in_fecha_real_ini"));
            stmt.setString(16, readFieldString(jsonObj, "in_fecha_real_fin"));
            // BYSECURITY logger.info(("previo a in_observacion");
            clob2.setString(1, readFieldString(jsonObj, "in_observacion"));
            stmt.setClob(17, clob2);
            stmt.setFloat(18, Float.parseFloat(readFieldString(jsonObj, "in_id_usuario_creacion")));
            stmt.setFloat(19, Float.parseFloat(readFieldString(jsonObj, "in_id_comportamiento")));
            stmt.setFloat(20, Float.parseFloat(readFieldString(jsonObj, "in_id_area")));
            stmt.setFloat(21, Float.parseFloat(readFieldString(jsonObj, "in_id_tipo_template")));
            stmt.registerOutParameter(22, Types.NUMERIC);
            stmt.registerOutParameter(23, Types.VARCHAR);
            stmt.registerOutParameter(24, Types.VARCHAR);
            // BYSECURITY logger.info(("previo a ejecutar");
            stmt.execute();
            salida.setId(stmt.getInt(22)); // out_idproyecto
            salida.setCode(stmt.getInt(23)); // out_codigo
            salida.setDescription(stmt.getString(24).toString()); // out_mensaje

        } catch (Exception e) {
            logger.error("storePartDB : Exception: ", e.fillInStackTrace());
            StringBuilder sb = new StringBuilder();
        } finally {
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
    
    
    private ResultOut deleteAllTasks(Connection conn, long id_proyecto) throws IOException, SQLException {
        CallableStatement stmt = null;
        ResultOut salida = new ResultOut(0, -1, "");
        try {
            stmt = conn.prepareCall("{call PKG_MONITOR_DELETE.PROYECTO_ELIMINAR_TAREAS(?,?,?)}");
            stmt.setFloat(1, id_proyecto); // "in_id_proyecto"
            stmt.registerOutParameter(2, Types.NUMERIC);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
            salida.setCode(stmt.getInt(2));
            salida.setDescription(stmt.getString(3));
            return salida;

        } catch (SQLException e) {
            logger.error("storePartDB : Exception: ", e.fillInStackTrace());
            StringBuilder sb = new StringBuilder();
        } finally {
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

    private String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

//    public static String slurpbin(final InputStream is) throws UnsupportedEncodingException, IOException {
//        final int bufferSize = 8196;
//        final byte[] buffer = new byte[bufferSize];
//        final StringBuilder out = new StringBuilder();
//        DataInputStream in = new DataInputStream(is);
//        for (;;) {
//            int rsz = in.read(buffer, 0, buffer.length);
//            if (rsz < 0) {
//                break;
//            }
//            out.append(BinaryFiles.bytesToHex(buffer, rsz));
//        }
//        return out.toString();
//    }

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

    private String readFieldString(JsonObject jsonObj, String field) throws GesvitaException {
        JsonElement value = jsonObj.get(field.toLowerCase());
        if (value instanceof JsonArray) {
            throw new GesvitaException("Field: " + field + " is not spected as JsonArray");
        } else if (value instanceof JsonObject) {
            throw new GesvitaException("Field: " + field + " is not spected as JsonObject");
        } else {
            String salida = value.toString();
            if (salida.startsWith("\"") && salida.endsWith("\"")) {
                salida = salida.substring(1, salida.length() - 1);
            } else if (salida.startsWith("'") && salida.endsWith("'")) {
                salida = salida.substring(1, salida.length() - 1);
            }
            return salida;
        }
    }
}
