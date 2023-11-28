package entrega1;

import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class InformacaoPonto extends JFrame {
    private JTextArea listaPontosTextArea;
    private Socket socket;
    private JButton listarButton;
    private JButton voltarButton; 
    private TelaPrincipal telaPrincipal;
    private String token;

    public InformacaoPonto(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Lista de Pontos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        JPanel panel = new JPanel(new BorderLayout());

        listaPontosTextArea = new JTextArea();
        listaPontosTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(listaPontosTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        listarButton = new JButton("Listar Pontos");
        listarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {            	
                listarPontos();
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

    private void listarPontos() {
	    	JSONObject mensagem = new JSONObject();  
	        mensagem.put("action", "listar-pontos");
		    JSONObject data = new JSONObject();
		    data.put("token", token); 
		    mensagem.put("data", data);          
	
	            
	            try {
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	            out.println(mensagem.toString());
	            System.out.println("ListarPontos-> Enviado para o servidor: "+mensagem);
	            
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String resposta = in.readLine();
	            if (resposta != null) {
		            System.out.println("ListarPontos<-Recebida do servidor: "+ resposta);
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
	
		                JSONArray pontos = dataJson.getJSONArray("pontos");
		
		                StringBuilder lista = new StringBuilder();
		                lista.append("---------------------------------PONTOS---------------------------------\n\n");
		
		                for (int i = 0; i < pontos.length(); i++) {
		                    JSONObject ponto = pontos.getJSONObject(i);
		                    Integer pontoId = ponto.getInt("id");
		                    String nome = ponto.getString("name");
		                    String obs = ponto.getString("obs");
		                    if (obs.isEmpty() || obs == null) {
		                    	obs = "null";
		                    }

		
		                    lista.append("ID: ").append(pontoId).append("\n");
		                    lista.append("Nome: ").append(nome).append("\n");
		                    lista.append("Obs: ").append(obs).append("\n");
		                    lista.append("\n");
							lista.append("____________________________________________________");
							lista.append("\n");

			                }
		                listaPontosTextArea.setText(lista.toString());
		            	JOptionPane.showMessageDialog(this, message);
	
		            }else {
		                listaPontosTextArea.setText("Erro ao listar pontos: "+message );
		                JOptionPane.showMessageDialog(this, message);
		            	}
		            }
				} catch (IOException e) {
				    System.out.println("ListarPontos->Erro ao tentar enviar comando de listar pontos: " + e.getMessage());
				    
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
