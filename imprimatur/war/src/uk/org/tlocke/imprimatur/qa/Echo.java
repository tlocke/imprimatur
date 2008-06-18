package uk.org.tlocke.imprimatur.qa;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Echo extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println("Imprimatur Echo Servlet - POST");
		out.println(request.getParameter("quote"));
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
