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

@WebServlet("/editar")
@SuppressWarnings("serial")
public class AlteracaoBrinquedo extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cod = request.getParameter("cod");

		Brinquedo brinquedo = new BrinquedoDAO().retrieve(cod);

		request.setAttribute("brinquedo", brinquedo);
		request.setAttribute("action", "editar");

		request.getRequestDispatcher("form_brinquedo.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String img = request.getParameter("inputImg");
		// Se não há imagem, arrega uma imagem padrão
		img = ((img == null || img.equals("")) ? "img.jpg" : img);	
		
		// Se não há o path, adiciona o path
		String parts[] = img.split("/");
		if (parts.length < 3) {
			img = "resources/pictures/" + img;
		}

		Brinquedo brinquedo = 
				new Brinquedo(
						Long.parseLong(request.getParameter("cod")),
						Long.parseLong(request.getParameter("inputCat")),
						request.getParameter("inputDesc"),
						request.getParameter("inputMarca"),
						img,
						request.getParameter("inputDetalhes"),
						new BigDecimal(request.getParameter("inputPreco")));

		new BrinquedoDAO().update(brinquedo);

		response.sendRedirect("lista");
	}
}
