package com.example;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(urlPatterns = "/getcontent",
        initParams = {
                @WebInitParam(name = "path", value = "/media/niko/ExternalHDD/books/java")
        }
)
public class FolderContent extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(FolderContent.class.getName());

    private String path;

    @Override
    public void init() throws ServletException {
        path = getInitParameter("path");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        File folder = new File(path);

        JSONObject jsonObject = listFilesForFolder(folder, 0);
        LOGGER.info(jsonObject.toString());

        out.print(jsonObject.toString());
    }

    public JSONObject listFilesForFolder(final File data, int level) {
        if (data.isDirectory()) {
            JSONObject jsonFolder = new JSONObject();

            jsonFolder.put("name", data.getName());

            JSONArray jsonFolders = new JSONArray();
            JSONArray jsonFiles = new JSONArray();

            File[] far = data.listFiles();
            if (far != null) {
                for (final File fileEntry : far) {
                    if (fileEntry.isDirectory()) {
                        jsonFolders.put(listFilesForFolder(fileEntry, level + 1));
                    } else {
                        jsonFiles.put(listFilesForFolder(fileEntry, level + 1));
                    }
                }
            }

            jsonFolder.put("folders", jsonFolders);
            jsonFolder.put("files", jsonFiles);
            jsonFolder.put("level", level);

            return jsonFolder;
        } else {
            JSONObject jsonFile = new JSONObject();

            jsonFile.put("name", data.getName());
            jsonFile.put("size", data.length());
            jsonFile.put("mimetype", getMimetype(data));
            jsonFile.put("level", level - 1);

            return jsonFile;
        }
    }

    public static String getMimetype(File file) {
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "-";
    }
}
