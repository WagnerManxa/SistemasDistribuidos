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

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

public class EditarUsuario extends JFrame {
    private JTextField idUsuarioField;
    private JTextField nomeFieldEditar;
    private JTextField emailFieldEditar;
    private JComboBox<String> tipoComboBoxEditar;
    private JPasswordField senhaFieldEditar;
    private Socket socket;
    private TelaPrincipal telaPrincipal;
    private String token;

    public EditarUsuario(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Editar Usuário");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel idUsuarioLabel = new JLabel("ID do Usuário:");
        idUsuarioField = new JTextField();
        idUsuarioField.setPreferredSize(new Dimension(150, 25));

        JLabel nomeLabelEditar = new JLabel("Nome:");
        nomeFieldEditar = new JTextField(20);
        nomeFieldEditar.setEnabled(false);
        
        JLabel emailLabelEditar = new JLabel("Email:");
        emailFieldEditar = new JTextField(20);
        emailFieldEditar.setEnabled(false);
        
        JLabel tipoLabelEditar = new JLabel("Tipo de Usuário:");
        String[] tiposEditar = {"admin", "user"};
        tipoComboBoxEditar = new JComboBox<>(tiposEditar);
        tipoComboBoxEditar.setSelectedIndex(1);
        tipoComboBoxEditar.setEnabled(false);
        
        JLabel senhaLabelEditar = new JLabel("Senha:");
        senhaFieldEditar = new JPasswordField(20);

        JButton buscarButton = new JButton("Buscar Usuário");
        buscarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscarUsuario();
            }
        });

        JButton salvarButton = new JButton("Salvar Alterações");
        salvarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                salvarAlteracoes();
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
        panel.add(idUsuarioLabel, constraints);

        constraints.gridx = 1;
        panel.add(idUsuarioField, constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        panel.add(nomeLabelEditar, constraints);

        constraints.gridx = 1;
        panel.add(nomeFieldEditar, constraints);

        constraints.gridy = 2;
        constraints.gridx = 0;
        panel.add(emailLabelEditar, constraints);

        constraints.gridx = 1;
        panel.add(emailFieldEditar, constraints);

        constraints.gridy = 3;
        constraints.gridx = 0;
        panel.add(tipoLabelEditar, constraints);

        constraints.gridx = 1;
        panel.add(tipoComboBoxEditar, constraints);

        constraints.gridy = 4;
        constraints.gridx = 0;
        panel.add(senhaLabelEditar, constraints);

        constraints.gridx = 1;
        panel.add(senhaFieldEditar, constraints);

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
    }
    private void limparCampos() {
    	nomeFieldEditar.setText("");
        emailFieldEditar.setText("");
        idUsuarioField.setText("");
        senhaFieldEditar.setText("");
        idUsuarioField.setEnabled(true);
        senhaFieldEditar.setEnabled(false);
    	nomeFieldEditar.setEnabled(false);
        emailFieldEditar.setEnabled(false);
        tipoComboBoxEditar.setEnabled(false);
    }

    private void buscarUsuario() {
    	if (JwtUtil.isUserAdmin(token)) {
	        String idUsuarioStr = idUsuarioField.getText();
	        if (idUsuarioStr.isEmpty()) {
	            JOptionPane.showMessageDialog(this, "Por favor, informe o ID do usuário.");
	            return;
	        }
	
	        int idUsuario;
	        try {
	            idUsuario = Integer.parseInt(idUsuarioStr);
	        } catch (NumberFormatException e) {
	            JOptionPane.showMessageDialog(this, "ID do usuário deve ser um número inteiro.");
	            return;
	        }
	
	        JSONObject mensagem = new JSONObject();
	        mensagem.put("action", "pedido-edicao-usuario");
	        JSONObject data = new JSONObject();
	        data.put("token", token);
	        data.put("user_id", idUsuario);
	        mensagem.put("data", data);
	        try {
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);        
	            out.println(mensagem.toString());
	            System.out.println("EditarUsuario-> Enviado para o servidor: "+mensagem);
	
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String resposta = in.readLine();
	            senhaFieldEditar.setEnabled(false);
	            System.out.println("EditarUsuario<- Recebida do servidor: "+resposta);
	            
	            JSONObject respostaJson = new JSONObject(resposta);
	            String action = respostaJson.optString("action");
	           	boolean error = respostaJson.optBoolean("error");
	            String message = respostaJson.optString("message");
	 
	            if (!error) {
	            	JSONObject respostaData = respostaJson.getJSONObject("data");
	                JSONObject usuarioData = respostaData.getJSONObject("user");
	                String nome =usuarioData.getString("name");
	                String email = usuarioData.getString("email");
	                String tipo = usuarioData.getString("type");
	                String id = usuarioData.getString("id");
	                if (nome.isEmpty()) {
	                    JOptionPane.showMessageDialog(this, "Nome não envaido pelo Servidor");
	                    return;
	                }
	                if (email.isEmpty()) {
	                    JOptionPane.showMessageDialog(this, "E-mail não envaido pelo Servidor");
	                    return;
	                }
	                if (tipo.isEmpty()) {
	                    JOptionPane.showMessageDialog(this, "Tipo de usuario não envaido pelo Servidor");
	                    return;
	                }
	                if (Integer.parseInt(JwtUtil.getUserIdFromToken(token)) == idUsuario) {
	                	senhaFieldEditar.setEnabled(true);
	                }
	                idUsuarioField.setEnabled(false);
	                nomeFieldEditar.setEnabled(true);
	                emailFieldEditar.setEnabled(true);
	                tipoComboBoxEditar.setEnabled(true);
	                idUsuarioField.setText(id);
	                nomeFieldEditar.setText(nome);
	                emailFieldEditar.setText(email);
	                tipoComboBoxEditar.setSelectedItem(tipo);
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
        } else {
        	buscarMeusDados();
        }
    	
    }
    
    private void buscarMeusDados() {
    	JSONObject mensagem = new JSONObject();
        mensagem.put("action", "pedido-proprio-usuario");
        JSONObject data = new JSONObject();
        data.put("token", token);
        mensagem.put("data", data);
        
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);        
            out.println(mensagem.toString());
            System.out.println("EditarUsuario-> Enviado para o servidor: "+mensagem);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
            senhaFieldEditar.setEnabled(false);
            System.out.println("EditarUsuario<- Recebida do servidor: "+resposta);
            
            JSONObject respostaJson = new JSONObject(resposta);
            String action = respostaJson.optString("action");
           	boolean error = respostaJson.optBoolean("error");
            String message = respostaJson.optString("message");
 
            if (!error) {
                JSONObject respostaData = respostaJson.getJSONObject("data");
                JSONObject usuarioData = respostaData.getJSONObject("user");
                String usuarioId = usuarioData.getString("id");
                String nome = usuarioData.getString("name");
                String email = usuarioData.getString("email");
                String tipo = usuarioData.getString("type");
                if (usuarioId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Id não envaido pelo Servidor");
                    return;
                }
                if (nome.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nome não envaido pelo Servidor");
                    return;
                }
                if (email.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "E-mail não envaido pelo Servidor");
                    return;
                }
                if (tipo.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tipo de usuario não envaido pelo Servidor");
                    return;
                }
                idUsuarioField.setEnabled(false);
                nomeFieldEditar.setEnabled(true);
                emailFieldEditar.setEnabled(true);
                if(JwtUtil.isUserAdmin(token)) {
                	tipoComboBoxEditar.setEnabled(true);	
                }else
                	tipoComboBoxEditar.setEnabled(false);
                senhaFieldEditar.setEnabled(true);
                idUsuarioField.setText(usuarioId);
                nomeFieldEditar.setText(nome);
                emailFieldEditar.setText(email);
                tipoComboBoxEditar.setSelectedItem(tipo);
                JOptionPane.showMessageDialog(this, message);
            }else {
            	 JOptionPane.showMessageDialog(this, message);
            }

        } catch (SocketException se) {
            JOptionPane.showMessageDialog(this, "A conexão foi encerrada pelo servidor. Tente novamente.");
            voltarTelaPrincipal();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao receber resposta do servidor.");
        }
    }
    
    private void salvarAlteracoes() {
    	
	        String idUsuarioStr = idUsuarioField.getText();
	        if (idUsuarioStr.isEmpty()) {
	            JOptionPane.showMessageDialog(this, "Por favor, informe o ID do usuário.");
	            return;
	        }
	
	        int idUsuario;
	        try {
	            idUsuario = Integer.parseInt(idUsuarioStr);
	        } catch (NumberFormatException e) {
	            JOptionPane.showMessageDialog(this, "ID do usuário deve ser um número inteiro.");
	            return;
	        }
	
	        String nome = nomeFieldEditar.getText();
	        String email = emailFieldEditar.getText();
	        String tipo = (String) tipoComboBoxEditar.getSelectedItem();
	        String senha = String.valueOf(senhaFieldEditar.getPassword());
	
	
	        JSONObject mensagem = new JSONObject();
	        JSONObject data = new JSONObject();
	        if (JwtUtil.isUserAdmin(token)) {
	        	mensagem.put("action", "edicao-usuario");
	        	data.put("user_id", idUsuario);
	        }else {
	        	mensagem.put("action", "autoedicao-usuario");
	        	data.put("id", idUsuario);
	        }
	       
	        data.put("token", token);
	        data.put("name", nome);
	        data.put("email", email);
	        data.put("type", tipo);
	        if (!senha.isEmpty()) {
	        	data.put("password", DigestUtils.md5Hex(senha).toUpperCase());
	        }else {
	        	data.put("password", "");
	        }
	        mensagem.put("data", data);
	        
	        try {
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);           
	            out.println(mensagem.toString());
	            System.out.println("EditarUsuario-> Enviado para o servidor: "+mensagem);
	    
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String resposta = in.readLine();
	            System.out.println("EditarUsuario<- Resposta do servidor: " + resposta);
	            
	            JSONObject respostaJson = new JSONObject(resposta);
	            String action = respostaJson.optString("action");
	           	boolean error = respostaJson.optBoolean("error");
	            String message = respostaJson.optString("message");
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
