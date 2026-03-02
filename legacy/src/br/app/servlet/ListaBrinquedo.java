 package br.app.servlet;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.app.bean.Brinquedo;
import br.app.bean.Categoria;
import br.app.dao.BrinquedoDAO;
import br.app.dao.CategoriaDAO;

@WebServlet("/lista")
@SuppressWarnings("serial")
public class ListaBrinquedo extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String page = "tabela_brinquedo.jsp";
		List<Brinquedo> brinquedos = null;
		String tipo = 	((request.getParameter("tipo") == null) ? "" : request.getParameter("tipo"));
		String id = 	((request.getParameter("idCategoria") == null) ? "" : request.getParameter("idCategoria"));
		String filtro = ((request.getParameter("filtro") == null) ? "" : request.getParameter("filtro"));

		if (tipo.equals("destaque")) {
			brinquedos = new BrinquedoDAO().list();
			if(! (brinquedos == null)) { // Se há brinquedos
				/* Decidi que os brinquedos em destaque serão os com maior quantidade em estoque
				 * pois são os que mais precisam sair. */
				brinquedos = brinquedos.stream()
						.filter(b -> b.getQtdEstoque() > 0) // Se não existir no estoque, não quero na página destaques	
						.sorted(comparing(Brinquedo::getQtdEstoque).reversed()) // Ordeno pelo campo qtdEstoque e inverto para pegar os que tem em maior número no estoque.
						.limit(4) // Só quero mostrar os 4 primeiros na página de destaques.
						.collect(toList()); // Coleto a lista resultante.
			}

			request.setAttribute("brinquedos", brinquedos);
		} else 
			if(tipo.equals("catalogo")) {
				List<String> categorias = new CategoriaDAO().listcategoriaBrinquedoQtd();
				request.setAttribute("categorias", categorias);
			} else 
				if (tipo.equals("categoria")) {
					brinquedos = new BrinquedoDAO().listByCatId(id)
							.stream()
							.filter(b -> b.getQtdEstoque() > 0)
							.collect(toList());
					Categoria categoria = new CategoriaDAO().retrieve(id);
					request.setAttribute("categoria", categoria);
					request.setAttribute("brinquedos", brinquedos);
				} 
				else {
					page = "lista_brinquedos.jsp";
					brinquedos = new BrinquedoDAO().listByFilter(filtro);
					request.setAttribute("brinquedos", brinquedos);
				}

		request.setAttribute("tipo", tipo);
		request.getRequestDispatcher(page).forward(request, response);
	}
}
