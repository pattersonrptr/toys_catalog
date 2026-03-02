package br.app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
	public static Connection getConnection()  throws SQLException , ClassNotFoundException {  
		Class.forName("com.mysql.jdbc.Driver");

		//Configura os parâmetros da conexão  
		String url = "jdbc:mysql://localhost:3306/brinquedos";  
		String username = "patterson";   
		String password = "1234";  

		//Processa e retorna a conexão
		return DriverManager.getConnection(url, username, password);  
	}	
}
