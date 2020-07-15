/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.gesvita.ws.subirarchivoshp.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Felipe
 */
public class CallProcess {

    
    
    public static void callCopy(Logger logger, long id) {
        // CallProcess.callCopy(logger, CallProcess.getIdCacheCarpeta(logger,con,id_proyecto));
        if (id != 0L) {
            String archivoPro;
            Properties prop = new Properties();
            try {
                // archivoPro = System.getProperty("catalina.home") + "/webapps/propiedades/archivo.properties";
                archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/archivo.properties";
                prop.load(new FileInputStream(archivoPro));
            } catch (FileNotFoundException e) {
                logger.error("Error FileNotFoundException", e.fillInStackTrace());
            } catch (IOException e) {
                logger.error("Error durante el proceso del archivo: ", e.fillInStackTrace());
//                try {
//                    archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/archivos.properties";
//                    // archivoPro = System.getProperty("catalina.home") + "/webapps/propiedades/archivos.properties";
//                    prop.load(new FileInputStream(archivoPro));
//                } catch (FileNotFoundException e2) {
//                    logger.error("Error FileNotFoundException", e2.fillInStackTrace());
//                } catch (IOException e2) {
//                    logger.error("Error durante el proceso del archivo(s): ", e2.fillInStackTrace());
//                }
            }
            try {
                String app = prop.getProperty("appsharepoint.app");
                if (app == null) {
                    app = "/u01/home/app/gpisodcl/tomcat/webapps/shell/run.sh";
                }
                    
                if (app != null) {
                    String cmd[] = {"" + app,
                        "" + id}; // , " >/dev/null 2>" + prop.getProperty("appsharepoint.log");
                    // for (String p : cmd) {
                        // BYSECURITY logger.info("parameter : " + p);
                    // }
                    Process p = Runtime.getRuntime().exec(cmd);
                    // BYSECURITY logger.info("Called");
                }
            } catch (IOException ex) {
                logger.error("Error al lanzar proceso de copia a sharepoint con ID = " + id);
                logger.error(ex.fillInStackTrace());
            } catch (Exception ex) {
                logger.error("Error al lanzar proceso de copia a sharepoint con ID = " + id);
                logger.error(ex.fillInStackTrace());
            }

        }
    }
    
    public static long getIdCacheCarpeta(Logger logger,Connection con, long id_proyecto) {
        long id_cache = 0L;
        String consulta = "SELECT ID_DOCUMENTO_ENREPOSITORIO"
                        + " FROM TB_PROYECTO_DOCUMENTO"
                        + " WHERE ID_PROYECTO = ?"
                        + " AND TIPO_DOCUMENTO = 'CARPETA'";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(consulta);
            stmt.setLong(1, id_proyecto);
            rs = stmt.executeQuery();
            if (rs.next()) {
                id_cache = rs.getLong("ID_DOCUMENTO_ENREPOSITORIO");
            }
        } catch (SQLException ex) {
            logger.error("Error al consultar id_cache de la solicitud", ex.fillInStackTrace());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e.fillInStackTrace());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e.fillInStackTrace());
                }
            }
        }
        return id_cache;
    }
}
