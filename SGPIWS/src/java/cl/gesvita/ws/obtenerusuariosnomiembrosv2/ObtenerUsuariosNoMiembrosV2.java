package cl.gesvita.ws.obtenerusuariosnomiembrosv2;

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
 * Servlet implementation class ObtenerUsuariosNoMiembrosV2
 */
@WebServlet("/WSObtenerUsuariosNoMiembrosV2/ObtenerUsuariosNoMiembrosV2")
public class ObtenerUsuariosNoMiembrosV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerUsuariosNoMiembrosV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerUsuariosNoMiembrosV2() {
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
"ID_USUARIO","I","CODI_USUARIO","S","NOMBRE_USUARIO","S","RUT_USUARIO","S","TELEFONO_USUARIO","S","CELULAR_USUARIO","S","CORREO_USUARIO","S","ESTADO_USUARIO","S","USUARIO","S","NOMBRE","S","APELLIDO_MATERNO","S","APELLIDO_PATERNO","S","RUT","S","TELEFONO","S","CELULAR","S","CORREO","S","ID_ROL","I","NOMBRE_ROL","S","CODI_GRUPO_BASE_ROL","S","ID_PERFIL","I","CODI_GRUPO_BASE_PERFIL","S","NOMBRE_PERFIL","S","ESTADO","S","ID_EMPRESA","I","NOMBRE_EMPRESA","S","ID_AREA","I","NOMBRE_AREA","S","PASSWORD_USUARIO","S","PASSWORD","S","OBSERVACION_USUARIO","S"
            };
            Boolean iscodi_grupo_filtro , isid_area_filtro, isid_grupo_filtro;
            String campos = "*";
            String continuacion;
            String extratabla = "";
            Map<String, String> mapa= new HashMap<>();
            
            iscodi_grupo_filtro = jsonObj.has("codi_grupo_filtro");
            isid_area_filtro = jsonObj.has("id_area_filtro");
            isid_grupo_filtro = jsonObj.has("id_grupo_filtro");
            
            if (iscodi_grupo_filtro && isid_area_filtro ){
                continuacion = "{{WHEREAND}} "
                             + "("
                             + " (NOT EXISTS"
                             + "(SELECT 1 FROM VW_SEGURIDAD_GRUPOUSUARIOS USERGRUPO"
                             + " WHERE"
                             + " PPLVIEW.ID_USUARIO = USERGRUPO.ID_USUARIO"
                             + " AND USERGRUPO.CODI_GRUPO = '{{CODI_GRUPO_FILTRO}}')"
                             + " AND PPLVIEW.ID_AREA = {{ID_AREA_FILTRO}})"
                             + " OR EXISTS("
                             + " SELECT 1 FROM VW_SEGURIDAD_USERSGRUPO UGRUP,"
                             + " VW_SEGURIDAD_USUARIO USUARIOGRDET, VW_SEGURIDAD_GRUPO GRUPO,"
                             + " TB_PROYECTO_AREA AREA"
                             + " WHERE"
                             + " AREA.ID_AREA_PADRE = {{ID_AREA_FILTRO}}"
                             + " AND AREA.ID_AREA = USUARIOGRDET.ID_AREA"
                             + " AND GRUPO.CODI_GRUPO = UGRUP.CODI_GRUPO"
                             + " AND UGRUP.ID_USUARIO  = USUARIOGRDET.ID_USUARIO"
                             + " AND UGRUP.ID_USUARIO  = PPLVIEW.ID_USUARIO)"
                             + ")";
                ObtenerLib.addToken(mapa, 'V', jsonObj ,"CODI_GRUPO_FILTRO");
                ObtenerLib.addToken(mapa, 'I', jsonObj ,"ID_AREA_FILTRO");
            } else if (iscodi_grupo_filtro){
                continuacion = "{{WHEREAND}} "
                             + " NOT EXISTS"
                             + "(SELECT 1 FROM VW_SEGURIDAD_GRUPOUSUARIOS USERGRUPO"
                             + " WHERE"
                             + " PPLVIEW.ID_USUARIO = USERGRUPO.ID_USUARIO"
                             + " AND USERGRUPO.CODI_GRUPO = '{{CODI_GRUPO_FILTRO}}'"
                             + " )";
                ObtenerLib.addToken(mapa, 'V', jsonObj ,"CODI_GRUPO_FILTRO");
            } else  if (isid_grupo_filtro){
                continuacion = "{{WHEREAND}} "
                             + " NOT EXISTS"
                             + " (SELECT 1 FROM VW_SEGURIDAD_GRUPOUSUARIOS USERGRUPO, VW_SEGURIDAD_GRUPO GRUPO"
                             + " WHERE"
                             + " PPLVIEW.ID_USUARIO =  USERGRUPO.ID_USUARIO"
                             + " AND USERGRUPO.CODI_GRUPO = GRUPO.CODI_GRUPO"
                             + " AND GRUPO.ID_GRUPO = {{ID_GRUPO_FILTRO}}"
                             + " )";
                ObtenerLib.addToken(mapa, 'I', jsonObj ,"ID_GRUPO_FILTRO");
            } else {
                continuacion = "";
                mapa = null;
            }
            // Llamada a la BD
            String json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, connection,
                    camposytipos, "VW_SEGURIDAD_USUARIO" , jsonObj,extratabla,campos,continuacion, mapa);
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
