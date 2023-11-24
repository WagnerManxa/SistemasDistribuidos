package entrega1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import org.json.JSONArray;
import org.json.JSONObject;

public class EditarSegmento extends JFrame {
    private JTextField idSegmentoField;
    private JTextField origemIdField;
    private JTextField origemNomeField;
    private JTextField origemObsField;
    private JTextField destinoIdField;
    private JTextField destinoNomeField;
    private JTextField destinoObsField;    
    private JTextField direcaoField;
    private JTextField distanciaField;
    private JTextField segmentoObsField;
    private Socket socket;
    private TelaPrincipal telaPrincipal;
    private String token;

    public EditarSegmento(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Editar Segmento");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        //botoes//
        JButton buscarButton = new JButton("Buscar Segmento");
        buscarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pedidoEdicaoSegmento();
            }
        });

        JButton salvarButton = new JButton("Salvar Alterações");
        salvarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                edicaoSegmento();
            }
        });
        

        JButton voltarButton = new JButton("Voltar");
        voltarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                voltarTelaPrincipal();
            }
        });

        JButton cancelarButton = new JButton("Cancelar");
        cancelarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                limparCampos();
            }
        });
        
        //labels e text fields
        
        JLabel idSegmentoLabel = new JLabel("Id Segmento:");
        idSegmentoField = new JTextField(20);
        idSegmentoField.setEnabled(false);
        
        JLabel origemIdLabel = new JLabel("Id Origem:");
        origemIdField = new JTextField(20);
        origemIdField.setEnabled(false);

        JLabel origemNomeLabel = new JLabel("Nome Origem:");
        origemNomeField = new JTextField(20);
        origemNomeField.setEnabled(false);

        JLabel origemObsLabel = new JLabel("Obs Origem:");
        origemObsField = new JTextField(20);
        origemObsField.setEnabled(false);
        
        JLabel destinoIdLabel = new JLabel("Id Destino:");
        destinoIdField = new JTextField(20);
        destinoIdField.setEnabled(false);

        JLabel destinoNomeLabel = new JLabel("Nome Destino:");
        destinoNomeField = new JTextField(20);
        destinoNomeField.setEnabled(false);

        JLabel destinoObsLabel = new JLabel("Obs Destino:");
        destinoObsField = new JTextField(20);
        destinoObsField.setEnabled(false);

        JLabel direcaoLabel = new JLabel("Direção:");
        direcaoField = new JTextField(20);
        direcaoField.setEnabled(false);

        JLabel distanciaLabel = new JLabel("Distância:");
        distanciaField = new JTextField(20);
        distanciaField.setEnabled(false);
        
        JLabel segmentoObsLabel = new JLabel("Obs Segmento:");
        segmentoObsField = new JTextField(20);
        segmentoObsField.setEnabled(false);

        //segmento //
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(idSegmentoLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(idSegmentoField, constraints);
        
        //origem//
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(origemIdLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(origemIdField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(origemNomeLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        panel.add(origemNomeField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(origemObsLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        panel.add(origemObsField, constraints);
        
        //destino//
        constraints.gridx = 0;
        constraints.gridy = 4;
        panel.add(destinoIdLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;
        panel.add(destinoIdField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 5;
        panel.add(destinoNomeLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 5;
        panel.add(destinoNomeField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 6;
        panel.add(destinoObsLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 6;
        panel.add(destinoObsField, constraints);
        
        // direcao -- distancia -- obs do segmento//
        
        constraints.gridx = 0;
        constraints.gridy = 7;
        panel.add(direcaoLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 7;
        panel.add(direcaoField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 8;
        panel.add(distanciaLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 8;
        panel.add(distanciaField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 9;
        panel.add(segmentoObsLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 9;
        panel.add(segmentoObsField, constraints);        
        
       //botoes
        
        constraints.gridx = 1;
        constraints.gridy = 10;
        panel.add(buscarButton, constraints);
       
        constraints.gridx = 1;
        constraints.gridy = 11;
        panel.add(salvarButton, constraints);
       
        constraints.gridx = 0;
        constraints.gridy = 11;
        panel.add(voltarButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 10;
        panel.add(cancelarButton, constraints);
                     
        getContentPane().add(panel);

        pack();
        setLocationRelativeTo(null);
        limparCampos();	
    }
    
    private void limparCampos() {
		idSegmentoField.setEnabled(true);;
		origemIdField.setEnabled(false);
		origemNomeField.setEnabled(false);
		origemObsField.setEnabled(false);
		destinoIdField.setEnabled(false);
		destinoNomeField.setEnabled(false);
		destinoObsField.setEnabled(false);    
		direcaoField.setEnabled(false);
		distanciaField.setEnabled(false);
		segmentoObsField.setEnabled(false);
		
		idSegmentoField.setText("");
		origemIdField.setText("");
		origemNomeField.setText("");
		origemObsField.setText("");
		destinoIdField.setText("");
		destinoNomeField.setText("");
		destinoObsField.setText("");    
		direcaoField.setText("");
		distanciaField.setText("");
		segmentoObsField.setText("");
    }

    private void pedidoEdicaoSegmento() {
        String idSegmentoStr = idSegmentoField.getText();
        if (idSegmentoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o ID do segmento.");
            return;
        }

        Integer idSegmento;
        try {
            idSegmento = Integer.parseInt(idSegmentoStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID do segmento deve ser um número inteiro.");
            return;
        }

        JSONObject mensagem = new JSONObject();
        mensagem.put("action", "pedido-edicao-segmento");
        JSONObject data = new JSONObject();
        data.put("token", token);
        data.put("segmento_id", idSegmento);
        mensagem.put("data", data);

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mensagem.toString());
            System.out.println("PedidoEdicaoSegmento-> Enviado para o servidor: " + mensagem);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
            System.out.println("PedidoEdicaoSegmento<- Recebida do servidor: " + resposta);

            JSONObject respostaJson = new JSONObject(resposta);
            Boolean error = respostaJson.optBoolean("error");
            String message = respostaJson.optString("message");
                        
            if (error == null || error.toString().isEmpty()) {
                JOptionPane.showMessageDialog(this, "'error' não enviado pelo Servidor ou nulo");
                return;
            }

            if (!error) {
            	JSONObject respostaData = new JSONObject();
            	respostaData = respostaJson.optJSONObject("data");
            	if (respostaData == null || respostaData.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'data' não enviado pelo Servidor ou nulo");
                    return;
            	}
            	JSONObject respostaSegmento = new JSONObject();
                respostaSegmento = respostaData.optJSONObject("segmento");
                
                if (respostaSegmento == null || respostaSegmento.toString().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'segmento' não enviado pelo Servidor ou nulo");
                    return;
                }
                Integer segmentoId = respostaSegmento.optInt("segmento_id");
                if (segmentoId == null || segmentoId.toString().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'segmento_id' não enviado pelo Servidor ou nulo");
                    return;
                }
                
                String direcao = respostaSegmento.optString("direcao");	
                if (direcao == null || direcao.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'direcao' não enviado pelo Servidor ou nulo");
                    return;
                }
                
                String distancia = respostaSegmento.optString("distancia");
                if (distancia == null || distancia.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'error' não enviado pelo Servidor ou nulo");
                    return;
                }
                
                String obsSegmento = respostaSegmento.optString("obs");
                if (obsSegmento == null || obsSegmento.isEmpty()) {
                	System.out.println("'obs' não enviado pelo Servidor ou nulo");
                }
                
                JSONObject pontoOrigem = respostaSegmento.getJSONObject("ponto_origem");
                if (pontoOrigem == null || pontoOrigem.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'ponto_origem' não enviado pelo Servidor ou nulo");
                    return;
                }
                Integer origemId = pontoOrigem.optInt("id");
                if (origemId == null || origemId.toString().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'id' da origem não enviado pelo Servidor ou nulo");
                    return;
                }
                String nameOrigem = pontoOrigem.optString("name");
                if (nameOrigem == null || nameOrigem.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'name' da origem não enviado pelo Servidor ou nulo");
                    return;
                }
                
                String obsOrigem = pontoOrigem.optString("obs");
                if (obsOrigem == null || obsOrigem.isEmpty()) {
                	System.out.println("'obs' da origem não enviado pelo Servidor ou nulo");
                }
                
                JSONObject pontoDestino = respostaSegmento.getJSONObject("ponto_destino");
                if (pontoDestino == null || pontoDestino.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'ponto_destino' não enviado pelo Servidor ou nulo");
                    return;
                }
                
                Integer destinoId = pontoDestino.optInt("id");
                if (destinoId == null || destinoId.toString().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'id' do destino não enviado pelo Servidor ou nulo");
                    return;
                }
                String nameDestino = pontoDestino.optString("name");
                if (nameDestino == null || nameDestino.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "'name' do destino não enviado pelo Servidor ou nulo");
                    return;
                }
                String obsDestino = pontoDestino.optString("obs");
                if (obsDestino == null || obsDestino.isEmpty()) {
                	System.out.println("'obs' do destino não enviado pelo Servidor ou nulo");
                }
                
                
            	idSegmentoField.setEnabled(false);
        		origemIdField.setEnabled(true);
        		origemNomeField.setEnabled(true);
        		origemObsField.setEnabled(true);
        		destinoIdField.setEnabled(true);
        		destinoNomeField.setEnabled(true);
        		destinoObsField.setEnabled(true);   
        		direcaoField.setEnabled(true);
        		distanciaField.setEnabled(true);
        		segmentoObsField.setEnabled(true);
        		
        		idSegmentoField.setText(idSegmentoStr);
        		origemIdField.setText(origemId.toString());
        		origemNomeField.setText(nameOrigem);
        		origemObsField.setText(obsOrigem);
        		destinoIdField.setText(destinoId.toString());
        		destinoNomeField.setText(nameDestino);
        		destinoObsField.setText(obsDestino);    
        		direcaoField.setText(direcao);
        		distanciaField.setText(distancia);
        		segmentoObsField.setText(obsSegmento);

                JOptionPane.showMessageDialog(this, message);
            } else {
                JOptionPane.showMessageDialog(this, message);
                limparCampos();
            }

        } catch (SocketException se) {
            JOptionPane.showMessageDialog(this, "A conexão foi encerrada pelo servidor. Tente novamente.");
            voltarTelaPrincipal();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao receber resposta do servidor.");
        }
    }
               
    private void edicaoSegmento() {
    	String idSegmentoStr = idSegmentoField.getText();
		String idOrigemStr = origemIdField.getText();
		String nomeOrigem = origemNomeField.getText();
		String obsOrigem = origemObsField.getText();
		String idDestinoStr = destinoIdField.getText();
		String nomeDestino = destinoNomeField.getText();
		String obsDestino = destinoObsField.getText();    
		String direcaoSegmento = direcaoField.getText();
		String distanciaSegmento = distanciaField.getText();
		String obsSegmento = segmentoObsField.getText();
		
        if (idSegmentoStr.isEmpty() ||idOrigemStr.isEmpty() || nomeOrigem.isEmpty() || idDestinoStr.isEmpty() || nomeDestino.isEmpty() || direcaoSegmento.isEmpty() || distanciaSegmento.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, verifique se todos os campos estão preenchidos.");
            return;
        }

        Integer idSegmento;
        Integer idOrigem;
        Integer idDestino;
        try {
            idSegmento = Integer.parseInt(idSegmentoStr);
            idOrigem = Integer.parseInt(idOrigemStr);
            idDestino = Integer.parseInt(idDestinoStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Os campos 'id' devem ser um número inteiro.");
            return;
        }
        
        if (obsSegmento.isEmpty()) {
        	obsSegmento = null;
        }
        
        if (obsOrigem.isEmpty()) {
        	obsOrigem = null;
        }
        
        if (obsDestino.isEmpty()) {
        	obsDestino = null;
        }
        	
	        JSONObject mensagem = new JSONObject();
        	mensagem.put("action", "edicao-segmento");       	
        	
        	JSONObject pontoOrigem = new JSONObject();
        	pontoOrigem.put("id", idOrigem);
        	pontoOrigem.put("name", nomeOrigem);
        	pontoOrigem.put("obs", obsOrigem);
        	
        	JSONObject pontoDestino = new JSONObject();
        	pontoDestino.put("id", idDestino);
        	pontoDestino.put("name", nomeDestino);
        	pontoDestino.put("obs", obsDestino);
        	
        	JSONObject segmento = new JSONObject();
        	segmento.put("ponto_origem", pontoOrigem);
        	segmento.put("ponto_destino", pontoDestino);
        	segmento.put("direcao", direcaoSegmento);
        	segmento.put("distancia", distanciaSegmento);
        	segmento.put("obs", obsSegmento);
        	JSONObject data = new JSONObject();
	        data.put("token", token);
	        data.put("segmento_id", idSegmento);
	        data.put("segmento", segmento);
	        mensagem.put("data", data);
	        
	        try {
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);           
	            out.println(mensagem.toString());
	            System.out.println("EdicaoSegmento-> Enviado para o servidor: "+mensagem);
	    
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String resposta = in.readLine();
	            System.out.println("EdicaoSegmento<- Resposta do servidor: " + resposta);
	            
	            JSONObject respostaJson = new JSONObject(resposta);
	            String action = respostaJson.optString("action");
	           	Boolean error = respostaJson.optBoolean("error");
	            String message = respostaJson.optString("message");
	            if (error == null || error.toString().isEmpty()) {
	                JOptionPane.showMessageDialog(this, "'error' não enviado pelo Servidor ou nulo");
	                return; 
	            }
	            if (action == null|| action.isEmpty() ) {
	                JOptionPane.showMessageDialog(this, "'action' não enviado pelo Servidor ou nulo");
	            }
	            if (message == null || message.isEmpty()) {
	                JOptionPane.showMessageDialog(this, "'message' não enviado pelo Servidor ou nulo");
	            }
	            if(error) {
	            	JOptionPane.showMessageDialog(this, message);
	            	return;
	            }else {
	            	JOptionPane.showMessageDialog(this, message);
	            	limparCampos();
	            }
	
	        } catch (SocketException se) {
	            JOptionPane.showMessageDialog(this, "EdicaoSegmento: A conexão foi encerrada pelo servidor. Tente novamente.");
	            voltarTelaPrincipal();
	        } catch (IOException e) {
	            e.printStackTrace();
	            JOptionPane.showMessageDialog(this, "EdicaoSegmento: Erro ao receber resposta do servidor.");
	        }

    }
    
    private void voltarTelaPrincipal() {
    	limparCampos();
        setVisible(false);
        telaPrincipal.setVisible(true);
    }

    public void atualizarSocket(Socket novoSocket) {
        this.socket = novoSocket;
    }  
    
    public void atualizarToken(String novoToken) {
        this.token = novoToken;
    } 
}
