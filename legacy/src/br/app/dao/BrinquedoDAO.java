package br.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.app.bean.Brinquedo;
import br.app.util.ConnectionFactory;

public class BrinquedoDAO {

	private Connection con;
	private PreparedStatement pstm;
	private ResultSet rs;
	
	private String sqlLista = 
			"SELECT * "
			+ "FROM brinquedo "
			+ "INNER JOIN estoque ON brinquedo_id = estoque_brinquedo_id "
			+ "INNER JOIN categoria ON brinquedo_categoria_id = categoria_id ";
	
	public List<Brinquedo>  listById (String id) {
		sqlLista += ((id != null && id.length() > 0) ? (" WHERE estoque_qtd > " + id) : ""); 
		return list();
	}
	
	public List<Brinquedo> listByFilter (String filter) {
		if(filter != null && filter.length() > 0) {
			sqlLista += " WHERE categoria_id='" + filter + "' "
					+ " OR brinquedo_descricao LIKE '%" + filter + "%' "
					+ " OR brinquedo_preco LIKE '" + filter + "%' ";
		}
		return list();
	}
	
	public List<Brinquedo>  listByCatId (String id) {
		sqlLista += ((id != null && id.length() > 0) ? (" WHERE categoria_id=" + id) : ""); 
		return list();
	}
	
	public List<Brinquedo> list () {

		List<Brinquedo> brinquedos = new ArrayList<>();
		try {
			con =  ConnectionFactory.getConnection();
			pstm = con.prepareStatement(sqlLista);
			rs = pstm.executeQuery();
			while (rs.next()) {
				brinquedos.add(
						new Brinquedo(
								rs.getLong("brinquedo_id"), rs.getLong("brinquedo_categoria_id"), rs.getString("brinquedo_descricao"),
								rs.getString("brinquedo_marca"), rs.getString("brinquedo_imagem_url"), rs.getString("brinquedo_detalhes"),
								rs.getBigDecimal("brinquedo_preco"), rs.getLong("estoque_qtd")
								)
						);
			}

			rs.close();
			pstm.close();
			con.close();

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
		return brinquedos;
	}

	public boolean create(Brinquedo brinquedo) {

		try {
			con =  ConnectionFactory.getConnection();

			String sql = 
					"INSERT INTO brinquedo(brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, "
							+ "brinquedo_categoria_id, brinquedo_marca)"
							+ "VALUES (?, ?, ?, ?, ?, ?)";
			pstm = con.prepareStatement(sql);
			pstm.setString(1, brinquedo.getDescricao());
			pstm.setString(2, brinquedo.getImgUrl());
			pstm.setBigDecimal(3, brinquedo.getPreco());
			pstm.setString(4, brinquedo.getDetalhes());
			pstm.setLong(5, brinquedo.getCodCategoria());
			pstm.setString(6, brinquedo.getMarca());

			pstm.execute();

			pstm.close();
			con.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public Brinquedo retrieve(String id) {

		String sql = "Select * FROM brinquedo WHERE brinquedo_id = ? ";

		Brinquedo brinquedo = null;
		try {
			con =  ConnectionFactory.getConnection();
			pstm = con.prepareStatement(sql);
			pstm.setString(1, id);
			rs = pstm.executeQuery();

			if (rs.next()) {
				brinquedo = new Brinquedo(
						rs.getLong("brinquedo_id"), rs.getLong("brinquedo_categoria_id"), rs.getString("brinquedo_descricao"),
						rs.getString("brinquedo_marca"), rs.getString("brinquedo_imagem_url"), rs.getString("brinquedo_detalhes"),
						rs.getBigDecimal("brinquedo_preco")
						);
			}

			rs.close();
			pstm.close();
			con.close();

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}

		return brinquedo;
	}

	public boolean update(Brinquedo brinquedo) {
		try {
			con =  ConnectionFactory.getConnection();

			String sql = "UPDATE brinquedo SET brinquedo_descricao=?, brinquedo_imagem_url=?, brinquedo_preco=?,"
					+ "brinquedo_detalhes=?, brinquedo_categoria_id=?, brinquedo_marca=? WHERE brinquedo_id=?";

			pstm = con.prepareStatement(sql);
			pstm.setString(1, brinquedo.getDescricao());
			pstm.setString(2, brinquedo.getImgUrl());
			pstm.setBigDecimal(3, brinquedo.getPreco());
			pstm.setString(4, brinquedo.getDetalhes());
			pstm.setLong(5, brinquedo.getCodCategoria());
			pstm.setString(6, brinquedo.getMarca());
			pstm.setLong(7, brinquedo.getCod());

			pstm.execute();

			pstm.close();
			con.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean delete (String rgm) {
		try {
			con =  ConnectionFactory.getConnection();

			String sql = "DELETE FROM brinquedo WHERE brinquedo_id = ?";
			pstm = con.prepareStatement(sql);

			pstm.setString(1, rgm);

			pstm.execute();

			pstm.close();
			con.close();

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}























