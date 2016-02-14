package com.example;

import com.example.pebble.MyExtension;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@MultipartConfig
@WebServlet(
        urlPatterns = "/updownloader",
        initParams = {
                @WebInitParam(name = "path", value = "/media/niko/ExternalHDD/books/java")
        }
)
public class Uploader extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(Uploader.class.getName());

    private PebbleEngine engine;
    private String path;

    @Override
    public void init() throws ServletException {
        engine = new PebbleEngine.Builder().extension(new MyExtension()).build();
        path = getInitParameter("path");
    }

    public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        ServletContext servletContext = httpServletRequest.getSession().getServletContext();

        for (Part part : httpServletRequest.getParts()) {
            String fileName = extractFileName(part);

            if (fileName.isEmpty()) {
                continue;
            }

            File file = new File(path + File.separator + fileName);

            OutputStream outputStream = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[1024];

            InputStream inputStream = part.getInputStream();

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            String message = String.format("Uploaded: Filename - %s, Formname - %s, size - %d Bytes", fileName, part.getName(), part.getSize());
            LOGGER.info(message);
        }

        httpServletResponse.sendRedirect(httpServletResponse.encodeRedirectURL(servletContext.getContextPath() + "/updownloader"));
    }

    public void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("text/html");
        PrintWriter printWriter = httpServletResponse.getWriter();

        ServletContext servletContext = getServletContext();

        PebbleTemplate compiledTemplate;

        try {
            compiledTemplate = engine.getTemplate("templates/updownloader.html");
        } catch (PebbleException e) {
            throw new ServletException(e);
        }

        File data = new File(path);
        File[] files = data.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });

        if (files != null) {
            Arrays.sort(files);
        }

        Map<String, Object> context = new HashMap<>();
        context.put("contextPath", servletContext.getContextPath());
        context.put("files", files);

        Writer writer = new StringWriter();
        try {
            compiledTemplate.evaluate(writer, context);
        } catch (PebbleException e) {
            throw new ServletException(e);
        }

        printWriter.println(writer.toString());
    }

    public static String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] items = contentDisposition.split(";");
        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                return item.substring(item.indexOf("=") + 2, item.length() - 1);
            }
        }

        return "";
    }
}
