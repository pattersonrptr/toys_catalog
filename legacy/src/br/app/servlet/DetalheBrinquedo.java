package br.app.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.app.bean.Brinquedo;
import br.app.dao.BrinquedoDAO;

@WebServlet("/detalhe")
@SuppressWarnings("serial")
public class DetalheBrinquedo extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Brinquedo brinquedo = new BrinquedoDAO().retrieve(request.getParameter("cod"));
		
		request.setAttribute("cat", request.getParameter("cat"));
		request.setAttribute("brinquedo", brinquedo);
		request.getRequestDispatcher("detalhe_brinquedo.jsp").forward(request, response);
	}
}
