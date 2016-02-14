package com.example;

import com.example.pebble.MyExtension;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
        urlPatterns = "/downloader",
        initParams = {
                @WebInitParam(name = "path", value = "/media/niko/ExternalHDD/books/java")
        }
)
public class Downloader extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(Downloader.class.getName());

    private PebbleEngine engine;
    private String path;

    @Override
    public void init() throws ServletException {
        engine = new PebbleEngine.Builder().extension(new MyExtension()).build();
        path = getInitParameter("path");
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("text/html");

        String file = httpServletRequest.getParameter("file");
        String filePath = path + File.separator + file;
        File downloadFile = new File(filePath);

        try {
            InputStream inputStream = new FileInputStream(downloadFile);

            ServletContext context = getServletContext();
            String mimeType = context.getMimeType(filePath);

            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            String message = String.format("Downloaded: Filename - %s, MIME type - %s", file, mimeType);
            LOGGER.info(message);

            httpServletResponse.setContentType(mimeType);
            httpServletResponse.setContentLength((int) downloadFile.length());
            httpServletResponse.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", MimeUtility.encodeText(downloadFile.getName())));

            OutputStream outputStream = httpServletResponse.getOutputStream();
            inputStreamToOutputStream(inputStream, outputStream);
        } catch (FileNotFoundException e) {
            PebbleTemplate compiledTemplate;

            try {
                compiledTemplate = engine.getTemplate("templates/filenotfound.html");
            } catch (PebbleException pe) {
                throw new ServletException(pe);
            }

            Map<String, Object> context = new HashMap<>();
            context.put("file", file);

            Writer writer = httpServletResponse.getWriter();
            try {
                compiledTemplate.evaluate(writer, context);
            } catch (PebbleException pe) {
                throw new ServletException(pe);
            }
        }
    }

    private void inputStreamToOutputStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024 * 4];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();
    }
}
