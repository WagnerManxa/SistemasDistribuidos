package entrega1;

import javax.swing.*;

import org.json.JSONObject;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;



public class CadastroSegmento extends JFrame {
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

    public CadastroSegmento(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Cadastro de Segmento");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        //botoes//
        JButton cadastrarButton = new JButton("Cadastrar");
        cadastrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cadastrarSegmento();
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
                
        JLabel origemIdLabel = new JLabel("Id Origem:");
        origemIdField = new JTextField(20);

        JLabel origemNomeLabel = new JLabel("Nome Origem:");
        origemNomeField = new JTextField(20);

        JLabel origemObsLabel = new JLabel("Obs Origem:");
        origemObsField = new JTextField(20);
        
        JLabel destinoIdLabel = new JLabel("Id Destino:");
        destinoIdField = new JTextField(20);

        JLabel destinoNomeLabel = new JLabel("Nome Destino:");
        destinoNomeField = new JTextField(20);

        JLabel destinoObsLabel = new JLabel("Obs Destino:");
        destinoObsField = new JTextField(20);

        JLabel direcaoLabel = new JLabel("Direção:");
        direcaoField = new JTextField(20);

        JLabel distanciaLabel = new JLabel("Distância:");
        distanciaField = new JTextField(20);
        
        JLabel segmentoObsLabel = new JLabel("Obs Segmento:");
        segmentoObsField = new JTextField(20);

        //segmento //
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(origemIdLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(origemIdField, constraints);
        
        //origem//
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(origemNomeLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(origemNomeField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(origemObsLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        panel.add(origemObsField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(destinoIdLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        panel.add(destinoIdField, constraints);
        
        //destino//
        constraints.gridx = 0;
        constraints.gridy = 4;
        panel.add(destinoNomeLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;
        panel.add(destinoNomeField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 5;
        panel.add(destinoObsLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 5;
        panel.add(destinoObsField, constraints);
        
        // direcao -- distancia -- obs do segmento//
        
        constraints.gridx = 0;
        constraints.gridy = 6;
        panel.add(direcaoLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 6;
        panel.add(direcaoField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 7;
        panel.add(distanciaLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 7;
        panel.add(distanciaField, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 8;
        panel.add(segmentoObsLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 8;
        panel.add(segmentoObsField, constraints);
        
        //botoes
        
        constraints.gridx = 1;
        constraints.gridy = 9;
        panel.add(cadastrarButton, constraints);

        constraints.gridx = 1;
        constraints.gridy = 10;
        panel.add(voltarButton, constraints);        
             
        constraints.gridx = 0;
        constraints.gridy = 9;
        panel.add(cancelarButton, constraints);

       
        getContentPane().add(panel);

        pack();
        setLocationRelativeTo(null);
        limparCampos();	
    }
    
    private void limparCampos() {

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

    private void cadastrarSegmento() {

		String idOrigemStr = origemIdField.getText();
		String nomeOrigem = origemNomeField.getText();
		String obsOrigem = origemObsField.getText();
		String idDestinoStr = destinoIdField.getText();
		String nomeDestino = destinoNomeField.getText();
		String obsDestino = destinoObsField.getText();    
		String direcaoSegmento = direcaoField.getText();
		String distanciaSegmentoStr = distanciaField.getText();
		String obsSegmento = segmentoObsField.getText();
		
        if (idOrigemStr.isEmpty() || nomeOrigem.isEmpty() || idDestinoStr.isEmpty() || nomeDestino.isEmpty() || direcaoSegmento.isEmpty() || distanciaSegmentoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, verifique se todos os campos estão preenchidos.");
            return;
        }
        Integer distanciaSegmento;
        Integer idOrigem;
        Integer idDestino;
        try {
        	distanciaSegmento = Integer.parseInt(distanciaSegmentoStr);
            idOrigem = Integer.parseInt(idOrigemStr);
            idDestino = Integer.parseInt(idDestinoStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Os campos 'id' e 'distancia' devem ser um número inteiro.");
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
        	mensagem.put("action", "cadastro-segmento");       	
        	
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
	        data.put("segmento", segmento);
	        mensagem.put("data", data);

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);            
            out.println(mensagem.toString());
            System.out.println("TelaCadastroSegmento-> Enviado para o servidor: "+mensagem);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
            if (resposta != null) {
	            System.out.println("TelaCadastroSegmento<-Recebida do servidor: "+ resposta);
	            JSONObject respostaJSON = new JSONObject(resposta);           
                String action = respostaJSON.optString("action");
               	Boolean error = respostaJSON.optBoolean("error");
                String message = respostaJSON.optString("message");
                if (error == null || error.toString().isEmpty()) {
	                JOptionPane.showMessageDialog(this, "Campo 'error' não enviado pelo Servidor ou nulo");
	                return; 
	            }
	            if (action == null || action.isEmpty()) {
	                JOptionPane.showMessageDialog(this, "Campo 'action' não enviado pelo Servidor ou nulo");
	            }
	            if (message == null || message.isEmpty()) {
	                JOptionPane.showMessageDialog(this, "Campo 'message' não enviado pelo Servidor ou nulo");
	            }

             if (!error) {
                JOptionPane.showMessageDialog(this, message);
                setVisible(false);
                telaPrincipal.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, message);
            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void voltarTelaPrincipal() {
    	telaPrincipal.atualizarToken(token);
        telaPrincipal.setVisible(true);
        dispose();
    }
    public void atualizarSocket(Socket novoSocket) {
        this.socket = novoSocket;
    }  
    public void atualizarToken(String novoToken) {
        this.token = novoToken;
    }  

}

