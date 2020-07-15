package cl.gesvita.ws.verarchivo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Properties;
import javax.servlet.ServletContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class VerArchivo
 */
@WebServlet("/WSVerArchivo/VerArchivo")
public class VerArchivo extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(VerArchivo.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public VerArchivo() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Properties prop = new Properties();
        try {
            ServletContext cntx = req.getServletContext();
            InputStream stream = this.getClass().getResourceAsStream("proyecto.properties");
            prop.load(stream);
            String savePath = prop.getProperty("ruta");
            // Get the absolute path of the image
            //String filename = cntx.getRealPath(savePath + File.separator + req.getParameter("archivo"));
            String shortname = req.getParameter("archivo").replaceAll("\r|\n", "");
            String filename = savePath + File.separator + shortname;
//            logger.info("archivo : " + filename);
            String mime = URLConnection.guessContentTypeFromName(shortname);
            // retrieve mimeType dynamically
            //String mime = cntx.getMimeType(filename);
            /*
            if (mime == null) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
            }
            */
//            logger.info("mime : " + mime);
            resp.setContentType(mime);
            File file = new File(filename);
            resp.setContentLength((int) file.length());
            FileInputStream in = new FileInputStream(file);
            resp.setHeader("content-disposition", "inline; filename=\""+ shortname + "\"");

            OutputStream out = resp.getOutputStream();
            // Copy the contents of the file to the output stream
            byte[] buf = new byte[1024];
            int count;
            while ((count = in.read(buf)) >= 0) {
                    out.write(buf, 0, count);
            }
            out.close();
            in.close();
        } catch (IOException e) {
                logger.error("Error durante el proceso del archivo: " + e.getMessage());
        } catch (Exception e) {
                logger.error("Ha ocurrido un error grave: " + e.getMessage());
        }
    }

}
