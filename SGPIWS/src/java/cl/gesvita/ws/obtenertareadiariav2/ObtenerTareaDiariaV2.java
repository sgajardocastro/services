package cl.gesvita.ws.obtenertareadiariav2;

import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import com.google.gson.JsonElement;
// import com.google.gson.JsonElement;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
// import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class ObtenerTareasDetalle
 */
@WebServlet("/WSObtenerTareaDiariaV2/ObtenerTareaDiariaV2")
public class ObtenerTareaDiariaV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerTareaDiariaV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerTareaDiariaV2() {
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
            if (!jsonObj.has("in_dia_inicial") || !jsonObj.has("in_dia_final")) {
                throw new GesvitaException("Los campos dia inicial y final son obligatorios");
            }

            connection = datasource.getConnection();
            String[] camposytipos = {
                "ID_EMPRESA","I","ID_USUARIO_EJECUTOR","I","NOMBRE_USUARIO_EJECUTOR","S",
                "ID_AREA_RESPONSABLE","I","NOMBRE_AREA_RESPONSABLE","S",
//                "FECHAS","+C",
                "OCUPACION","+C"
            };
            String campos;
            String continuacion;
            Map<String, String> mapa= new HashMap<>();
            ObtenerLib.addToken(mapa, 'D', jsonObj ,"IN_DIA_INICIAL");
            ObtenerLib.addToken(mapa, 'D', jsonObj ,"IN_DIA_FINAL");
            campos = " pplview.id_empresa, pplview.id_usuario_ejecutor, pplview.nombre_usuario_ejecutor"
                    + ",pplview.id_area_responsable, pplview.nombre_area_responsable"
//                    + ",JSON_ARRAYAGG("
//                    + " to_char(pivote_dias.dia,'dd/MM/yyyy')"
//                    + " order by pivote_dias.dia"
//                    + " returning clob"
//                    + " ) fechas"
                    + ",JSON_ARRAYAGG("
                    + "(SELECT distinct count(1)"
                    + "  FROM VW_PROYECTO_TAREADETALLE P2"
                    + "  WHERE P2.id_empresa = PPLVIEW.id_empresa"
                    + "  AND P2.id_usuario_ejecutor = PPLVIEW.id_usuario_ejecutor"
                    + "  AND P2.id_area_responsable = PPLVIEW.id_area_responsable"
                    + "  AND P2.fecha_plan_ini <  pivote_dias.dia + 1"
                    // + "  AND P2.fecha_plan_ini <  pivote_dias.dia + {{STEP}}"
                    + "  and P2.fecha_plan_fin >= pivote_dias.dia"
                    + "  and P2.id_tipo_tarea = 1 )"
                    + " order by pivote_dias.dia"
                    + " returning clob"
                    + " ) OCUPACION";
            continuacion = "{{WHEREAND}}"
                         + " PPLVIEW.fecha_plan_ini <= TO_DATE('{{IN_DIA_FINAL}}','dd-MM-yyyy')"
                         + " AND PPLVIEW.fecha_plan_fin >= to_date('{{IN_DIA_INICIAL}}','dd/mm/yyyy')"
                         + " and PPLVIEW.id_tipo_tarea = 1"   // 1: Hito
                         + " AND NOT EXISTS(SELECT 1 FROM VW_PROYECTO_TAREADETALLE CHEKVIEW WHERE"
                            + " CHEKVIEW.ID_EMPRESA = PPLVIEW.ID_EMPRESA"
                            + " AND CHEKVIEW.ID_USUARIO_EJECUTOR = PPLVIEW.ID_USUARIO_EJECUTOR"
                            + " AND CHEKVIEW.id_area_responsable = PPLVIEW.id_area_responsable"
                            + " and CHEKVIEW.id_tipo_tarea = PPLVIEW.id_tipo_tarea"
                            + " AND CHEKVIEW.ID_TAREA > PPLVIEW.ID_TAREA"
                            + " AND CHEKVIEW.fecha_plan_ini <= to_date('{{IN_DIA_FINAL}}','dd/mm/yyyy')"
                            + " AND CHEKVIEW.fecha_plan_fin >= to_date('{{IN_DIA_INICIAL}}','dd/mm/yyyy')"
                            + " ) "
                         + " group by PPLVIEW.id_empresa, PPLVIEW.id_usuario_ejecutor "
                         + ", PPLVIEW.nombre_usuario_ejecutor, PPLVIEW.id_area_responsable "
                         + ", PPLVIEW.nombre_area_responsable";

            
            String extratabla = ",(select (TO_DATE('{{IN_DIA_INICIAL}}','dd-MM-yyyy') + level - 1) dia from dual"
                              + " connect by level <= (1 + TO_DATE('{{IN_DIA_FINAL}}','dd-MM-yyyy') - TO_DATE('{{IN_DIA_INICIAL}}','dd-MM-yyyy') ))"
                              + " pivote_dias";
//            CON VARIABLE {{STEP}}
//            String extratabla = ",(select (TO_DATE('{{IN_DIA_INICIAL}}','dd-MM-yyyy') + level - 1) dia from dual"
//                              + " connect by level * {{STEP}} <= (1 + TO_DATE('{{IN_DIA_FINAL}}','dd-MM-yyyy') - TO_DATE('{{IN_DIA_INICIAL}}','dd-MM-yyyy') ))"
//                              + " pivote_dias";
            
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_TAREADETALLE", jsonObj, extratabla ,campos, continuacion , mapa);
            String salidaJson = quitarComillasDoblesAOcupacion(json);
            out.print(salidaJson.replaceFirst("^\\{",
                    "{\n\"resultadoEjecucion\":\"OK\",\n\"FECHAS\":" + listaFechas(mapa) + ","));
        } catch (SQLException e) {
            logger.error("Error durante la consulta a la base de datos. ",e.fillInStackTrace());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"SQLException\"}");
        } catch (GesvitaException ex) {
            logger.error("Error de entrada : " + ex.getMessage());
            String salidaNOK = "{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error en consulta a BD\"}";
            out.print(salidaNOK);
        } catch (ParseException ex) {
            logger.error("Error de entrada : " + ex.getMessage());
            String salidaNOK = "{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Formato de fecha invalido\",\"mensajeTec\":\"Error formato de fecha ingresada\"}";
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
    
    private String quitarComillasDoblesAOcupacion(String in) {
        return in.replaceAll("(\"OCUPACION\"[: ]+)\"(\\[[,\\d]*\\])\"", "\"OCUPACION\":$2");
    }
    

    private String listaFechas(Map<String,String> map) throws ParseException{
        return listaFechas(map,1);
    }

    private String listaFechas(Map<String,String> map, int step) throws ParseException{
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        
        Date dia1 = df.parse(map.get("IN_DIA_INICIAL"));
        Date diaf= df.parse(map.get("IN_DIA_FINAL"));
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Calendar cal = Calendar.getInstance();
        cal.setTime(dia1);
        if (cal.getTime().compareTo(diaf) < 1){ // menor o igual
            while (true) {
                sb.append("\"").append(df.format(cal.getTime())).append("\"");
                cal.add(Calendar.DATE, step);
                if (cal.getTime().compareTo(diaf) < 1)
                    sb.append(",");
                else
                    break;
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    private JsonElement ParseJSON(String in) {
            JsonParser parser = new JsonParser();
            JsonElement resp = parser.parse(in);
            return resp;
    }
}
