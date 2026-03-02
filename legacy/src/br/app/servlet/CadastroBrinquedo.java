package br.app.servlet;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.app.bean.Brinquedo;
import br.app.dao.BrinquedoDAO;

@WebServlet("/novo")
@SuppressWarnings("serial")
public class CadastroBrinquedo extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("action", "novo");
		request.getRequestDispatcher("form_brinquedo.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String img = request.getParameter("inputImg");
		// Se não há imagem, carrega uma imagem padrão
		img = ((img == null || img.equals("")) ? "resources/pictures/img.jpg" : "resources/pictures/" + img);
		System.out.println(request.getParameter("inputCodigo"));
		Brinquedo brinquedo = 
				new Brinquedo(
						0,
						Long.parseLong(request.getParameter("inputCat")),
						request.getParameter("inputDesc"),
						request.getParameter("inputMarca"),
						img,
						request.getParameter("inputDetalhes"),
						new BigDecimal(request.getParameter("inputPreco")));
		
		new BrinquedoDAO().create(brinquedo);
		
		response.sendRedirect("lista");
	}
}
