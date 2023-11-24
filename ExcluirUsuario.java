package entrega1;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

public class ExcluirUsuario extends JFrame {
    private JTextField idUsuarioField;
    private JTextField emailFieldEditar;
    private JPasswordField senhaFieldEditar;
    private Socket socket;
    private TelaPrincipal telaPrincipal;
    private String token;

    public ExcluirUsuario(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Excluir Usuário");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        
        JLabel paginaExcluitLabel = new JLabel("Informe os dados do usuário para exclusão:");
        
        JLabel idUsuarioLabel = new JLabel("ID do Usuário:");
        idUsuarioField = new JTextField();
        idUsuarioField.setPreferredSize(new Dimension(150, 25));
        
        JLabel emailLabelEditar = new JLabel("Email:");
        emailFieldEditar = new JTextField(20);
        emailFieldEditar.setEnabled(false);
        
        
        JLabel senhaLabelEditar = new JLabel("Senha:");
        senhaFieldEditar = new JPasswordField(20);


        JButton excluirButton = new JButton("Excluir");
        excluirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              excluir();
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
        panel.add(paginaExcluitLabel, constraints);


        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(idUsuarioLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(idUsuarioField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(emailLabelEditar, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        panel.add(emailFieldEditar, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(senhaLabelEditar, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        panel.add(senhaFieldEditar, constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;        
        panel.add(excluirButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;        
        panel.add(voltarButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        panel.add(cancelarButton, constraints);

        getContentPane().add(panel);

        pack();
        setLocationRelativeTo(null);
        if (JwtUtil.isUserAdmin(token)) {
        	idUsuarioField.setEnabled(true);
        	emailFieldEditar.setEnabled(false);
        	senhaFieldEditar.setEnabled(false);
        	
        }else {
        	idUsuarioField.setEnabled(false);
        	emailFieldEditar.setEnabled(true);
        	senhaFieldEditar.setEnabled(true);
        }
    }
    private void excluir() {
    	String email = emailFieldEditar.getText();
        String senha = String.valueOf(senhaFieldEditar.getPassword());
    	JSONObject mensagem = new JSONObject();		
    	if (!JwtUtil.isUserAdmin(token)) {
    		mensagem.put("action", "excluir-proprio-usuario");
            JSONObject data = new JSONObject();
            data.put("token", token); 
            data.put("email", email);
            data.put("password", DigestUtils.md5Hex(senha).toUpperCase());
            mensagem.put("data", data);
    	}else {
    		mensagem.put("action", "excluir-usuario");
        	String user_id = idUsuarioField.getText();
            JSONObject data = new JSONObject();
            data.put("token", token); 
            data.put("user_id", user_id);
            mensagem.put("data", data);
    	}
        
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
                JOptionPane.showMessageDialog(this, message);
                limparCampos();
                 
            }else {
                JOptionPane.showMessageDialog(this, message);
            	}
            }
		} catch (IOException e) {
		    System.out.println("ListarUsuario->Erro ao tentar enviar comando de listar usuarios: " + e.getMessage());
		    
		}
    }
    
    
    private void limparCampos() {
        emailFieldEditar.setText("");
        idUsuarioField.setText("");
        senhaFieldEditar.setText("");
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
