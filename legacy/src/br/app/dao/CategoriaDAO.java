package br.app.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.app.bean.Categoria;
import br.app.util.ConnectionFactory;

public class CategoriaDAO {

	private Connection con;
	private CallableStatement csmt = null; // permite chamar procedures e functions
	private PreparedStatement pstm = null;
	private ResultSet rs = null;

	public List<String> listcategoriaBrinquedoQtd() {

		List<String> result = new ArrayList<>();
		try {
			con =  ConnectionFactory.getConnection();
			csmt = con.prepareCall("{call categoriaBrinquedoQtd()}");
			rs = csmt.executeQuery();
			while(rs.next()) {
				result.add(
						rs.getInt("brinquedo_categoria_qtd") +
						"#" +
						rs.getString("categoria_nome") +
						 "#" +
						rs.getString("brinquedo_imagem_url") +
						"#" +
						rs.getLong("categoria_id") 
						);
			}
			
			csmt.close();
			rs.close();
			con.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

	public Categoria retrieve(String id) {

		String sql = "Select categoria_id, CAP_FIRST(categoria_nome) as categoria_nome FROM categoria WHERE categoria_id = ? ";

		Categoria Categoria = null;
		try {
			con =  ConnectionFactory.getConnection();
			pstm = con.prepareStatement(sql);
			pstm.setString(1, id);
			rs = pstm.executeQuery();

			if (rs.next()) {
				Categoria = new Categoria(rs.getLong("categoria_id"), rs.getString("categoria_nome"));
			}

			rs.close();
			pstm.close();
			con.close();

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}

		return Categoria;
	}
}
