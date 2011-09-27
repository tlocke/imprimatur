package uk.org.tlocke.imprimatur.qa;

import java.io.*;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class Echo extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println("Imprimatur Echo Servlet - POST");
		out.println(request.getParameter("quote"));
		if (ServletFileUpload.isMultipartContent(request)) {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				for (FileItem item : (List<FileItem>) upload
						.parseRequest(request)) {
					if (item.isFormField()) {
						out.println(item.getString());
					} else {
						out.println("filename: " + item.getFieldName());
						out.println("name: " + item.getName());
						InputStream is = item.getInputStream();
						Reader reader = new InputStreamReader(is, "UTF-8");
						LineNumberReader lnr = new LineNumberReader(reader);
						String line = null;
						while ((line = lnr.readLine()) != null) {
							out.print(line);
						}
					}
				}
			} catch (FileUploadException e) {
				throw new ServletException(e);
			}
		}
		out.close();
	}

	public void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType(request.getContentType());
		PrintWriter out = response.getWriter();
		out.println("Imprimatur Echo Servlet - PUT");
		BufferedReader reader = request.getReader();

		String line;
		while ((line = reader.readLine()) != null) {
			out.println(line);
		}
		out.close();
	}

}
