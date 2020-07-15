package cl.gesvita.ws.obteneravancetargrupo;

import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.JsonObject;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class ObtenerAvancetarGrupo
 */
@WebServlet("/WSObtenerAvancetarGrupo/ObtenerAvancetarGrupo")
public class ObtenerAvancetarGrupo extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerAvancetarGrupo.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerAvancetarGrupo() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        DataSource datasource;
        Connection connection = null;
        JsonObject jsonObj;
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
        try {
            // Leer la entrada
            jsonObj = ObtenerLib.readInput(logger, request);
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error de parseo\"}");
            return;
        }
        // Realizar la consulta
        try {
            // Preparar la consulta
            connection = datasource.getConnection();
            String[] camposytipos = {
                    "ID_EMPRESA","I","ID_PROYECTO","I","NOMBRE","S","CODI_PROYECTO","S","FECHA_CREACION","S","ID_TIPO","I","OBSERVACION_ACTUAL","S","ID_PROYECTO_PADRE","I"
                    ,"NOMBRE_PROYECTO_PADRE","S","ID_PROYECTO_TEMPLATE","I","ID_AREA","I","NOMBRE_AREA","S","CODI_GRUPO_PROYECTO","S","NOMBRE_GRUPO_PROYECTO","S"
                    ,"SLA_PLAZOS","I","DOCUMENTOS_ADJUNTOS","S","AVANCE_REAL_PROYECTO","I","ID_AREA_CLIENTE","I","NOMBRE_AREA_CLIENTE","S"
                    ,"ID_SUCURSAL","I","NOMBRE_SUCURSAL","S","CODI_GRUPO_SUCURSAL","S","NOMBRE_GRUPO_SUCURSAL","S"
                    ,"ID_CENTRO_COSTO","I","ID_ESTADO_PROYECTO","I","NOMBRE_ESTADO_PROYECTO","S","ID_PRIORIDAD","I","NOMBRE_PRIORIDAD","S"
                    ,"ID_FORMULARIO_PROYECTO","I","URL_FORMULARIO_PROYECTO","S","ID_USUARIO_CREACION","I","NOMBRE_USUARIO_CREACION","S"
                    ,"ID_USUARIO_MODIFICACION","I","NOMBRE_USUARIO_MODIFICACION","S","FECHA_MODIFICACION_FMT","S","ID_REQUERIMIENTO","I"
                    ,"NOMBRE_REQUERIMIENTO","S","ID_TIPOSOLICITUD","I","NOMBRE_SOLICITUD","S","ID_USUARIO_JEFE_PROYECTO","I"
                    ,"NOMBRE_JEFEPROYECTO","S","ID_TAREA_ABUELO","I","NOMBRE_TAREA_ABUELO","S","ID_TIPO_TAREA_ABUELO","I"
                    ,"NOMBRE_TIPO_ABUELO","S","ID_ESTADO_ABUELO","I","AVANCE_REAL_ABUELO","I","ID_TAREA_TEMPLATE_ABUELO"
                    ,"I","FECHA_PLAN_INI_ABUELO","D","FECHA_REAL_INIPROY","S","FECHA_REAL_FINPROY","S","FECHA_PLAN_INIPROY","S"
                    ,"FECHA_PLAN_FINPROY","S","FECHA_PLAN_INIPROY_DATE","D","ID_TAREA_PADRE","I","NOMBRE_TAREA_PADRE","S","ID_TIPO_TAREA_PADRE","I","NOMBRE_TIPO_PADRE","S"
                    ,"ID_ESTADO_PADRE","I","AVANCE_REAL_PADRE","I","FECHA_PLAN_INI_PADRE","D","ID_TAREA_TEMPLATE_PADRE","I"
                    ,"NOMBRE_TAREA_TEMPLATE_PADRE","S","ID_PROYECTO_ENLACETEMPLATE_PADRE","I","ID_PROYECTO_ENLACE_PADRE","I"
                    ,"DATA_HITOS","S","CANTIDADTAREAPADRE","I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_AVANCETARGRUPO" , jsonObj);
            // Imprimir la salida
            out.print(json);
        } catch (SQLException e) {
            logger.error("Error durante la consulta a la base de datos. ",e.fillInStackTrace());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"SQLException\"}");
        } catch (GesvitaException ex) {
            logger.error("Error de entrada : " + ex.getMessage());
            String salidaNOK = "{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error en consulta a BD\"}";
            out.print(salidaNOK);
        } finally {
            try {
                if (connection!= null && !connection.isClosed()){
                    connection.close();
                }
            } catch (SQLException sqle) {
                logger.error(sqle.getMessage(), sqle.fillInStackTrace());
            }
            try {
                datasource.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e.fillInStackTrace());
            }
        }
    }
}
