package entrega1;

import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class InformacaoUsuario extends JFrame {
    private JTextArea listaUsuariosTextArea;
    private Socket socket;
    private JButton listarButton;
    private JButton voltarButton; 
    private TelaPrincipal telaPrincipal;
    private String token;

    public InformacaoUsuario(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Lista de Usuários");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        JPanel panel = new JPanel(new BorderLayout());

        listaUsuariosTextArea = new JTextArea();
        listaUsuariosTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(listaUsuariosTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        listarButton = new JButton("Listar Usuários");
        listarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {            	
                listarUsuarios();
            }
        });
        
        voltarButton = new JButton("Voltar");
        voltarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                voltarTelaAnterior();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(listarButton);
        buttonPanel.add(voltarButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setLocationRelativeTo(null);
    }

    private void listarUsuarios() {
    	if (JwtUtil.isUserAdmin(token)) {
	    	JSONObject mensagem = new JSONObject();  
	        mensagem.put("action", "listar-usuarios");
		    JSONObject data = new JSONObject();
		    data.put("token", token); 
		    mensagem.put("data", data);          
	
	            
	            try {
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	            out.println(mensagem.toString());
	            System.out.println("ListarUsuario-> Enviado para o servidor: "+mensagem);
	            
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String resposta = in.readLine();
	            if (resposta != null) {
		            System.out.println("ListarUsuario<-Recebida do servidor: "+ resposta);
		            JSONObject respostaJSON = new JSONObject(resposta);
		            String action = respostaJSON.optString("action");
		            
	               	Boolean error = respostaJSON.optBoolean("error");
	                String message = respostaJSON.optString("message");
		            if (error == null || error.toString().isEmpty()) {
		                JOptionPane.showMessageDialog(this, "'error' não enviado pelo Servidor ou nulo");
		                return; 
		            }
		            if (action == null || action.isEmpty()) {
		                JOptionPane.showMessageDialog(this, "'action' não enviado pelo Servidor ou nulo");
		            }
		            if (message == null || message.isEmpty()) {
		                JOptionPane.showMessageDialog(this, "'message' não enviado pelo Servidor ou nulo");
		            }
	
		
		            if (!error) {	            	           
		                JSONObject dataJson = respostaJSON.getJSONObject("data");
	
		                JSONArray usuarios = dataJson.getJSONArray("users");
		
		                StringBuilder lista = new StringBuilder();
		                lista.append("Usuários cadastrados:\n\n");
		
		                for (int i = 0; i < usuarios.length(); i++) {
		                    JSONObject usuario = usuarios.getJSONObject(i);
		                    int usuarioId = usuario.getInt("id");
		                    String nome = usuario.getString("name");
		                    String email = usuario.getString("email");
		                    String tipo = usuario.getString("type");
		
		                    lista.append("ID: ").append(usuarioId).append("\n");
		                    lista.append("Nome: ").append(nome).append("\n");
		                    lista.append("Email: ").append(email).append("\n");
		                    lista.append("Tipo: ").append(tipo).append("\n\n");
			                }
		                listaUsuariosTextArea.setText(lista.toString());
		            	JOptionPane.showMessageDialog(this, message);
	
		            }else {
		                listaUsuariosTextArea.setText("Erro ao listar usuários: "+message );
		                JOptionPane.showMessageDialog(this, message);
		            	}
		            }
				} catch (IOException e) {
				    System.out.println("ListarUsuario->Erro ao tentar enviar comando de listar usuarios: " + e.getMessage());
				    
				}
    	}else {
    		verMeusDados();
    	}
    }
    
    private void verMeusDados() {
    	JSONObject mensagem = new JSONObject();		
        mensagem.put("action", "pedido-proprio-usuario");
        JSONObject data = new JSONObject();
        data.put("token", token); 
        mensagem.put("data", data);
        
        try {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(mensagem.toString());
        System.out.println("InfoUsuario-> Enviado para o servidor: "+mensagem);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String resposta = in.readLine();
        if (resposta != null) {
            System.out.println("InfoUsuario<-Recebida do servidor: "+ resposta);
            JSONObject respostaJSON = new JSONObject(resposta);
            String action = respostaJSON.optString("action");
           	Boolean error = respostaJSON.optBoolean("error");
            String message = respostaJSON.optString("message");
            if (error == null || error.toString().isEmpty()) {
                JOptionPane.showMessageDialog(this, "'error' não enviado pelo Servidor ou nulo");
                return; 
            }
            if (action == null || action.isEmpty()) {
                JOptionPane.showMessageDialog(this, "'action' não enviado pelo Servidor ou nulo");
            }
            if (message == null || message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "'message' não enviado pelo Servidor ou nulo");
            }

            if (!error) {
            	           
                JSONObject dataJson = respostaJSON.getJSONObject("data");
            	JSONObject usuario = dataJson.getJSONObject("user");         		
                StringBuilder lista = new StringBuilder();
                lista.append("Meus dados de cadastrado:\n\n");

                Integer usuarioId = usuario.getInt("id");
                String nome = usuario.getString("name");
                String email = usuario.getString("email");
                String tipo = usuario.getString("type");
                
	            if (usuarioId == null || usuarioId.toString().isEmpty()) {
	                JOptionPane.showMessageDialog(this, "'id' não enviado pelo Servidor ou nulo");
	                return; 
	            }
	            if (nome == null || nome.isEmpty()) {
	                JOptionPane.showMessageDialog(this, "'name' não enviado pelo Servidor ou nulo");
	                return;
	            }
	            if (email == null || email.isEmpty()) {
	                JOptionPane.showMessageDialog(this, "'email' não enviado pelo Servidor ou nulo");
	                return;
	            }
	            if (tipo == null || tipo.isEmpty()) {
	                JOptionPane.showMessageDialog(this, "'type' não enviado pelo Servidor ou nulo");
	                return;
	            }

                lista.append("ID: ").append(usuarioId).append("\n");
                lista.append("Nome: ").append(nome).append("\n");
                lista.append("Email: ").append(email).append("\n");
                lista.append("Tipo: ").append(tipo).append("\n\n");

                listaUsuariosTextArea.setText(lista.toString());
                JOptionPane.showMessageDialog(this, message);
                 
            }else {
                listaUsuariosTextArea.setText("Erro ao listar usuários: "+message );
                JOptionPane.showMessageDialog(this, message);
            	}
            }
		} catch (IOException e) {
		    System.out.println("ListarUsuario->Erro ao tentar enviar comando de listar usuarios: " + e.getMessage());
		    
		}
    }



    private void voltarTelaAnterior() {
        setVisible(false);
        telaPrincipal.atualizarToken(token);
        telaPrincipal.setVisible(true);
    }
    public void atualizarSocket(Socket novoSocket) {
        this.socket = novoSocket;
    }  
    public void atualizarToken(String novoToken) {
        this.token = novoToken;
    }      
}
