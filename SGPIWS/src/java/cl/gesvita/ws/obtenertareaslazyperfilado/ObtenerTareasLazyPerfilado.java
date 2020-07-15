package cl.gesvita.ws.obtenertareaslazyperfilado;

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
import java.util.HashMap;
import java.util.Map;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class ObtenerTareasLazyPerfilado
 */
@WebServlet("/WSObtenerTareasLazyPerfilado/ObtenerTareasLazyPerfilado")
public class ObtenerTareasLazyPerfilado extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerTareasLazyPerfilado.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerTareasLazyPerfilado() {
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
            String campos = " * ";
            String continuacion;
            String extratabla= "";
            Map<String, String> mapa= new HashMap<>();
            
          continuacion = "{{WHEREAND}} ("
                             // Sub select 1
                             + " exists("
                             + " select 1 from VW_SEGURIDAD_USERSGRUPO ugrup"
                             + " where PPLVIEW.CODI_GRUPO_PROYECTO = ugrup.CODI_GRUPO and ugrup.id_usuario = {{ID_USUARIO}})"
                             // Sub select 2
                             + " or exists (select 1 from tb_seguridad_usuario usuario"
                             + " where usuario.id_usuario = {{ID_USUARIO}} and usuario.ID_ROL = 10)"
                             // Sub select 3
                             + " or exists (select 1 from tb_seguridad_usuario usuario, vw_seguridad_grupo grupo"
                             + " where usuario.id_usuario = {{ID_USUARIO}}"
                             + " and usuario.ID_ROL in (11,2) and PPLVIEW.CODI_GRUPO_PROYECTO = grupo.CODI_GRUPO"
                             + " and grupo.NOMBRE_TIPO = 'MESADETRABAJO'"
                             + " and grupo.id_area_grupo = usuario.ID_AREA )"
                             // Sub select 4
                             + " or exists (select 1 from dual"
                             + " where PPLVIEW.id_requerimiento = 2029 and {{ID_AREA_USUARIO_REQUEST}} = 3 )"
                             // Sub select 5
                             + " or exists (select 1 from tb_seguridad_usuario usuario,"
                             + " VW_SEGURIDAD_USERSGRUPO ugrup, tb_seguridad_usuario usuarioGRdet,"
                             + " vw_seguridad_grupo grupo"
                             + " where usuario.id_usuario = {{ID_USUARIO}}"
                             + " and usuario.ID_ROL in (11,2,10)"
                             + " and grupo.NOMBRE_TIPO = 'MESADETRABAJO'"
                             + " and usuario.id_area  = usuarioGRdet.id_area"
                             + " and grupo.codi_grupo = ugrup.codi_grupo"
                             + " and ugrup.id_usuario = usuarioGRdet.id_usuario)"
                             // NO VA!!! + " and PPLVIEW.id_area = grupo.id_area_grupo)"
                             // Sub select 5: Check de Usuario "Jefes" con participantes de sus SubÁrea en Mesas
                             +" or exists "
                             +" ( "
                             +"     select 1"
                             +"     from tb_seguridad_usuario usuario, tb_proyecto_area area"
                             +"     where usuario.id_usuario =  {{ID_USUARIO}}"
                             +"     and usuario.ID_ROL in (11,2,10)  "
                             +"     and  usuario.id_area = area.id_area_padre"
                             +"     and exists"
                             +"     ("
                             +"     select 1"
                             +"     from VW_SEGURIDAD_USERSGRUPO ugrup, tb_seguridad_usuario usuarioGRdet, vw_seguridad_grupo grupo "
                             +"     where "
                             +"     grupo.NOMBRE_TIPO = 'MESADETRABAJO'"
                             +"     and area.id_area = usuarioGRdet.id_area "
                             +"     and grupo.codi_grupo = ugrup.codi_grupo"
                             +"     and ugrup.id_usuario  = usuarioGRdet.id_usuario"
                             +"     )"
                             +"     )"
                             + " )";
            

            ObtenerLib.addToken(mapa, 'I', jsonObj ,"ID_USUARIO");
            ObtenerLib.addToken(mapa, 'I', jsonObj ,"ID_AREA_USUARIO_REQUEST");
            String[] camposytipos = {
                     "ID_PROYECTO","I","ID_EMPRESA","I","NOMBRE_PROYECTO","S","CODI_PROYECTO","S","ID_ESTADO_SOLICITUD","I",
                "ID_PROYECTO_TEMPLATE","I","NOMBRE_JEFEPROYECTO","S","ID_USUARIO_JEFE_PROYECTO","I",
                "NOMBRE_REQUERIMIENTO","S","ID_TIPO_PROYECTO","I","FECHA_PLAN_INIPROY_DATE","D",
                "FECHA_PLAN_FINPROY_DATE","D","FECHA_REAL_INIPROY_DATE","D","FECHA_REAL_FINPROY_DATE","D",
                "AVANCE_REAL_PROYECTO","F","AVANCE_PLAN_PORTIEMPO","I","KPI_PROYECTO_AVANCE_STATUS","I",
                "NOMBRE_TAREA","S","CODI_TAREA","S","ID_TIPO_TAREA","I","ID_ESTADO","I","NOMBRE_ESTADO","S",
                "DATA_EXTENDIDA","S","ID_USUARIO_EJECUTOR","I","NOMBRE_AREA_JEFEPROYECTO","S"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_TAREALAZY" , jsonObj,extratabla,campos,continuacion, mapa);
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
