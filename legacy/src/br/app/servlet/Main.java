package br.app.servlet;

import java.io.IOException;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Main
 */
@SuppressWarnings("serial")
@WebServlet(description = "Servlet principal, responsável por chamar a view index.jsp", urlPatterns = { "/index" })
public class Main extends HttpServlet {       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int year = LocalDate.now().getYear(); // Ano que será mostrado no rodapé do site
		request.setAttribute("ano", year);
		request.getRequestDispatcher("index.jsp").forward(request, response);
	}

}
