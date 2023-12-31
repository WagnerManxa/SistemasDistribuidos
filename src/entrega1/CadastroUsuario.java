package entrega1;

import javax.swing.*;

import org.json.JSONObject;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class CadastroUsuario extends JFrame {
    private JTextField nomeField;
    private JTextField emailField;
    private JPasswordField senhaField;
    private Socket socket;
    private JComboBox<String> tipoComboBox;
    private TelaPrincipal telaPrincipal;
    private String token;

    public CadastroUsuario(Socket socket, TelaPrincipal telaPrincipal, String token) {
        this.socket = socket;
        this.telaPrincipal = telaPrincipal;
        this.token = token;

        setTitle("Cadastro de Usuário");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel nomeLabel = new JLabel("Nome:");
        nomeField = new JTextField(20);

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);
        
        JLabel tipoLabel = new JLabel("Tipo de Usuário:");
        String[] tipos = {"admin", "user"};
        tipoComboBox = new JComboBox<>(tipos);
        tipoComboBox.setSelectedIndex(1);

        JLabel senhaLabel = new JLabel("Senha:");
        senhaField = new JPasswordField(20);

        constraints.gridy = 0;
        panel.add(nomeLabel, constraints);

        constraints.gridy = 1;
        panel.add(emailLabel, constraints);

        constraints.gridy = 2;
        panel.add(tipoLabel, constraints);

        constraints.gridy = 3;
        panel.add(senhaLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(nomeField, constraints);

        constraints.gridy = 1;
        panel.add(emailField, constraints);

        constraints.gridy = 2;
        panel.add(tipoComboBox, constraints);

        constraints.gridy = 3;
        panel.add(senhaField, constraints);

        JButton cadastrarButton = new JButton("Cadastrar");
        cadastrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cadastrarUsuario();
            }
        });

        JButton backButton = new JButton("Voltar");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                voltarTelaPrincipal();
            }
        });

        constraints.gridy = 5;
        panel.add(backButton, constraints);

        constraints.gridy = 4;
        panel.add(cadastrarButton, constraints);

        add(panel);
        setLocationRelativeTo(null);
    }

    private void cadastrarUsuario() {
        String nome = nomeField.getText();
        String email = emailField.getText();
        String tipo = (String) tipoComboBox.getSelectedItem();
        String senha = new String(senhaField.getPassword());

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos são obrigatórios.");
            return;
        }

        if (senha.length() < 6) {
            JOptionPane.showMessageDialog(this, "A senha deve ter no minimo 6 caracteres.");
            return;
        }

        String senhaMD5 = hashMD5(senha).toUpperCase();

        JSONObject mensagem = new JSONObject();
        mensagem.put("action", "cadastro-usuario");

        JSONObject data = new JSONObject();
        data.put("name", nome);
        data.put("token", token);
        data.put("email", email);
        data.put("type", tipo);
        data.put("password", senhaMD5);
        mensagem.put("data", data);

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);            
            out.println(mensagem.toString());
            System.out.println("CadastroUsuario-> Enviado para o servidor: "+mensagem);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
            if (resposta != null) {
	            System.out.println("TelaCadastro<-Recebida do servidor: "+ resposta);
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

    private String hashMD5(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] senhaBytes = senha.getBytes();
            byte[] hashBytes = md.digest(senhaBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

