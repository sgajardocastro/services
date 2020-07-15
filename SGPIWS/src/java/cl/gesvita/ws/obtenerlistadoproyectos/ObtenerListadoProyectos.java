package cl.gesvita.ws.obtenerlistadoproyectos;

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
 * Servlet implementation class ObtenerListadoProyectos
 */
@WebServlet("/WSObtenerListadoProyectos/ObtenerListadoProyectos")
public class ObtenerListadoProyectos extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerListadoProyectos.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerListadoProyectos() {
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
"ID_PROYECTO","I","NOMBRE","S","OBJETIVO_PROYECTO","S","FECHA_PLAN_INI","S","FECHA_PLAN_FIN","S","FECHA_ORDER","D","FECHA_REAL_INI","S","FECHA_REAL_FIN","S","NOMBRE_USUARIO","S","NOMBRE_JEFEPROYECTO","S","NOMBRE_SPONSOR","S","ID_TIPO","I","DESCRIPCION_TIPO","S","NOMBRE_TIPO","S","ID_MAGNITUD","I","NOMBRE_MAGNITUD","S","ID_PRIORIDAD","I","NOMBRE_PRIORIDAD","S","ID_ESTADO","I","NOMBRE_ESTADO","S","NOMBRE_EMPRESA","S","OBSERVACION_PROYECTO","S","ID_FORMULARIO_PROYECTO","I","URL_FORMULARIO_PROYECTO","S","SEVERIDAD","I","COMPRA_Y_LICITACION","S","EJECUCION","S","EVALUACION_PROVEEDORES","S","DIAS_CUTOVER","S","DIAS_CUTOVER_NUMERO","I","ID_USUARIO_JEFE_PROYECTO","I","CODI_GRUPO_PROYECTO","S","NOMBRE_GRUPO_PROYECTO","S","NOMBRE_PROGRAMA","S","ID_EMPRESA","I","ID_PAIS","I","FECHA_REAL_INI_DATE","D","FECHA_REAL_FIN_DATE","D","FECHA_PLAN_INI_DATE","D","FECHA_PLAN_FIN_DATE","D","AVANCE_PLAN_PROYECTO","F","AVANCE_REAL_PROYECTO","F","SLA_PLAZOS","I","AVANCE_PLAN_PORTIEMPO","I","KPI_PROYECTO_AVANCE_STATUS","I","DOCUMENTOS_ADJUNTOS","S","ID_USUARIO_MODIFICACION","I","NOMBRE_USUARIO_MODIFICACION","S","FECHA_MODIFICACION","D","FECHA_MODIFICACION_FMT","S","ID_AREA","I","NOMBRE_AREA","S","ID_EMPRESA_CLIENTE","I","NOMBRE_EMPRESA_CLIENTE","S","ID_AREA_CLIENTE","I","NOMBRE_AREA_CLIENTE","S","ID_USUARIO_CLIENTE","I","NOMBRE_USUARIO_CLIENTE","S","CODI_PROYECTO","S","ID_PROYECTO_PADRE","I","NOMBRE_PROYECTO_PADRE","S","ID_REQUERIMIENTO","I","NOMBRE_REQUERIMIENTO","S","ID_TIPOSOLICITUD","I","NOMBRE_SOLICITUD","S","ID_SUCURSAL","I","NOMBRE_SUCURSAL","S","CODI_GRUPO_SUCURSAL","S","NOMBRE_GRUPO_SUCURSAL","S","CENTRO_COSTO_SUCURSAL","S","TIPO_SUCURSAL","S","ID_CENTRO_COSTO","I","ID_PPTO","I","NOMBRE_PRESUPUESTO","S","TIPO_PROYECTO_NEGOCIO","S","DATA_EXTENDIDA","S","ID_CONTRATO","I","NOMBRE_CONTRATO","S","ID_EMPRESA_CONTRATO","I","ID_USUARIO_CREACION","I","NOMBRE_USUARIO_CREACION","S","FECHA_CREACION","S","ID_PROYECTO_TEMPLATE","I","OBSERVACION_ACTUAL","S"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_SETUP" , jsonObj);
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
