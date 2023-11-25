package entrega1;

import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class InformacaoSegmento extends JFrame {
    private JTextArea listaSegmentosTextArea;
    private Socket socket;
    private JButton listarButton;
    private JButton voltarButton; 
    private TelaPrincipal telaPrincipal;
    private String token;

    public InformacaoSegmento(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Lista de Segmentos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        JPanel panel = new JPanel(new BorderLayout());

        listaSegmentosTextArea = new JTextArea();
        listaSegmentosTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(listaSegmentosTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        listarButton = new JButton("Listar Segmentos");
        listarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {            	
                listarSegmentos();
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

    private void listarSegmentos() {
	    	JSONObject mensagem = new JSONObject();  
	        mensagem.put("action", "listar-segmentos");
		    JSONObject data = new JSONObject();
		    data.put("token", token); 
		    mensagem.put("data", data);          
	
	            
	            try {
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	            out.println(mensagem.toString());
	            System.out.println("ListarSegmentos-> Enviado para o servidor: "+mensagem);
	            
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String resposta = in.readLine();
	            if (resposta != null) {
		            System.out.println("ListarSegmentos<-Recebida do servidor: "+ resposta);
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
		                if (dataJson == null || dataJson.isEmpty()) {
			                JOptionPane.showMessageDialog(this, "'data' não enviado pelo Servidor ou nulo");
			            }
		                
		                StringBuilder lista = new StringBuilder();
		                JSONArray segmentos = dataJson.getJSONArray("segmentos");
		                if (segmentos == null || segmentos.isEmpty()) {
		                	lista.append("Nenhum segmento enviado pelo Servidor");
		                	System.out.println("Nenhum segmento enviado pelo Servidor ou recebido campo 'segmentos' nulo");
			            }
		                
		                else {
		                lista.append("----------------------SEGMENTOS----------------------\n\n");
		                for (int i = 0; i < segmentos.length(); i++) {
							JSONObject segmento = segmentos.getJSONObject(i);
							Integer segmentoId = segmento.getInt("id");
							String direcao = segmento.getString("direcao");
							Integer distancia = segmento.getInt("distancia");
							String obs = segmento.getString("obs");
							JSONObject pontoOrigem = segmento.getJSONObject("ponto_origem");
							Integer pontoOrigemId = pontoOrigem.getInt("id");
							String pontoOrigemNome = pontoOrigem.getString("name");
							String pontoOrigemObs = pontoOrigem.getString("obs");
							JSONObject pontoDestino = segmento.getJSONObject("ponto_destino");
							Integer pontoDestinoId = pontoDestino.getInt("id");
							String pontoDestinoNome = pontoDestino.getString("name");
							String pontoDestinoObs = pontoDestino.getString("obs");              						
							lista.append("ID Segmento: ").append(segmentoId).append("\n");
							lista.append("-----\n");
							lista.append("Ponto de Origem ").append("\n");
							lista.append("ID: ").append(pontoOrigemId).append("\n");
							lista.append("Nome: ").append(pontoOrigemNome).append("\n");
							lista.append("Obs: ").append(pontoOrigemObs).append("\n");
							lista.append("-----\n");
							lista.append("Ponto de Destino ").append("\n");
							lista.append("ID: ").append(pontoDestinoId).append("\n");
							lista.append("Nome: ").append(pontoDestinoNome).append("\n");
							lista.append("Obs: ").append(pontoDestinoObs).append("\n");
							lista.append("-----\n");
							lista.append("Direção: ").append(direcao).append("\n");
							lista.append("Distância: ").append(distancia).append("\n");
							lista.append("Obs do segmento: ").append(obs).append("\n");
							lista.append("\n");
							lista.append("____________________________________________________");
							lista.append("\n");
		                }
			                }
		                listaSegmentosTextArea.setText(lista.toString());
		            	JOptionPane.showMessageDialog(this, message);
	
		            }else {
		                listaSegmentosTextArea.setText("Erro ao listar os Segmentos: "+message );
		                JOptionPane.showMessageDialog(this, message);
		            	}
		            }
				} catch (IOException e) {
				    System.out.println("ListarSegmentos->Erro ao tentar enviar comando de listar segmentos: " + e.getMessage());
				    
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
