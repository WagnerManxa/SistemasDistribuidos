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
import org.json.JSONObject;

public class EditarPonto extends JFrame {
    private JTextField idPontoField;
    private JTextField nomeFieldEditar;
    private JTextField obsFieldEditar;
    private Socket socket;
    private TelaPrincipal telaPrincipal;
    private String token;

    public EditarPonto(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Editar Ponto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel idPontoLabel = new JLabel("ID do Ponto:");
        idPontoField = new JTextField();
        idPontoField.setPreferredSize(new Dimension(150, 25));

        JLabel nomeLabelEditar = new JLabel("Nome:");
        nomeFieldEditar = new JTextField(20);
        nomeFieldEditar.setEnabled(false);
        
        JLabel obsLabelEditar = new JLabel("Obs:");
        obsFieldEditar = new JTextField(20);
        obsFieldEditar.setEnabled(false);
        
        

        JButton buscarButton = new JButton("Buscar Ponto");
        buscarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pedidoEdicaoPonto();
            }
        });

        JButton salvarButton = new JButton("Salvar Alterações");
        salvarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                edicaoPonto();
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

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(idPontoLabel, constraints);

        constraints.gridx = 1;
        panel.add(idPontoField, constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        panel.add(nomeLabelEditar, constraints);

        constraints.gridx = 1;
        panel.add(nomeFieldEditar, constraints);

        constraints.gridy = 2;
        constraints.gridx = 0;
        panel.add(obsLabelEditar, constraints);

        constraints.gridx = 1;
        panel.add(obsFieldEditar, constraints);

        constraints.gridy = 5;
        constraints.gridx = 1;
        panel.add(buscarButton, constraints);

        constraints.gridy = 6;
        constraints.gridx = 1;
        panel.add(salvarButton, constraints);

        constraints.gridy = 6;
        constraints.gridx = 0;
        panel.add(voltarButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        panel.add(cancelarButton, constraints);


        getContentPane().add(panel);

        pack();
        setLocationRelativeTo(null);
        limparCampos();
    }
    private void limparCampos() {
    	nomeFieldEditar.setText("");
        obsFieldEditar.setText("");
        idPontoField.setText("");
        idPontoField.setEnabled(true);
    	nomeFieldEditar.setEnabled(false);
        obsFieldEditar.setEnabled(false);
    }

    private void pedidoEdicaoPonto() {
	        String idPontoStr = idPontoField.getText();
	        if (idPontoStr.isEmpty()) {
	            JOptionPane.showMessageDialog(this, "Por favor, informe o ID do ponto.");
	            return;
	        }
	
	        Integer idPonto;
	        try {
	            idPonto = Integer.parseInt(idPontoStr);
	        } catch (NumberFormatException e) {
	            JOptionPane.showMessageDialog(this, "ID do ponto deve ser um número inteiro.");
	            return;
	        }
	
	        JSONObject mensagem = new JSONObject();
	        mensagem.put("action", "pedido-edicao-ponto");
	        JSONObject data = new JSONObject();
	        data.put("token", token);
	        data.put("ponto_id", idPonto);
	        mensagem.put("data", data);
	        try {
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);        
	            out.println(mensagem.toString());
	            System.out.println("PedidoEdicaoPonto-> Enviado para o servidor: "+mensagem);
	
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String resposta = in.readLine();
	            System.out.println("PedidoEdicaoPonto<- Recebida do servidor: "+resposta);
	            
	            JSONObject respostaJson = new JSONObject(resposta);
	            String action = respostaJson.optString("action");
	           	Boolean error = respostaJson.optBoolean("error");
	            String message = respostaJson.optString("message");
	            
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
	            	JSONObject respostaData = respostaJson.getJSONObject("data");
	                JSONObject pontoData = respostaData.getJSONObject("ponto");
	                String name =pontoData.getString("name");
	                String obs = pontoData.getString("obs");
	                Integer id = pontoData.getInt("id");
	                if (name == null || name.isEmpty()) {
	                    JOptionPane.showMessageDialog(this, "'name' não enviado pelo Servidor");
	                    return;
	                }
	                if (obs == null || obs.isEmpty()) {
	                    System.out.println("'obs' não enviado pelo Servidor ou nulo");
	                }
	                if (id == null || id.toString().isEmpty()) {
	                    JOptionPane.showMessageDialog(this, "'id' não enviado pelo Servidor");
	                    return;
	                }

	                
	                idPontoField.setEnabled(false);
	                nomeFieldEditar.setEnabled(true);
	                obsFieldEditar.setEnabled(true);
	                idPontoField.setText(id.toString());
	                nomeFieldEditar.setText(name);
	                obsFieldEditar.setText(obs);
	                JOptionPane.showMessageDialog(this, message);
	            }else {
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
        
    private void edicaoPonto() {
    	String idPontoStr = idPontoField.getText();
        if (idPontoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o ID do ponto.");
            return;
        }

        Integer idPonto;
        try {
            idPonto = Integer.parseInt(idPontoStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID do ponto deve ser um número inteiro.");
            return;
        }
	
	        String name = nomeFieldEditar.getText();
	        String obs = obsFieldEditar.getText();	
	        JSONObject mensagem = new JSONObject();
	        JSONObject data = new JSONObject();
	        if (obs == null || obs.isEmpty()) {
	        	obs = null;
	        }
        	mensagem.put("action", "edicao-ponto");
        	data.put("ponto_id", idPonto);	       
	        data.put("token", token);
	        data.put("name", name);
	        data.put("obs", obs);
	        mensagem.put("data", data);
	        
	        try {
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);           
	            out.println(mensagem.toString());
	            System.out.println("EdicaoPonto-> Enviado para o servidor: "+mensagem);
	    
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String resposta = in.readLine();
	            System.out.println("EdicaoPonto<- Resposta do servidor: " + resposta);
	            
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
	            JOptionPane.showMessageDialog(this, "A conexão foi encerrada pelo servidor. Tente novamente.");
	            voltarTelaPrincipal();
	        } catch (IOException e) {
	            e.printStackTrace();
	            JOptionPane.showMessageDialog(this, "Erro ao receber resposta do servidor.");
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
