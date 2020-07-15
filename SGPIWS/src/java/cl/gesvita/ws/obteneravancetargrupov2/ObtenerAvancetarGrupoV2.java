package cl.gesvita.ws.obteneravancetargrupov2;

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
 * Servlet implementation class ObtenerAvancetarGrupo
 */
@WebServlet("/WSObtenerAvancetarGrupoV2/ObtenerAvancetarGrupoV2")
public class ObtenerAvancetarGrupoV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerAvancetarGrupoV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerAvancetarGrupoV2() {
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
            ObtenerLib.setLogParam(ObtenerAvancetarGrupoV2.class);
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
            // BYSECURITY logger.info("DATA IN : " + jsonObj.toString());
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error de parseo\"}");
            return;
        }
        // Realizar la consulta
        try {
            // Preparar la consulta
            connection = datasource.getConnection();
            // Declaración de variables (inicio - cambio con respecto a V1)
            String campos = "*";
            String continuacion;
            String extratabla = "";
            Map<String, String> mapa= new HashMap<>();
            
          continuacion = "{{WHEREAND}} ("
                             // Sub select 1
                             + " exists("
                             + " select 1 from VW_GRUPO_USUARIO ugrup"
                             + " where PPLVIEW.CODI_GRUPO_PROYECTO = ugrup.CODI_GRUPO and ugrup.id_usuario = {{ID_USUARIO}})"
                  
//                             + " exists("
//                             + " select 1 from VW_SEGURIDAD_USERSGRUPO ugrup"
//                             + " where PPLVIEW.CODI_GRUPO_PROYECTO = ugrup.CODI_GRUPO and ugrup.id_usuario = {{ID_USUARIO}})"
                             // Sub select 2
                             + " or exists (select 1 from tb_seguridad_usuario usuario"
                             + " where usuario.id_usuario = {{ID_USUARIO}} and usuario.ID_ROL = 10)"
                             // Sub select 2.1
                             + " or exists (select 1 from tb_seguridad_usuario usuario"
                             + " where usuario.id_usuario = {{ID_USUARIO}} and usuario.ID_ROL in (27,28) and PPLVIEW.id_area = 111)"
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
                             + " VW_GRUPO_USUARIO ugrup, tb_seguridad_usuario usuarioGRdet,"
                             + " vw_seguridad_grupo grupo"
                             + " where usuario.id_usuario = {{ID_USUARIO}}"
                             + " and usuario.ID_ROL in (11,2,10)"
                             + " and grupo.NOMBRE_TIPO = 'MESADETRABAJO'"
                             + " and usuario.id_area  = usuarioGRdet.id_area"
                             + " and grupo.codi_grupo = ugrup.codi_grupo"
                             + " and ugrup.id_usuario = usuarioGRdet.id_usuario)"
//                             + " or exists (select 1 from tb_seguridad_usuario usuario,"
//                             + " VW_SEGURIDAD_USERSGRUPO ugrup, tb_seguridad_usuario usuarioGRdet,"
//                             + " vw_seguridad_grupo grupo"
//                             + " where usuario.id_usuario = {{ID_USUARIO}}"
//                             + " and usuario.ID_ROL in (11,2,10)"
//                             + " and grupo.NOMBRE_TIPO = 'MESADETRABAJO'"
//                             + " and usuario.id_area  = usuarioGRdet.id_area"
//                             + " and grupo.codi_grupo = ugrup.codi_grupo"
//                             + " and ugrup.id_usuario = usuarioGRdet.id_usuario)"
                             // NO VA!!!! + " and PPLVIEW.id_area = grupo.id_area_grupo)"
                             // Sub select 6: Check de Usuario "Jefes" con participantes de sus SubÁrea en Mesas
//                             +" or exists "
//                             +" ( "
//                             +"     select 1"
//                             +"     from tb_seguridad_usuario usuario, tb_proyecto_area area"
//                             +"     where usuario.id_usuario =  {{ID_USUARIO}}"
//                             +"     and usuario.ID_ROL in (11,2,10)  "
//                             +"     and  usuario.id_area = area.id_area_padre"
//                             +"     and exists"
//                             +"     ("
//                             +"     select 1"
//                             +"     from VW_SEGURIDAD_USERSGRUPO ugrup, tb_seguridad_usuario usuarioGRdet, vw_seguridad_grupo grupo "
//                             +"     where "
//                             +"     grupo.NOMBRE_TIPO = 'MESADETRABAJO'"
//                             +"     and area.id_area = usuarioGRdet.id_area "
//                             +"     and grupo.codi_grupo = ugrup.codi_grupo"
//                             +"     and ugrup.id_usuario  = usuarioGRdet.id_usuario"
//                             +"     )"
//                             +"     )"
//                             + " )";
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
                             +"     from VW_GRUPO_USUARIO ugrup, tb_seguridad_usuario usuarioGRdet, vw_seguridad_grupo grupo "
                             +"     where "
                             +"     grupo.NOMBRE_TIPO = 'MESADETRABAJO'"
                             +"     and area.id_area = usuarioGRdet.id_area "
                             +"     and grupo.codi_grupo = ugrup.codi_grupo"
                             +"     and ugrup.id_usuario  = usuarioGRdet.id_usuario"
                             +"     )"
                             +"     )"
                             + " )";
//            
//            
//            continuacion = "{{WHEREAND}} ("
//                             // Sub select 1
//                             + " exists("
//                             + " select 1 from VW_SEGURIDAD_USERSGRUPO ugrup"
//                             + " where PPLVIEW.CODI_GRUPO_PROYECTO = ugrup.CODI_GRUPO and ugrup.id_usuario = {{ID_USUARIO}})"
//                             // Sub select 2
//                             + " or exists (select 1 from vw_seguridad_usuario usuario"
//                             + " where usuario.id_usuario = {{ID_USUARIO}} and usuario.ID_ROL = 10)"
//                             // Sub select 3
//                             + " or exists (select 1 from vw_seguridad_usuario usuario, vw_seguridad_grupo grupo, tb_grupo_tipo grtipo"
//                             + " where usuario.id_usuario = {{ID_USUARIO}}"
//                             + " and usuario.ID_ROL in (11,2) and PPLVIEW.CODI_GRUPO_PROYECTO = grupo.CODI_GRUPO"
//                             + " and grupo.id_tipo_grupo = grtipo.id_tipo_grupo"
//                             + " and grtipo.NOMBRE_TIPO = 'MESADETRABAJO'"
//                             + " and grupo.id_area_grupo = usuario.ID_AREA )"
//                             // Sub select 4
//                             + " or exists (select 1 from dual"
//                             + " where PPLVIEW.id_requerimiento = 2029 and {{ID_AREA_USUARIO_REQUEST}} = 3 )"
//                             // Sub select 5
//                             + " or exists (select 1 from vw_seguridad_usuario usuario,"
//                             + " VW_SEGURIDAD_USERSGRUPO ugrup, vw_seguridad_usuario usuarioGRdet,"
//                             + " vw_seguridad_grupo grupo, tb_grupo_tipo grtipo"
//                             + " where usuario.id_usuario = {{ID_USUARIO}}"
//                             + " and usuario.ID_ROL in (11,2,10)"
//                             + " and grupo.id_tipo_grupo = grtipo.id_tipo_grupo"
//                             + " and grtipo.NOMBRE_TIPO = 'MESADETRABAJO'"
//                             + " and usuario.id_area  = usuarioGRdet.id_area"
//                             + " and grupo.codi_grupo = ugrup.codi_grupo"
//                             + " and ugrup.id_usuario = usuarioGRdet.id_usuario"
//                             + " and PPLVIEW.id_area = grupo.id_area_grupo)"
//                             + " )";
            ObtenerLib.addToken(mapa, 'I', jsonObj ,"ID_USUARIO");
            ObtenerLib.addToken(mapa, 'I', jsonObj ,"ID_AREA_USUARIO_REQUEST");
            // Declaración de variables (fin - cambio con respecto a V1)
            String[] camposytipos = {
                    "ID_EMPRESA","I","ID_PROYECTO","I","NOMBRE","S","CODI_PROYECTO","S","FECHA_CREACION","D","ID_TIPO","I","OBSERVACION_ACTUAL","S"
                    ,"ID_PROYECTO_PADRE","I", 
                    // "NOMBRE_PROYECTO_PADRE","S",
                    "ID_CENTRO_COSTO","I"
                    ,"ID_PROYECTO_TEMPLATE","I","ID_AREA","I","NOMBRE_AREA","S","CODI_GRUPO_PROYECTO","S","NOMBRE_GRUPO_PROYECTO","S"
                    ,"SLA_PLAZOS","I","DOCUMENTOS_ADJUNTOS","S","AVANCE_REAL_PROYECTO","I","ID_AREA_CLIENTE","I","NOMBRE_AREA_CLIENTE"
                    ,"S","ID_SUCURSAL","I","NOMBRE_SUCURSAL","S","CODI_GRUPO_SUCURSAL","S","NOMBRE_GRUPO_SUCURSAL","S"
                    ,"ID_ESTADO_PROYECTO","I","NOMBRE_ESTADO_PROYECTO","S","ID_PRIORIDAD","I","NOMBRE_PRIORIDAD","S"
                    ,"ID_FORMULARIO_PROYECTO","I","URL_FORMULARIO_PROYECTO","S","ID_USUARIO_CREACION","I","NOMBRE_USUARIO_CREACION","S"
                    ,"ID_USUARIO_MODIFICACION","I","NOMBRE_USUARIO_MODIFICACION","S","FECHA_MODIFICACION_FMT","S","ID_REQUERIMIENTO","I"
                    ,"NOMBRE_REQUERIMIENTO","S","ID_TIPOSOLICITUD","I","NOMBRE_SOLICITUD","S","ID_USUARIO_JEFE_PROYECTO","I"
                    ,"NOMBRE_JEFEPROYECTO","S","ID_TAREA_ABUELO","I","NOMBRE_TAREA_ABUELO","S","ID_TIPO_TAREA_ABUELO","I"
                    ,"NOMBRE_TIPO_ABUELO","S","ID_ESTADO_ABUELO","I","AVANCE_REAL_ABUELO","I","ID_TAREA_TEMPLATE_ABUELO","I"
                    ,"FECHA_PLAN_INI_ABUELO","D","FECHA_REAL_INIPROY","S","FECHA_REAL_FINPROY","S","FECHA_PLAN_INIPROY","S"
                    ,"FECHA_PLAN_FINPROY","S","FECHA_PLAN_INIPROY_DATE","D","ID_TAREA_PADRE","I","NOMBRE_TAREA_PADRE","S","ID_TIPO_TAREA_PADRE","I","NOMBRE_TIPO_PADRE","S"
                    ,"ID_ESTADO_PADRE","I","AVANCE_REAL_PADRE","I","FECHA_PLAN_INI_PADRE","D","ID_TAREA_TEMPLATE_PADRE","I"
                    ,"NOMBRE_TAREA_TEMPLATE_PADRE","S","ID_PROYECTO_ENLACETEMPLATE_PADRE","I","ID_PROYECTO_ENLACE_PADRE","I"
                    ,"DATA_HITOS","S","CANTIDADTAREAPADRE","I"
            };
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_PROYECTO_AVANCETARGRUPO" , jsonObj,extratabla,campos,continuacion, mapa);
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
