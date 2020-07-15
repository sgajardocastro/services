package cl.gesvita.ws.obtenertareadiaria;

import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import com.google.gson.JsonElement;
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
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class ObtenerTareasDetalle
 */
@WebServlet("/WSObtenerTareaDiaria/ObtenerTareaDiaria")
public class ObtenerTareaDiaria extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerTareaDiaria.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerTareaDiaria() {
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
                "ID_EMPRESA","I","ID_USUARIO_EJECUTOR","I","NOMBRE_AREA_RESPONSABLE","S",
                "ID_AREA_RESPONSABLE","I","DIA","+D","CANT_HITOS_DIA","+I"
            };
            String campos;
            String continuacion;
            Map<String, String> mapa= new HashMap<>();

            campos = " pplview.id_empresa, pplview.id_usuario_ejecutor"
                    + ",pplview.id_area_responsable, pplview.nombre_area_responsable"
                    + ",pivote_dias.dia, sum(1) cant_hitos_dia"
                    ;
            continuacion = "{{WHEREAND}}"
                         + " PPLVIEW.fecha_plan_ini <  pivote_dias.dia + 1"
                         + " and PPLVIEW.fecha_plan_fin >= pivote_dias.dia"
                         + " and PPLVIEW.id_tipo_tarea = 1"   // 1: Hito
                         + " group by PPLVIEW.id_empresa, PPLVIEW.id_usuario_ejecutor"
                         + ", PPLVIEW.id_area_responsable "
                         + ", PPLVIEW.NOMBRE_AREA_RESPONSABLE "
                         + ", pivote_dias.dia";
            
            if (!jsonObj.has("in_dia_inicial") || !jsonObj.has("in_dia_final")) {
                throw new GesvitaException("Los campos dia inicial y final son obligatorios");
            }

            ObtenerLib.addToken(mapa, 'D', jsonObj ,"IN_DIA_INICIAL");
            ObtenerLib.addToken(mapa, 'D', jsonObj ,"IN_DIA_FINAL");
            
            String extratabla = ",(select (TO_DATE('{{IN_DIA_INICIAL}}','dd-MM-yyyy') + level - 1) dia from dual"
                              + " connect by level <= (1 + TO_DATE('{{IN_DIA_FINAL}}','dd-MM-yyyy') - TO_DATE('{{IN_DIA_INICIAL}}','dd-MM-yyyy') ))"
                              + " pivote_dias";
            
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_TAREADETALLE", jsonObj, extratabla ,campos, continuacion , mapa);
            
            JsonElement json1 = ParseJSON(json);
            
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
    
    private JsonElement ParseJSON(String in) {
            JsonParser parser = new JsonParser();
            JsonElement resp = parser.parse(in);
            return resp;
    }
}
